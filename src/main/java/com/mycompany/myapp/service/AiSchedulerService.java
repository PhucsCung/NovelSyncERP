package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*;
import com.mycompany.myapp.repository.*;
import com.mycompany.myapp.service.dto.*;
import com.mycompany.myapp.service.dto.MonthlySalesDataDTO;
import com.mycompany.myapp.service.mapper.WarehouseMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional
public class AiSchedulerService {

    private final Logger log = LoggerFactory.getLogger(AiSchedulerService.class);

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final SalesOrderLineRepository salesOrderLineRepository;
    private final PurchaseOrderService purchaseOrderService;
    private final RestTemplate restTemplate;
    private final WarehouseMapper warehouseMapper;
    private final InventoryBalanceRepository inventoryBalanceRepository;
    private final SupplierRepository supplierRepository;

    public AiSchedulerService(
        ProductRepository productRepository,
        WarehouseRepository warehouseRepository,
        SalesOrderLineRepository salesOrderLineRepository,
        PurchaseOrderService purchaseOrderService,
        RestTemplate restTemplate,
        WarehouseMapper warehouseMapper,
        InventoryBalanceRepository inventoryBalanceRepository,
        SupplierRepository supplierRepository
    ) {
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.salesOrderLineRepository = salesOrderLineRepository;
        this.purchaseOrderService = purchaseOrderService;
        this.restTemplate = restTemplate;
        this.warehouseMapper = warehouseMapper;
        this.inventoryBalanceRepository = inventoryBalanceRepository;
        this.supplierRepository = supplierRepository;
    }

    /**
     * Cron Job: Chạy tự động vào 0h 00p ngày mùng 1 hàng tháng
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void executeMonthlyRestockPrediction() {
        log.info("BẮT ĐẦU CHẠY CRON JOB: TRỢ LÝ AI DỰ BÁO NHẬP HÀNG TỰ ĐỘNG...");

        List<Warehouse> warehouses = warehouseRepository.findAll();
        List<Product> products = productRepository.findAll();

        String pythonAiUrl = "http://localhost:8000/predict";

        // Fetch a default supplier for AI orders
        Supplier defaultSupplier = supplierRepository.findAll().stream().findFirst().orElse(null);

        for (Warehouse warehouse : warehouses) {
            PurchaseOrderDTO draftPo = new PurchaseOrderDTO();
            draftPo.setWarehouse(warehouseMapper.toDto(warehouse));
            if (defaultSupplier != null) {
                SupplierDTO sDto = new SupplierDTO();
                sDto.setId(defaultSupplier.getId());
                draftPo.setSupplier(sDto);
            }
            List<PurchaseOrderLineDTO> poLines = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (Product product : products) {
                List<SalesOrderLine> historyLines = salesOrderLineRepository.findCompletedSalesHistory(product.getId(), warehouse.getId());

                if (historyLines.isEmpty()) {
                    continue;
                }
                // LẤY TỒN KHO THỰC TẾ TỪ INVENTORY BALANCE
                int currentStock = 0;
                Optional<InventoryBalance> balanceOpt = inventoryBalanceRepository.findOneByProductIdAndWarehouseId(
                    product.getId(),
                    warehouse.getId()
                );

                if (balanceOpt.isPresent()) {
                    currentStock = balanceOpt.get().getQuantity();
                }

                AiPredictRequestDTO requestData = buildAiRequest(product, warehouse, historyLines, currentStock);

                try {
                    AiPredictResponseDTO aiResponse = restTemplate.postForObject(pythonAiUrl, requestData, AiPredictResponseDTO.class);

                    if (aiResponse != null && aiResponse.getRecommend_restock() != null && aiResponse.getRecommend_restock() > 0) {
                        log.info(
                            "AI đề xuất nhập thêm {} cuốn cho sản phẩm ID: {} tại Kho: {}",
                            aiResponse.getRecommend_restock(),
                            product.getId(),
                            warehouse.getName()
                        );

                        PurchaseOrderLineDTO poLine = new PurchaseOrderLineDTO();
                        poLine.setQuantity(aiResponse.getRecommend_restock());

                        // LẤY GIÁ NHẬP (PURCHASE PRICE) ĐỂ ĐIỀN VÀO ĐƠN MUA HÀNG
                        poLine.setUnitPrice(product.getPurchasePrice());
                        ProductDTO pDto = new ProductDTO();
                        pDto.setId(product.getId());
                        poLine.setProduct(pDto);

                        poLines.add(poLine);
                        totalAmount = totalAmount.add(poLine.getUnitPrice().multiply(new BigDecimal(poLine.getQuantity())));
                    }
                } catch (Exception e) {
                    log.error("Lỗi khi gọi API AI Python cho sản phẩm ID {}: {}", product.getId(), e.getMessage());
                }
            }

            if (!poLines.isEmpty()) {
                draftPo.setPurchaseOrderLines(poLines);
                draftPo.setTotalAmount(totalAmount);
                purchaseOrderService.save(draftPo);
                log.info("ĐÃ TỰ ĐỘNG TẠO ĐƠN NHẬP HÀNG NHÁP CHO KHO: {}", warehouse.getName());
            }
        }

        log.info("HOÀN THÀNH CRON JOB: TRỢ LÝ AI DỰ BÁO NHẬP HÀNG TỰ ĐỘNG!");
    }

    /**
     * Dùng cho Luồng 2: Sếp thử nghiệm chiến dịch (Tái sử dụng logic của Luồng 1)
     */
    public Map<String, Object> simulateSingleCampaign(
        Long productId,
        Long warehouseId,
        BigDecimal discountPercent,
        BigDecimal marketingSpend,
        Integer isHoliday
    ) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow(() -> new RuntimeException("Không tìm thấy kho"));

