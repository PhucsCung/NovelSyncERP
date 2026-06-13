package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.PurchaseOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the PurchaseOrder entity.
 */
@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    default Optional<PurchaseOrder> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<PurchaseOrder> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<PurchaseOrder> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select distinct purchaseOrder from PurchaseOrder purchaseOrder left join fetch purchaseOrder.supplier left join fetch purchaseOrder.employee left join fetch purchaseOrder.warehouse",
        countQuery = "select count(distinct purchaseOrder) from PurchaseOrder purchaseOrder"
    )
    Page<PurchaseOrder> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        "select distinct purchaseOrder from PurchaseOrder purchaseOrder left join fetch purchaseOrder.supplier left join fetch purchaseOrder.employee left join fetch purchaseOrder.warehouse"
    )
    List<PurchaseOrder> findAllWithToOneRelationships();

    @Query(
        "select purchaseOrder from PurchaseOrder purchaseOrder left join fetch purchaseOrder.supplier left join fetch purchaseOrder.employee left join fetch purchaseOrder.warehouse where purchaseOrder.id =:id"
    )
    Optional<PurchaseOrder> findOneWithToOneRelationships(@Param("id") Long id);

    @Query(
        value = "select distinct purchaseOrder from PurchaseOrder purchaseOrder " +
        "left join fetch purchaseOrder.supplier " +
        "left join fetch purchaseOrder.employee e " +
        "left join fetch e.user u " +
        "left join fetch purchaseOrder.warehouse " +
        "where u.login = :login",
        countQuery = "select count(distinct purchaseOrder) from PurchaseOrder purchaseOrder " +
        "left join purchaseOrder.employee e " +
        "left join e.user u " +
        "where u.login = :login"
    )
    Page<PurchaseOrder> findAllByEmployeeUserLogin(@Param("login") String login, Pageable pageable);

    //Lọc danh sách đơn hàng theo Kho của Manager đang đăng nhập
    @Query(
        value = "select distinct purchaseOrder from PurchaseOrder purchaseOrder " +
        "left join fetch purchaseOrder.supplier " +
        "left join fetch purchaseOrder.employee e " +
        "left join fetch e.user u " +
        "left join fetch purchaseOrder.warehouse " +
        "where purchaseOrder.warehouse.id in (select emp.scopedWarehouse.id from Employee emp where emp.user.login = :login)",
        countQuery = "select count(distinct purchaseOrder) from PurchaseOrder purchaseOrder " +
        "where purchaseOrder.warehouse.id in (select emp.scopedWarehouse.id from Employee emp where emp.user.login = :login)"
    )
    Page<PurchaseOrder> findAllByEmployeeScopedWarehouse(@Param("login") String login, Pageable pageable);

    //Lọc chi tiết 1 đơn hàng theo Kho (Chống xem trộm)
    @Query(
        "select distinct purchaseOrder from PurchaseOrder purchaseOrder " +
        "left join fetch purchaseOrder.supplier " +
        "left join fetch purchaseOrder.employee e " +
        "left join fetch e.user u " +
        "left join fetch purchaseOrder.warehouse " +
        "where purchaseOrder.id = :id and " +
        "purchaseOrder.warehouse.id in (select emp.scopedWarehouse.id from Employee emp where emp.user.login = :login)"
    )
    Optional<PurchaseOrder> findOneByIdAndEmployeeScopedWarehouse(@Param("id") Long id, @Param("login") String login);

    //Lọc chi tiết 1 đơn hàng dành riêng cho Purchaser (Chính chủ)
    @Query(
        "select distinct purchaseOrder from PurchaseOrder purchaseOrder " +
        "left join fetch purchaseOrder.supplier " +
        "left join fetch purchaseOrder.employee e " +
        "left join fetch e.user u " +
        "left join fetch purchaseOrder.warehouse " +
        "where purchaseOrder.id = :id and u.login = :login"
    )
    Optional<PurchaseOrder> findOneByIdAndEmployeeUserLogin(@Param("id") Long id, @Param("login") String login);

    boolean existsByEmployeeId(Long employeeId);
    // (Thay chữ EmployeeId bằng đúng tên property map với Employee trong file SalesOrder.java của bác)
}
