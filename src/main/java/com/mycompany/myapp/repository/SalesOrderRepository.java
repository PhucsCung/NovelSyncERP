package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.SalesOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the SalesOrder entity.
 */
@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    default Optional<SalesOrder> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<SalesOrder> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<SalesOrder> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select distinct salesOrder from SalesOrder salesOrder left join fetch salesOrder.customer left join fetch salesOrder.employee left join fetch salesOrder.warehouse",
        countQuery = "select count(distinct salesOrder) from SalesOrder salesOrder"
    )
    Page<SalesOrder> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        "select distinct salesOrder from SalesOrder salesOrder left join fetch salesOrder.customer left join fetch salesOrder.employee left join fetch salesOrder.warehouse"
    )
    List<SalesOrder> findAllWithToOneRelationships();

    @Query(
        "select salesOrder from SalesOrder salesOrder left join fetch salesOrder.customer left join fetch salesOrder.employee left join fetch salesOrder.warehouse where salesOrder.id =:id"
    )
    Optional<SalesOrder> findOneWithToOneRelationships(@Param("id") Long id);

    @Query(
        value = "select distinct salesOrder from SalesOrder salesOrder " +
        "left join fetch salesOrder.customer " +
        "left join fetch salesOrder.employee e " +
        "left join fetch e.user u " +
        "left join fetch salesOrder.warehouse " +
        "where u.login = :login",
        countQuery = "select count(distinct salesOrder) from SalesOrder salesOrder " +
        "left join salesOrder.employee e " +
        "left join e.user u " +
        "where u.login = :login"
    )
    Page<SalesOrder> findAllByEmployeeUserLogin(@Param("login") String login, Pageable pageable);

    // Dành riêng cho Sales - Lọc chi tiết 1 đơn hàng theo chính User tạo ra nó
    @Query(
        "select distinct salesOrder from SalesOrder salesOrder " +
        "left join fetch salesOrder.customer " +
        "left join fetch salesOrder.employee e " +
        "left join fetch e.user u " +
        "left join fetch salesOrder.warehouse " +
        "where salesOrder.id = :id and u.login = :login"
    )
    Optional<SalesOrder> findOneByIdAndEmployeeUserLogin(@Param("id") Long id, @Param("login") String login);

    //Lọc danh sách đơn hàng theo Kho của Manager đang đăng nhập
    @Query(
        value = "select distinct salesOrder from SalesOrder salesOrder " +
        "left join fetch salesOrder.customer " +
        "left join fetch salesOrder.employee e " +
        "left join fetch e.user u " +
        "left join fetch salesOrder.warehouse " +
        "where salesOrder.warehouse.id in (select emp.scopedWarehouse.id from Employee emp where emp.user.login = :login)",
        countQuery = "select count(distinct salesOrder) from SalesOrder salesOrder " +
        "where salesOrder.warehouse.id in (select emp.scopedWarehouse.id from Employee emp where emp.user.login = :login)"
    )
    Page<SalesOrder> findAllByEmployeeScopedWarehouse(@Param("login") String login, Pageable pageable);

    //Lọc chi tiết 1 đơn hàng theo Kho (Chống xem trộm đơn chi nhánh khác bằng ID)
    @Query(
        "select distinct salesOrder from SalesOrder salesOrder " +
        "left join fetch salesOrder.customer " +
        "left join fetch salesOrder.employee e " +
        "left join fetch e.user u " +
        "left join fetch salesOrder.warehouse " +
        "where salesOrder.id = :id and " +
        "salesOrder.warehouse.id in (select emp.scopedWarehouse.id from Employee emp where emp.user.login = :login)"
    )
    Optional<SalesOrder> findOneByIdAndEmployeeScopedWarehouse(@Param("id") Long id, @Param("login") String login);

    // =================================================================================
    // CÁC HÀM DÀNH RIÊNG CHO TÍNH NĂNG DASHBOARD (THỐNG KÊ)
    // =================================================================================

    /**
     * 1. Dành cho Biểu đồ mặc định: Kéo toàn bộ lịch sử đơn hàng ĐÃ CHỐT SỔ.
     * Có hỗ trợ lọc theo Kho (Nếu warehouseId = null thì tự động lấy tất cả các kho).
     */
    @Query(
        "SELECT DISTINCT so FROM SalesOrder so " +
        "LEFT JOIN FETCH so.orderLines sol " +
        "LEFT JOIN FETCH sol.product p " +
        "WHERE so.status = 'COMPLETED' " +
        "AND (:warehouseId IS NULL OR so.warehouse.id = :warehouseId) " +
        "ORDER BY so.createdDate ASC"
    )
    List<SalesOrder> findAllCompletedForDashboard(@Param("warehouseId") Long warehouseId);

    /**
     * 2. Dành cho Bộ lọc chi tiết: Lấy các đơn chốt sổ trong 1 khoảng thời gian cụ thể (1 tháng).
     * Có hỗ trợ lọc theo Kho.
     */
    @Query(
        "SELECT DISTINCT so FROM SalesOrder so " +
        "LEFT JOIN FETCH so.orderLines sol " +
        "LEFT JOIN FETCH sol.product p " +
        "WHERE so.status = 'COMPLETED' " +
        "AND (:warehouseId IS NULL OR so.warehouse.id = :warehouseId) " +
        "AND so.createdDate >= :startDate AND so.createdDate <= :endDate " +
        "ORDER BY so.createdDate ASC"
    )
    List<SalesOrder> findCompletedByTimeRangeForDashboard(
        @Param("warehouseId") Long warehouseId,
        @Param("startDate") java.time.Instant startDate,
        @Param("endDate") java.time.Instant endDate
    );

    boolean existsByEmployeeId(Long employeeId);
    // (Thay chữ EmployeeId bằng đúng tên property map với Employee trong file SalesOrder.java của bác)
}