        List<SalesOrderLine> historyLines = salesOrderLineRepository.findCompletedSalesHistory(productId, warehouseId);

        // LẤY TỒN KHO THỰC TẾ TỪ INVENTORY BALANCE
        int currentStock = 0;
        Optional<InventoryBalance> balanceOpt = inventoryBalanceRepository.findOneByProductIdAndWarehouseId(
            product.getId(),
            warehouse.getId()
        );

        if (balanceOpt.isPresent()) {
            currentStock = balanceOpt.get().getQuantity();
        }

        AiPredictRequestDTO aiReq = buildAiRequest(product, warehouse, historyLines, currentStock);

        // 2. Ghi đè tham số của Sếp truyền vào cho tháng tới
        aiReq.setNext_month_price(product.getSellingPrice()); // Giá bán gốc
        aiReq.setNext_month_discount(discountPercent);
        aiReq.setNext_month_marketing(marketingSpend);
        aiReq.setNext_month_is_holiday(isHoliday);

        // 3. Gọi AI Python
        String pythonAiUrl = "http://localhost:8000/predict";
        AiPredictResponseDTO aiResponse = restTemplate.postForObject(pythonAiUrl, aiReq, AiPredictResponseDTO.class);

        // 4. Trả kết quả tự do bằng Map (Khỏi cần tạo DTO)
        Map<String, Object> result = new HashMap<>();
        result.put("productId", productId);
        result.put("warehouseId", warehouseId);
        result.put("scenario_discount", discountPercent);
        result.put("scenario_marketing", marketingSpend);

        if (aiResponse != null) {
            int predictedSales = aiResponse.getPredicted_sales();
            result.put("predicted_sales", predictedSales);

            // Khuyến mãi tính nhẩm cho Sếp xem Lợi nhuận gộp
            BigDecimal discountAmount = product
                .getSellingPrice()
                .multiply(discountPercent)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

            BigDecimal revenue = product.getSellingPrice().subtract(discountAmount).multiply(new BigDecimal(predictedSales));
            BigDecimal cost = product.getPurchasePrice().multiply(new BigDecimal(predictedSales));
            BigDecimal profit = revenue.subtract(cost).subtract(marketingSpend);

            result.put("expected_revenue", revenue);
            result.put("expected_profit", profit);
        } else {
            result.put("predicted_sales", 0);
        }

        return result;
    }

    /**
     * Hàm phụ trợ: Nhóm dữ liệu thật theo tháng
     */
    private AiPredictRequestDTO buildAiRequest(Product product, Warehouse warehouse, List<SalesOrderLine> historyLines, int currentStock) {
        AiPredictRequestDTO requestDTO = new AiPredictRequestDTO();
        requestDTO.setProduct_id(product.getId());
        requestDTO.setWarehouse_id(warehouse.getId());
        requestDTO.setCurrent_stock(currentStock);

        // CẤU HÌNH THÁNG SAU DÙNG GIÁ BÁN (SELLING PRICE) ĐỂ AI DỰ BÁO DOANH SỐ
        requestDTO.setNext_month_price(product.getSellingPrice());
        requestDTO.setNext_month_discount(BigDecimal.ZERO);
        requestDTO.setNext_month_marketing(BigDecimal.ZERO);
        requestDTO.setNext_month_is_holiday(0);

        // Gom nhóm theo tháng.
        Map<YearMonth, List<SalesOrderLine>> groupedByMonth = historyLines
            .stream()
            .collect(
                Collectors.groupingBy(
                    line -> {
                        Instant orderDate = line.getSalesOrder().getCreatedDate();
                        if (orderDate == null) orderDate = Instant.now();
                        return YearMonth.from(orderDate.atZone(ZoneId.systemDefault()));
                    },
                    TreeMap::new,
                    Collectors.toList()
                )
            );

        List<MonthlySalesDataDTO> historyList = new ArrayList<>();
        int monthIndex = 1;

        // Duyệt qua từng tháng đã gom để tính tổng
        for (Map.Entry<YearMonth, List<SalesOrderLine>> entry : groupedByMonth.entrySet()) {
            List<SalesOrderLine> linesInMonth = entry.getValue();

            MonthlySalesDataDTO monthData = new MonthlySalesDataDTO();
            monthData.setMonth(monthIndex++);

            // Tính tổng số lượng bán được trong tháng đó
            int totalVolume = linesInMonth.stream().mapToInt(SalesOrderLine::getQuantity).sum();
            monthData.setSales_volume(totalVolume);

            // Lấy mức giá bán ra trung bình của tháng đó
            BigDecimal avgPrice = linesInMonth
                .stream()
                .map(SalesOrderLine::getUnitPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(linesInMonth.size()), 2, RoundingMode.HALF_UP);
            monthData.setPrice(avgPrice);

            // Mặc định các feature khác
            monthData.setDiscount_percent(BigDecimal.ZERO);
            monthData.setMarketing_spend(BigDecimal.ZERO);

            // Đặt logic tự động nhận diện tháng cao điểm
            int currentMonthValue = entry.getKey().getMonthValue();
            monthData.setIs_holiday(currentMonthValue >= 9 ? 1 : 0);

            historyList.add(monthData);
        }

        requestDTO.setHistory(historyList);
        return requestDTO;
    }
}
