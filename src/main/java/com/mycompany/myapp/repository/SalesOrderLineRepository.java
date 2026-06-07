package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.SalesOrderLine;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the SalesOrderLine entity.
 */
@Repository
public interface SalesOrderLineRepository extends JpaRepository<SalesOrderLine, Long> {
    default Optional<SalesOrderLine> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<SalesOrderLine> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<SalesOrderLine> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select distinct salesOrderLine from SalesOrderLine salesOrderLine left join fetch salesOrderLine.product",
        countQuery = "select count(distinct salesOrderLine) from SalesOrderLine salesOrderLine"
    )
    Page<SalesOrderLine> findAllWithToOneRelationships(Pageable pageable);

    @Query("select distinct salesOrderLine from SalesOrderLine salesOrderLine left join fetch salesOrderLine.product")
    List<SalesOrderLine> findAllWithToOneRelationships();

    @Query("select salesOrderLine from SalesOrderLine salesOrderLine left join fetch salesOrderLine.product where salesOrderLine.id =:id")
    Optional<SalesOrderLine> findOneWithToOneRelationships(@Param("id") Long id);

    List<SalesOrderLine> findBySalesOrderId(Long SalesOrderId);

    // Lấy toàn bộ các dòng chi tiết đơn hàng ĐÃ HOÀN THÀNH của 1 sản phẩm tại 1 kho
    @Query(
        "SELECT sol FROM SalesOrderLine sol " +
        "JOIN FETCH sol.salesOrder so " +
        "WHERE sol.product.id = :productId " +
        "AND so.warehouse.id = :warehouseId " +
        "AND so.status = 'COMPLETED' " +
        "ORDER BY so.id DESC"
    ) // Sắp xếp giảm dần để lấy dữ liệu mới nhất
    List<SalesOrderLine> findCompletedSalesHistory(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId);
}
