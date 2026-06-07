package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.service.DashboardService;
import com.mycompany.myapp.service.dto.dashboard.DashboardResponseDTO;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing Dashboard statistics.
 */
@RestController
@RequestMapping("/api")
public class DashboardController {

    private final Logger log = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * {@code GET  /dashboard} : Lấy dữ liệu thống kê cho màn hình Dashboard.
     * Tự động thay đổi dữ liệu trả về dựa trên các bộ lọc (filter) truyền lên.
     *
     * @param warehouseId (Tùy chọn) ID của kho cần xem. Nếu không truyền sẽ lấy toàn bộ các kho.
     * @param year (Tùy chọn) Năm cần xem chi tiết.
     * @param month (Tùy chọn) Tháng cần xem chi tiết.
     * @return {@link ResponseEntity} với status {@code 200 (OK)} và body là {@link DashboardResponseDTO}.
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.ACCOUNTANT + "\")")
    public ResponseEntity<DashboardResponseDTO> getDashboardData(
        @RequestParam(required = false) Long warehouseId,
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month
    ) {
        log.debug("REST request to get Dashboard data: warehouseId={}, year={}, month={}", warehouseId, year, month);

        DashboardResponseDTO result = dashboardService.getDashboardData(warehouseId, year, month);

        return ResponseEntity.ok().body(result);
    }

    /**
     * {@code GET  /dashboard/export} : Xuất file Excel báo cáo kinh doanh theo khoảng thời gian.
     *
     * @param warehouseId (Tùy chọn) ID của kho cần kết xuất số liệu.
     * @param startDate (Bắt buộc) Ngày bắt đầu định dạng ISO (VD: 2026-05-01T00:00:00Z)
     * @param endDate (Bắt buộc) Ngày kết thúc định dạng ISO (VD: 2026-05-31T23:59:59Z)
     * @return File Excel dạng mảng byte kèm mã trạng thái 200 (OK).
     */
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.ACCOUNTANT + "\")")
    @GetMapping("/dashboard/export")
    public ResponseEntity<byte[]> exportDashboardExcel(
        @RequestParam(required = false) Long warehouseId,
        @RequestParam Instant startDate,
        @RequestParam Instant endDate
    ) {
        log.debug("REST request to export Dashboard Excel data: warehouseId={}, start={}, end={}", warehouseId, startDate, endDate);

        // Gọi Service thực thi nghiệp vụ lập báo cáo và nhận về mảng byte
        byte[] excelData = dashboardService.exportDashboardToExcel(warehouseId, startDate, endDate);

        // Cấu hình tên file tải về tự động đi kèm mốc thời gian hệ thống để tránh trùng lặp
        String fileName = "Bao_Cao_Kinh_Doanh_" + Instant.now().toEpochMilli() + ".xlsx";

        return ResponseEntity
            .ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(excelData);
    }
}
