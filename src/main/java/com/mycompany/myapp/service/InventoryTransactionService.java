package com.mycompany.myapp.service;

import com.mycompany.myapp.service.dto.InventoryTransactionDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.mycompany.myapp.domain.InventoryTransaction}.
 */
public interface InventoryTransactionService {
    /**
     * Save a inventoryTransaction.
     *
     * @param inventoryTransactionDTO the entity to save.
     * @return the persisted entity.
     */
    InventoryTransactionDTO save(InventoryTransactionDTO inventoryTransactionDTO);

    /**
     * Updates a inventoryTransaction.
     *
     * @param inventoryTransactionDTO the entity to update.
     * @return the persisted entity.
     */
    InventoryTransactionDTO update(InventoryTransactionDTO inventoryTransactionDTO);

    /**
     * Partially updates a inventoryTransaction.
     *
     * @param inventoryTransactionDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<InventoryTransactionDTO> partialUpdate(InventoryTransactionDTO inventoryTransactionDTO);

    /**
     * Get all the inventoryTransactions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<InventoryTransactionDTO> findAll(Pageable pageable);

    /**
     * Get all the inventoryTransactions with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<InventoryTransactionDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" inventoryTransaction.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<InventoryTransactionDTO> findOne(Long id);

    /**
     * Delete the "id" inventoryTransaction.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
