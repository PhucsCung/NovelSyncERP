package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.TransferOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the TransferOrder entity.
 */
@Repository
public interface TransferOrderRepository extends JpaRepository<TransferOrder, Long> {
    default Optional<TransferOrder> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<TransferOrder> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<TransferOrder> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select distinct transferOrder from TransferOrder transferOrder left join fetch transferOrder.fromWarehouse left join fetch transferOrder.toWarehouse",
        countQuery = "select count(distinct transferOrder) from TransferOrder transferOrder"
    )
    Page<TransferOrder> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        "select distinct transferOrder from TransferOrder transferOrder left join fetch transferOrder.fromWarehouse left join fetch transferOrder.toWarehouse"
    )
    List<TransferOrder> findAllWithToOneRelationships();

    @Query(
        "select transferOrder from TransferOrder transferOrder left join fetch transferOrder.fromWarehouse left join fetch transferOrder.toWarehouse where transferOrder.id =:id"
    )
    Optional<TransferOrder> findOneWithToOneRelationships(@Param("id") Long id);

    // 1. Hàm lọc danh sách phân trang theo Kho của nhân viên
    @Query(
        value = "select distinct t from TransferOrder t " +
        "left join fetch t.fromWarehouse " +
        "left join fetch t.toWarehouse " +
        "where t.fromWarehouse.id in (select e.scopedWarehouse.id from Employee e where e.user.login = :login) " +
        "   or t.toWarehouse.id in (select e.scopedWarehouse.id from Employee e where e.user.login = :login)",
        countQuery = "select count(distinct t) from TransferOrder t " +
        "where t.fromWarehouse.id in (select e.scopedWarehouse.id from Employee e where e.user.login = :login) " +
        "   or t.toWarehouse.id in (select e.scopedWarehouse.id from Employee e where e.user.login = :login)"
    )
    Page<TransferOrder> findAllByEmployeeScopedWarehouse(@Param("login") String login, Pageable pageable);

    // 2. Hàm bảo vệ chi tiết (findOne) chống xem trộm bằng ID
    @Query(
        "select distinct t from TransferOrder t " +
        "left join fetch t.fromWarehouse " +
        "left join fetch t.toWarehouse " +
        "where t.id = :id and (" +
        "  t.fromWarehouse.id in (select e.scopedWarehouse.id from Employee e where e.user.login = :login) or " +
        "  t.toWarehouse.id in (select e.scopedWarehouse.id from Employee e where e.user.login = :login)" +
        ")"
    )
    Optional<TransferOrder> findOneByIdAndUserLogin(@Param("id") Long id, @Param("login") String login);
}
