package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.service.AiSchedulerService;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiSchedulerService aiSchedulerService;

    public AiController(AiSchedulerService aiSchedulerService) {
        this.aiSchedulerService = aiSchedulerService;
    }

    /**
     * LUỒNG 1: Nút bấm chạy Demo ngay lập tức (Thay vì chờ đến 0h mùng 1)
     * Frontend chỉ cần gọi POST /api/ai/trigger-restock
     */
    @PostMapping("/trigger-restock")
    public ResponseEntity<String> triggerRestock() {
        aiSchedulerService.executeMonthlyRestockPrediction();
        return ResponseEntity.ok("Luồng AI tự động tạo đơn nhập hàng đã chạy thành công! Vui lòng kiểm tra màn hình Đơn Mua Hàng Nháp.");
    }

    /**
     * LUỒNG 2: Sếp thử nghiệm chiến dịch.
     * Dùng GET và @RequestParam để khỏi phải tạo thêm DTO Class nào.
     * Ví dụ gọi từ Frontend: /api/ai/simulate?productId=1&warehouseId=1&discountPercent=15&marketingSpend=2000000&isHoliday=1
     */
    @GetMapping("/simulate")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Map<String, Object>> simulateCampaign(
        @RequestParam Long productId,
        @RequestParam Long warehouseId,
        @RequestParam BigDecimal discountPercent,
        @RequestParam BigDecimal marketingSpend,
        @RequestParam Integer isHoliday
    ) {
        Map<String, Object> result = aiSchedulerService.simulateSingleCampaign(
            productId,
            warehouseId,
            discountPercent,
            marketingSpend,
            isHoliday
        );
        return ResponseEntity.ok(result);
    }
}
