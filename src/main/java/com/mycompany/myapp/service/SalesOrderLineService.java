package com.mycompany.myapp.service;

import com.mycompany.myapp.service.dto.SalesOrderLineDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.mycompany.myapp.domain.SalesOrderLine}.
 */
public interface SalesOrderLineService {
    /**
     * Save a salesOrderLine.
     *
     * @param salesOrderLineDTO the entity to save.
     * @return the persisted entity.
     */
    SalesOrderLineDTO save(SalesOrderLineDTO salesOrderLineDTO);

    /**
     * Updates a salesOrderLine.
     *
     * @param salesOrderLineDTO the entity to update.
     * @return the persisted entity.
     */
    SalesOrderLineDTO update(SalesOrderLineDTO salesOrderLineDTO);

    /**
     * Partially updates a salesOrderLine.
     *
     * @param salesOrderLineDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<SalesOrderLineDTO> partialUpdate(SalesOrderLineDTO salesOrderLineDTO);

    /**
     * Get all the salesOrderLines.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<SalesOrderLineDTO> findAll(Pageable pageable);

    /**
     * Get all the salesOrderLines with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<SalesOrderLineDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" salesOrderLine.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<SalesOrderLineDTO> findOne(Long id);

    /**
     * Delete the "id" salesOrderLine.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
