package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Product;
import com.mycompany.myapp.domain.SalesOrder;
import com.mycompany.myapp.domain.SalesOrderLine;
import com.mycompany.myapp.repository.SalesOrderRepository;
import com.mycompany.myapp.service.dto.dashboard.DashboardResponseDTO;
import com.mycompany.myapp.service.dto.dashboard.DashboardSummaryDTO;
import com.mycompany.myapp.service.dto.dashboard.MonthlyTrendDTO;
import com.mycompany.myapp.service.dto.dashboard.TopProductDTO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final Logger log = LoggerFactory.getLogger(DashboardService.class);
    private final SalesOrderRepository salesOrderRepository;

    public DashboardService(SalesOrderRepository salesOrderRepository) {
        this.salesOrderRepository = salesOrderRepository;
    }

    /**
     * Hàm chính xử lý logic trả về Báo cáo cho Sếp
     * @param warehouseId ID của kho (Nếu null thì lấy toàn công ty)
     * @param year Năm muốn xem chi tiết (Nếu null thì lấy toàn thời gian)
     * @param month Tháng muốn xem chi tiết (Nếu null thì lấy toàn thời gian)
     */
    public DashboardResponseDTO getDashboardData(Long warehouseId, Integer year, Integer month) {
        List<SalesOrder> orders;

        // 1. KÉO DỮ LIỆU TỪ DATABASE LÊN DỰA VÀO BỘ LỌC
        if (year != null && month != null) {
            // Sếp chọn 1 tháng cụ thể
            YearMonth ym = YearMonth.of(year, month);
            Instant startDate = ym.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endDate = ym.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

            orders = salesOrderRepository.findCompletedByTimeRangeForDashboard(warehouseId, startDate, endDate);
        } else {
            // Sếp không chọn tháng -> Lấy toàn bộ để vẽ biểu đồ
            orders = salesOrderRepository.findAllCompletedForDashboard(warehouseId);
        }

        // 2. CÁC BIẾN LƯU TRỮ TẠM THỜI ĐỂ CỘNG DỒN
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;

        // Dùng TreeMap để các tháng tự động sắp xếp theo thứ tự thời gian (VD: 01/2026 -> 02/2026)
        Map<String, MonthlyTrendDTO> trendMap = new TreeMap<>();
        Map<Long, TopProductDTO> productMap = new HashMap<>();

        // Formatter để in ra chữ "MM/yyyy" cho Frontend vẽ trục X
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy").withZone(ZoneId.systemDefault());

        // 3. VÒNG LẶP MA THUẬT: BÓC TÁCH TÀI CHÍNH
        for (SalesOrder order : orders) {
            String monthLabel = formatter.format(order.getCreatedDate());

            // Lấy điểm biểu đồ của tháng này ra (Nếu chưa có thì tạo mới)
            MonthlyTrendDTO trendPoint = trendMap.computeIfAbsent(
                monthLabel,
                k -> new MonthlyTrendDTO(monthLabel, BigDecimal.ZERO, BigDecimal.ZERO)
            );

            for (SalesOrderLine line : order.getOrderLines()) {
                Product product = line.getProduct();
                if (product == null) continue;

                BigDecimal qty = new BigDecimal(line.getQuantity());

                // Tính Doanh thu của dòng này (Đã trừ đi % giảm giá của chính dòng đó)
                BigDecimal discountPercent = line.getDiscountPercent() != null ? line.getDiscountPercent() : BigDecimal.ZERO;
                BigDecimal baseAmount = line.getUnitPrice().multiply(qty);
                BigDecimal discountAmount = baseAmount.multiply(discountPercent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                BigDecimal lineRevenue = baseAmount.subtract(discountAmount);

                // Tính Chi phí vốn (Giá nhập x Số lượng)
                BigDecimal lineCost = product.getPurchasePrice().multiply(qty);

                // Tính Lợi nhuận của dòng này
                BigDecimal lineProfit = lineRevenue.subtract(lineCost);

                // --- CỘNG DỒN VÀO TỔNG CỤC ---
                totalRevenue = totalRevenue.add(lineRevenue);
                totalProfit = totalProfit.add(lineProfit);

                // --- CỘNG DỒN VÀO BIỂU ĐỒ THÁNG ---
                trendPoint.setRevenue(trendPoint.getRevenue().add(lineRevenue));
                trendPoint.setProfit(trendPoint.getProfit().add(lineProfit));

                // --- CỘNG DỒN VÀO BẢNG XẾP HẠNG SẢN PHẨM ---
                TopProductDTO topP = productMap.computeIfAbsent(
                    product.getId(),
                    k -> {
                        TopProductDTO dto = new TopProductDTO();
                        dto.setProductId(product.getId());
                        dto.setProductName(product.getName());
                        dto.setQuantitySold(0);
                        dto.setProfitBrought(BigDecimal.ZERO);
                        return dto;
                    }
                );
                topP.setQuantitySold(topP.getQuantitySold() + line.getQuantity());
                topP.setProfitBrought(topP.getProfitBrought().add(lineProfit));
            }
        }

        // 4. CHẮT LỌC TOP 5 SẢN PHẨM LỢI NHUẬN CAO NHẤT
        List<TopProductDTO> topProductsList = productMap
            .values()
            .stream()
            .sorted((p1, p2) -> p2.getProfitBrought().compareTo(p1.getProfitBrought())) // Sắp xếp giảm dần theo Lợi nhuận
            .limit(5) // Cắt lấy đúng 5 ông top đầu
            .collect(Collectors.toList());

        // 5. ĐÓNG GÓI JSON GỬI LÊN MÀN HÌNH
        DashboardResponseDTO response = new DashboardResponseDTO();
        response.setSummary(new DashboardSummaryDTO(totalRevenue, totalProfit));

        // Nếu sếp lọc theo tháng thì không cần trả list biểu đồ, nếu xem toàn cảnh thì trả list
        if (year == null && month == null) {
            response.setTrendCharts(new ArrayList<>(trendMap.values()));
        } else {
            response.setTrendCharts(new ArrayList<>()); // Trả list rỗng
        }

        response.setTopProducts(topProductsList);

        return response;
    }

    /**
     * Xuất báo cáo tài chính tổng hợp và Top 5 sản phẩm ra file Excel (Mảng byte)
     * @param warehouseId ID kho (null nếu chọn toàn hệ thống)
     * @param startDate Ngày bắt đầu kịch bản lọc
     * @param endDate Ngày kết thúc kịch bản lọc
     */
    public byte[] exportDashboardToExcel(Long warehouseId, Instant startDate, Instant endDate) {
        // 1. Lấy dữ liệu thô từ Database lên thông qua Repository tùy chỉnh
        List<SalesOrder> orders = salesOrderRepository.findCompletedByTimeRangeForDashboard(warehouseId, startDate, endDate);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;
        Map<Long, TopProductDTO> productMap = new HashMap<>();

        // 2. Thuật toán tính toán doanh thu, lợi nhuận thực tế (đã trừ chiết khấu dòng và giá nhập)
        for (SalesOrder order : orders) {
            for (SalesOrderLine line : order.getOrderLines()) {
                Product product = line.getProduct();
                if (product == null) continue;

                BigDecimal qty = new BigDecimal(line.getQuantity());

                // Tính toán doanh thu thực của dòng (UnitPrice gốc từ bảng Product)
                BigDecimal discountPercent = line.getDiscountPercent() != null ? line.getDiscountPercent() : BigDecimal.ZERO;
                BigDecimal baseAmount = line.getUnitPrice().multiply(qty);
                BigDecimal discountAmount = baseAmount.multiply(discountPercent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                BigDecimal lineRevenue = baseAmount.subtract(discountAmount);

                // Tính toán chi phí vốn dựa trên giá nhập gốc trong bảng Product
                BigDecimal lineCost = product.getPurchasePrice().multiply(qty);
                BigDecimal lineProfit = lineRevenue.subtract(lineCost);

                // Cộng dồn các chỉ số tài chính cốt lõi
                totalRevenue = totalRevenue.add(lineRevenue);
                totalProfit = totalProfit.add(lineProfit);

                // Gom nhóm sản phẩm để chuẩn bị lập bảng xếp hạng
                TopProductDTO topP = productMap.computeIfAbsent(
                    product.getId(),
                    k -> {
                        TopProductDTO dto = new TopProductDTO();
                        dto.setProductId(product.getId());
                        dto.setProductName(product.getName());
                        dto.setQuantitySold(0);
                        dto.setProfitBrought(BigDecimal.ZERO);
                        return dto;
                    }
                );
                topP.setQuantitySold(topP.getQuantitySold() + line.getQuantity());
                topP.setProfitBrought(topP.getProfitBrought().add(lineProfit));
            }
        }

        // Chắt lọc lấy đúng 5 sản phẩm sinh lời lớn nhất
        List<TopProductDTO> topProductsList = productMap
            .values()
            .stream()
            .sorted((p1, p2) -> p2.getProfitBrought().compareTo(p1.getProfitBrought()))
            .limit(5)
            .collect(Collectors.toList());

        // 3. Tiến hành vẽ đồ họa bảng tính ảo trên bộ nhớ RAM bằng Apache POI
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Báo cáo kinh doanh");

            // Thiết lập phong cách thiết kế (Styles) cho bảng tính chuyên nghiệp
            XSSFFont titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            XSSFCellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);

            XSSFFont boldFont = workbook.createFont();
            boldFont.setBold(true);
            XSSFCellStyle boldLabelStyle = workbook.createCellStyle();
            boldLabelStyle.setFont(boldFont);

            XSSFCellStyle tableHeaderStyle = workbook.createCellStyle();
            tableHeaderStyle.setFont(boldFont);
            tableHeaderStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            tableHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setCellBorders(tableHeaderStyle);

            XSSFCellStyle dataStyle = workbook.createCellStyle();
            setCellBorders(dataStyle);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());

            // --- VẼ KHU VỰC TIÊU ĐỀ (HEADER METADATA) ---
            XSSFRow row0 = sheet.createRow(0);
            XSSFCell cell0 = row0.createCell(0);
            cell0.setCellValue("BÁO CÁO KẾT QUẢ KINH DOANH CHI TIẾT");
            cell0.setCellStyle(titleStyle);

            XSSFRow row1 = sheet.createRow(1);
            row1
                .createCell(0)
                .setCellValue("Khoảng thời gian: Từ ngày " + formatter.format(startDate) + " đến ngày " + formatter.format(endDate));

            XSSFRow row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("Chi nhánh áp dụng: " + (warehouseId == null ? "Toàn bộ hệ thống" : "Kho ID " + warehouseId));

            // --- VẼ BỨC TRANH TÀI CHÍNH TỔNG HỢP (SUMMARY SUMMARY) ---
            XSSFRow row4 = sheet.createRow(4);
            XSSFCell lblRev = row4.createCell(0);
            lblRev.setCellValue("TỔNG DOANH THU THỰC TẾ:");
            lblRev.setCellStyle(boldLabelStyle);
            row4.createCell(1).setCellValue(totalRevenue.doubleValue());

            XSSFRow row5 = sheet.createRow(5);
            XSSFCell lblProf = row5.createCell(0);
            lblProf.setCellValue("TỔNG LỢI NHUẬN THỰC TẾ:");
            lblProf.setCellStyle(boldLabelStyle);
            row5.createCell(1).setCellValue(totalProfit.doubleValue());

            // --- VẼ BẢNG XẾP HẠNG TOP 5 SẢN PHẨM ---
            XSSFRow row7 = sheet.createRow(7);
            XSSFCell lblTable = row7.createCell(0);
            lblTable.setCellValue("TOP 5 SẢN PHẨM MANG LẠI LỢI NHUẬN CAO NHẤT");
            lblTable.setCellStyle(boldLabelStyle);

            XSSFRow row8 = sheet.createRow(8);
            String[] tableHeaders = { "STT", "Mã Sản Phẩm (ID)", "Tên Sản Phẩm / Tên Sách", "Số Lượng Đã Bán", "Lợi Nhuận Đóng Góp (VNĐ)" };
            for (int i = 0; i < tableHeaders.length; i++) {
                XSSFCell cell = row8.createCell(i);
                cell.setCellValue(tableHeaders[i]);
                cell.setCellStyle(tableHeaderStyle);
            }

            int rowIdx = 9;
            int stt = 1;
            for (TopProductDTO prod : topProductsList) {
                XSSFRow row = sheet.createRow(rowIdx++);

                XSSFCell c0 = row.createCell(0);
                c0.setCellValue(stt++);
                c0.setCellStyle(dataStyle);
                XSSFCell c1 = row.createCell(1);
                c1.setCellValue(prod.getProductId());
                c1.setCellStyle(dataStyle);
                XSSFCell c2 = row.createCell(2);
                c2.setCellValue(prod.getProductName());
                c2.setCellStyle(dataStyle);
                XSSFCell c3 = row.createCell(3);
                c3.setCellValue(prod.getQuantitySold());
                c3.setCellStyle(dataStyle);
                XSSFCell c4 = row.createCell(4);
                c4.setCellValue(prod.getProfitBrought().doubleValue());
                c4.setCellStyle(dataStyle);
            }

            // Tự động căn chỉnh độ rộng của các cột vừa vặn với nội dung chữ
            for (int i = 0; i < tableHeaders.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Đóng gói dữ liệu workbook thành mảng bytes để truyền ra mạng
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Gặp lỗi nghiêm trọng trong quá trình dựng cấu trúc file Excel: ", e);
            throw new RuntimeException("Lỗi hệ thống không thể xuất file báo cáo kinh doanh", e);
        }
    }

    // Hàm phụ trợ tạo đường viền mảnh (Border) bao quanh các ô dữ liệu
    private void setCellBorders(XSSFCellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
