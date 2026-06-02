package com.mycompany.myapp.service;

import com.mycompany.myapp.service.dto.SalesOrderDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.mycompany.myapp.domain.SalesOrder}.
 */
public interface SalesOrderService {
    /**
     * Save a salesOrder.
     *
     * @param salesOrderDTO the entity to save.
     * @return the persisted entity.
     */
    SalesOrderDTO save(SalesOrderDTO salesOrderDTO);

    /**
     * Updates a salesOrder.
     *
     * @param salesOrderDTO the entity to update.
     * @return the persisted entity.
     */
    SalesOrderDTO update(SalesOrderDTO salesOrderDTO);

    /**
     * Partially updates a salesOrder.
     *
     * @param salesOrderDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<SalesOrderDTO> partialUpdate(SalesOrderDTO salesOrderDTO);

    /**
     * Get all the salesOrders.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<SalesOrderDTO> findAll(Pageable pageable);

    /**
     * Get all the salesOrders with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<SalesOrderDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" salesOrder.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<SalesOrderDTO> findOne(Long id);

    /**
     * Delete the "id" salesOrder.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Approve the "id" salesOrder.
     *
     * @param SalesOrderid the id of the entity.
     */
    SalesOrderDTO approveOrder(Long SalesOrderid);

    SalesOrderDTO completeOrder(Long id);

    SalesOrderDTO cancelOrder(Long id);

    // THÊM HÀM CHO SHIPPER
    SalesOrderDTO startDelivery(Long id); // Chuyển sang PROCESSING
    SalesOrderDTO confirmDelivery(Long id); // Báo cáo Kế toán
}
