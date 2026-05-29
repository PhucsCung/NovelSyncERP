package com.mycompany.myapp.service;

import com.mycompany.myapp.service.dto.PurchaseOrderLineDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.mycompany.myapp.domain.PurchaseOrderLine}.
 */
public interface PurchaseOrderLineService {
    /**
     * Save a purchaseOrderLine.
     *
     * @param purchaseOrderLineDTO the entity to save.
     * @return the persisted entity.
     */
    PurchaseOrderLineDTO save(PurchaseOrderLineDTO purchaseOrderLineDTO);

    /**
     * Updates a purchaseOrderLine.
     *
     * @param purchaseOrderLineDTO the entity to update.
     * @return the persisted entity.
     */
    PurchaseOrderLineDTO update(PurchaseOrderLineDTO purchaseOrderLineDTO);

    /**
     * Partially updates a purchaseOrderLine.
     *
     * @param purchaseOrderLineDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<PurchaseOrderLineDTO> partialUpdate(PurchaseOrderLineDTO purchaseOrderLineDTO);

    /**
     * Get all the purchaseOrderLines.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<PurchaseOrderLineDTO> findAll(Pageable pageable);

    /**
     * Get all the purchaseOrderLines with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<PurchaseOrderLineDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" purchaseOrderLine.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<PurchaseOrderLineDTO> findOne(Long id);

    /**
     * Delete the "id" purchaseOrderLine.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
