package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.InventoryTransaction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the InventoryTransaction entity.
 */
@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    default Optional<InventoryTransaction> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<InventoryTransaction> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<InventoryTransaction> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select distinct inventoryTransaction from InventoryTransaction inventoryTransaction left join fetch inventoryTransaction.product left join fetch inventoryTransaction.warehouse",
        countQuery = "select count(distinct inventoryTransaction) from InventoryTransaction inventoryTransaction"
    )
    Page<InventoryTransaction> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        "select distinct inventoryTransaction from InventoryTransaction inventoryTransaction left join fetch inventoryTransaction.product left join fetch inventoryTransaction.warehouse"
    )
    List<InventoryTransaction> findAllWithToOneRelationships();

    @Query(
        "select inventoryTransaction from InventoryTransaction inventoryTransaction left join fetch inventoryTransaction.product left join fetch inventoryTransaction.warehouse where inventoryTransaction.id =:id"
    )
    Optional<InventoryTransaction> findOneWithToOneRelationships(@Param("id") Long id);
}
