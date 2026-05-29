package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.TransferOrderLine;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the TransferOrderLine entity.
 */
@Repository
public interface TransferOrderLineRepository extends JpaRepository<TransferOrderLine, Long> {
    default Optional<TransferOrderLine> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<TransferOrderLine> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<TransferOrderLine> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select distinct transferOrderLine from TransferOrderLine transferOrderLine left join fetch transferOrderLine.product",
        countQuery = "select count(distinct transferOrderLine) from TransferOrderLine transferOrderLine"
    )
    Page<TransferOrderLine> findAllWithToOneRelationships(Pageable pageable);

    @Query("select distinct transferOrderLine from TransferOrderLine transferOrderLine left join fetch transferOrderLine.product")
    List<TransferOrderLine> findAllWithToOneRelationships();

    @Query(
        "select transferOrderLine from TransferOrderLine transferOrderLine left join fetch transferOrderLine.product where transferOrderLine.id =:id"
    )
    Optional<TransferOrderLine> findOneWithToOneRelationships(@Param("id") Long id);

    // Lấy danh sách chi tiết của một phiếu điều chuyển
    List<TransferOrderLine> findByTransferOrderId(Long transferOrderId);
}
