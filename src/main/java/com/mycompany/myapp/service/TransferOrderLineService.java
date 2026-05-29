package com.mycompany.myapp.service;

import com.mycompany.myapp.service.dto.TransferOrderLineDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.mycompany.myapp.domain.TransferOrderLine}.
 */
public interface TransferOrderLineService {
    /**
     * Save a transferOrderLine.
     *
     * @param transferOrderLineDTO the entity to save.
     * @return the persisted entity.
     */
    TransferOrderLineDTO save(TransferOrderLineDTO transferOrderLineDTO);

    /**
     * Updates a transferOrderLine.
     *
     * @param transferOrderLineDTO the entity to update.
     * @return the persisted entity.
     */
    TransferOrderLineDTO update(TransferOrderLineDTO transferOrderLineDTO);

    /**
     * Partially updates a transferOrderLine.
     *
     * @param transferOrderLineDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<TransferOrderLineDTO> partialUpdate(TransferOrderLineDTO transferOrderLineDTO);

    /**
     * Get all the transferOrderLines.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<TransferOrderLineDTO> findAll(Pageable pageable);

    /**
     * Get all the transferOrderLines with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<TransferOrderLineDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" transferOrderLine.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<TransferOrderLineDTO> findOne(Long id);

    /**
     * Delete the "id" transferOrderLine.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
