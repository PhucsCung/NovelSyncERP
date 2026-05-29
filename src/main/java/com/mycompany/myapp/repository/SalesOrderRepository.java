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

    // Lọc đơn hàng theo Phòng ban (Department) của Manager đang đăng nhập
    @Query(
        value = "select distinct salesOrder from SalesOrder salesOrder " +
        "left join fetch salesOrder.customer " +
        "left join fetch salesOrder.employee e " +
        "left join fetch e.user u " +
        "left join fetch salesOrder.warehouse " +
        "where e.department.id = (select emp.department.id from Employee emp where emp.user.login = :login)",
        countQuery = "select count(distinct salesOrder) from SalesOrder salesOrder " +
        "left join salesOrder.employee e " +
        "where e.department.id = (select emp.department.id from Employee emp where emp.user.login = :login)"
    )
    Page<SalesOrder> findAllByEmployeeDepartment(@Param("login") String login, Pageable pageable);

    // Lọc chi tiết 1 đơn hàng theo phòng ban (dùng cho hàm findOne)
    @Query(
        "select distinct salesOrder from SalesOrder salesOrder " +
        "left join fetch salesOrder.customer " +
        "left join fetch salesOrder.employee e " +
        "left join fetch e.user u " +
        "left join fetch salesOrder.warehouse " +
        "where salesOrder.id = :id and " +
        "e.department.id = (select emp.department.id from Employee emp where emp.user.login = :login)"
    )
    Optional<SalesOrder> findOneByEmployeeDepartment(@Param("id") Long id, @Param("login") String login);
}
