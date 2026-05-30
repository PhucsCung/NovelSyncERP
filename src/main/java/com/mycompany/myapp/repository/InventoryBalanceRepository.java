package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.InventoryBalance;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the InventoryBalance entity.
 */
@Repository
public interface InventoryBalanceRepository extends JpaRepository<InventoryBalance, Long> {
    default Optional<InventoryBalance> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<InventoryBalance> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<InventoryBalance> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select distinct inventoryBalance from InventoryBalance inventoryBalance left join fetch inventoryBalance.product left join fetch inventoryBalance.warehouse",
        countQuery = "select count(distinct inventoryBalance) from InventoryBalance inventoryBalance"
    )
    Page<InventoryBalance> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        "select distinct inventoryBalance from InventoryBalance inventoryBalance left join fetch inventoryBalance.product left join fetch inventoryBalance.warehouse"
    )
    List<InventoryBalance> findAllWithToOneRelationships();

    @Query(
        "select inventoryBalance from InventoryBalance inventoryBalance left join fetch inventoryBalance.product left join fetch inventoryBalance.warehouse where inventoryBalance.id =:id"
    )
    Optional<InventoryBalance> findOneWithToOneRelationships(@Param("id") Long id);

    Optional<InventoryBalance> findOneByProductIdAndWarehouseId(Long ProductId, Long WarehouseId);

    // 1. Hàm lọc tồn kho theo kho mà nhân viên được phân công
    @Query(
        value = "select distinct ib from InventoryBalance ib " +
        "left join fetch ib.product " +
        "left join fetch ib.warehouse " +
        "where ib.warehouse.id in (select e.scopedWarehouse.id from Employee e where e.user.login = :login)",
        countQuery = "select count(distinct ib) from InventoryBalance ib " +
        "where ib.warehouse.id in (select e.scopedWarehouse.id from Employee e where e.user.login = :login)"
    )
    Page<InventoryBalance> findAllByEmployeeScopedWarehouse(@Param("login") String login, Pageable pageable);

    // 2. Hàm bảo vệ chi tiết (chống xem trộm tồn kho chi nhánh khác bằng ID)
    @Query(
        "select distinct ib from InventoryBalance ib " +
        "left join fetch ib.product " +
        "left join fetch ib.warehouse " +
        "where ib.id = :id and " +
        "ib.warehouse.id in (select e.scopedWarehouse.id from Employee e where e.user.login = :login)"
    )
    Optional<InventoryBalance> findOneByIdAndUserLogin(@Param("id") Long id, @Param("login") String login);

    // Lấy danh sách tồn kho dựa vào ID Kho và danh sách ID Sản phẩm
    List<InventoryBalance> findByWarehouseIdAndProductIdIn(Long warehouseId, List<Long> productIds);
}
