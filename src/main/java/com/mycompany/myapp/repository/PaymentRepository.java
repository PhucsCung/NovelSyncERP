package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Payment;
import com.mycompany.myapp.domain.enumeration.PaymentType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Payment entity.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    default Optional<Payment> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Payment> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Payment> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select distinct payment from Payment payment left join fetch payment.customer left join fetch payment.supplier",
        countQuery = "select count(distinct payment) from Payment payment"
    )
    Page<Payment> findAllWithToOneRelationships(Pageable pageable);

    @Query("select distinct payment from Payment payment left join fetch payment.customer left join fetch payment.supplier")
    List<Payment> findAllWithToOneRelationships();

    @Query("select payment from Payment payment left join fetch payment.customer left join fetch payment.supplier where payment.id =:id")
    Optional<Payment> findOneWithToOneRelationships(@Param("id") Long id);

    Optional<Payment> findOneByPaymentCode(String paymentCode);
}
