package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.*;
import com.mycompany.myapp.domain.enumeration.OrderStatus;
import com.mycompany.myapp.repository.*;
import com.mycompany.myapp.service.dto.*;
import com.mycompany.myapp.service.dto.MonthlySalesDataDTO;
import com.mycompany.myapp.service.event.OrderNotificationEvent;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
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
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
    private final PurchaseOrderService purchaseOrderService;
    private final RestTemplate restTemplate;
    private final WarehouseMapper warehouseMapper;
    private final InventoryBalanceRepository inventoryBalanceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AiSchedulerService(
        ProductRepository productRepository,
        WarehouseRepository warehouseRepository,
        SalesOrderLineRepository salesOrderLineRepository,
        PurchaseOrderLineRepository purchaseOrderLineRepository,
        PurchaseOrderService purchaseOrderService,
        RestTemplate restTemplate,
        WarehouseMapper warehouseMapper,
        InventoryBalanceRepository inventoryBalanceRepository,
        ApplicationEventPublisher eventPublisher
    ) {
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.salesOrderLineRepository = salesOrderLineRepository;
        this.purchaseOrderLineRepository = purchaseOrderLineRepository;
        this.purchaseOrderService = purchaseOrderService;
        this.restTemplate = restTemplate;
        this.warehouseMapper = warehouseMapper;
        this.inventoryBalanceRepository = inventoryBalanceRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Cron Job: Chạy tự động vào 0h 00p ngày mùng 1 hàng tháng
     * AI tự động phân tích nhu cầu và lập Đơn nhập hàng nháp, bắn sự kiện thông báo cho Quản lý phòng Purchase theo Kho tương ứng
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void executeMonthlyRestockPrediction() {
        log.info("BẮT ĐẦU CHẠY CRON JOB: TRỢ LÝ AI DỰ BÁO NHẬP HÀNG TỰ ĐỘNG...");

        List<Warehouse> warehouses = warehouseRepository.findAll();
        List<Product> products = productRepository.findAll();

        String pythonAiUrl = "http://localhost:8000/predict";

        // Vòng lặp duyệt qua từng kho vật lý trên hệ thống
        for (Warehouse warehouse : warehouses) {
            Map<Long, AiRestockDraft> draftsBySupplier = new LinkedHashMap<>();

            // Vòng lặp duyệt qua từng sản phẩm để gọi AI dự báo lượng tiêu thụ tại Kho này
            for (Product product : products) {
                List<SalesOrderLine> historyLines = salesOrderLineRepository.findCompletedSalesHistory(product.getId(), warehouse.getId());

                if (historyLines.isEmpty()) {
                    continue;
                }

                // LẤY TỒN KHO THỰC TẾ TỪ BẢNG INVENTORY BALANCE
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
                    // Gọi API sang máy chủ Python FastAPI
                    AiPredictResponseDTO aiResponse = restTemplate.postForObject(pythonAiUrl, requestData, AiPredictResponseDTO.class);

                    if (aiResponse != null && aiResponse.getRecommend_restock() != null && aiResponse.getRecommend_restock() > 0) {
                        log.info(
                            "AI đề xuất nhập thêm {} sản phẩm cho sản phẩm ID: {} tại Kho: {}",
                            aiResponse.getRecommend_restock(),
                            product.getId(),
                            warehouse.getName()
                        );

                        Optional<Supplier> supplierOpt = findRecentSupplierForProduct(product.getId());
                        if (supplierOpt.isEmpty()) {
                            log.warn(
                                "Bỏ qua đề xuất AI cho sản phẩm ID {} tại Kho {} vì chưa có lịch sử nhập hàng để xác định Nhà cung cấp",
                                product.getId(),
                                warehouse.getName()
                            );
                            continue;
                        }

                        Supplier supplier = supplierOpt.get();
                        AiRestockDraft draft = draftsBySupplier.computeIfAbsent(supplier.getId(), ignored -> new AiRestockDraft(supplier));

                        PurchaseOrderLineDTO poLine = new PurchaseOrderLineDTO();
                        poLine.setQuantity(aiResponse.getRecommend_restock());
                        poLine.setUnitPrice(product.getPurchasePrice()); // Ép cứng đơn giá nhập tiêu chuẩn từ DB

                        ProductDTO productDTO = new ProductDTO();
                        productDTO.setId(product.getId());
                        poLine.setProduct(productDTO);

                        draft.lines.add(poLine);
                        draft.totalAmount =
                            draft.totalAmount.add(product.getPurchasePrice().multiply(new BigDecimal(aiResponse.getRecommend_restock())));
                    }
                } catch (Exception e) {
                    log.error("Lỗi khi gọi API AI Python cho sản phẩm ID {}: {}", product.getId(), e.getMessage());
                }
            }

            // NẾU CÓ MẶT HÀNG ĐƯỢC AI ĐỀ XUẤT NHẬP THÊM CHO KHO NÀY
            for (AiRestockDraft draft : draftsBySupplier.values()) {
                if (draft.lines.isEmpty()) {
                    continue;
                }

                PurchaseOrderDTO draftPo = new PurchaseOrderDTO();
                draftPo.setWarehouse(warehouseMapper.toDto(warehouse));
                draftPo.setStatus(OrderStatus.DRAFT);
                draftPo.setSupplier(toSupplierDTO(draft.supplier));
                draftPo.setPurchaseOrderLines(draft.lines);
                draftPo.setTotalAmount(draft.totalAmount);

                // 1. Tiến hành lưu đơn nhập hàng nháp xuống Database thông qua PurchaseOrderService
                PurchaseOrderDTO savedPo = purchaseOrderService.save(draftPo);
                log.info(
                    "Đã tự động tạo đơn nhập hàng nháp ID {} cho Kho: {}, Nhà cung cấp: {}",
                    savedPo.getId(),
                    warehouse.getName(),
                    draft.supplier.getName()
                );

                // 2. KHỞI TẠO VÀ PHÁT ĐI SỰ KIỆN EVENT-DRIVEN SANG LISTENER
                try {
                    // Trích xuất mã đơn hàng vừa sinh ra, nếu trống thì tạo mã tạm theo ID
                    String orderCode = (savedPo.getPoCode() != null) ? savedPo.getPoCode() : "AI-PO-" + savedPo.getId();

                    // Gọi Constructor: (orderType, action, orderId, orderCode, actionByUserLogin, originalCreatorLogin)
                    OrderNotificationEvent restockEvent = new OrderNotificationEvent(
                        "PURCHASE", // orderType: Phân loại chứng từ Mua hàng
                        "AI_RESTOCK_ALERT", // action: Đẻ ra một Case hành động mới tinh để Listener bắt bộ lọc riêng
                        savedPo.getId(), // orderId: ID đơn hàng vừa lưu thành công
                        orderCode, // orderCode: Mã đơn hàng
                        "SYSTEM_AI", // actionByUserLogin: Định danh tác nhân thực hiện thao tác
                        String.valueOf(warehouse.getId()) // originalCreatorLogin: "Mẹo" truyền trực tiếp ID Kho dưới dạng String sang Listener bóc tách
                    );

                    // Bắn sự kiện lên context ứng dụng để NotificationEventListener bắt lấy xử lý tiếp
                    eventPublisher.publishEvent(restockEvent);

                    log.info("Đã phát sóng sự kiện AI_RESTOCK_ALERT cho Đơn nhập tự động mã: {}", orderCode);
                } catch (Exception e) {
                    log.error("Lỗi khi kích hoạt luồng thông báo sự kiện AI cho kho {}: {}", warehouse.getName(), e.getMessage());
                }
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

    private Optional<Supplier> findRecentSupplierForProduct(Long productId) {
        return purchaseOrderLineRepository.findRecentSuppliersByProductId(productId, PageRequest.of(0, 1)).stream().findFirst();
    }

    private SupplierDTO toSupplierDTO(Supplier supplier) {
        SupplierDTO supplierDTO = new SupplierDTO();
        supplierDTO.setId(supplier.getId());
        return supplierDTO;
    }

    private static final class AiRestockDraft {

        private final Supplier supplier;
        private final List<PurchaseOrderLineDTO> lines = new ArrayList<>();
        private BigDecimal totalAmount = BigDecimal.ZERO;

        private AiRestockDraft(Supplier supplier) {
            this.supplier = supplier;
        }
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
