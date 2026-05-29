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

    // Lọc đơn hàng theo Phòng ban (Department) của Manager đang đăng nhập
    @Query(
        value = "select distinct purchaseOrder from PurchaseOrder purchaseOrder " +
        "left join fetch purchaseOrder.supplier " +
        "left join fetch purchaseOrder.employee e " +
        "left join fetch e.user u " +
        "left join fetch purchaseOrder.warehouse " +
        "where e.department.id = (select emp.department.id from Employee emp where emp.user.login = :login)",
        countQuery = "select count(distinct purchaseOrder) from PurchaseOrder purchaseOrder " +
        "left join purchaseOrder.employee e " +
        "where e.department.id = (select emp.department.id from Employee emp where emp.user.login = :login)"
    )
    Page<PurchaseOrder> findAllByEmployeeDepartment(@Param("login") String login, Pageable pageable);

    // Lọc chi tiết 1 đơn hàng theo phòng ban (dùng cho hàm findOne)
    @Query(
        "select distinct purchaseOrder from PurchaseOrder purchaseOrder " +
        "left join fetch purchaseOrder.supplier " +
        "left join fetch purchaseOrder.employee e " +
        "left join fetch e.user u " +
        "left join fetch purchaseOrder.warehouse " +
        "where purchaseOrder.id = :id and " +
        "e.department.id = (select emp.department.id from Employee emp where emp.user.login = :login)"
    )
    Optional<PurchaseOrder> findOneByEmployeeDepartment(@Param("id") Long id, @Param("login") String login);
}
