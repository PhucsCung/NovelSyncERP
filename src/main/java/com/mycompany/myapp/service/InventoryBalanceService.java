package com.mycompany.myapp.service;

import com.mycompany.myapp.service.dto.InventoryBalanceDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.mycompany.myapp.domain.InventoryBalance}.
 */
public interface InventoryBalanceService {
    /**
     * Save a inventoryBalance.
     *
     * @param inventoryBalanceDTO the entity to save.
     * @return the persisted entity.
     */
    InventoryBalanceDTO save(InventoryBalanceDTO inventoryBalanceDTO);

    /**
     * Updates a inventoryBalance.
     *
     * @param inventoryBalanceDTO the entity to update.
     * @return the persisted entity.
     */
    InventoryBalanceDTO update(InventoryBalanceDTO inventoryBalanceDTO);

    /**
     * Partially updates a inventoryBalance.
     *
     * @param inventoryBalanceDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<InventoryBalanceDTO> partialUpdate(InventoryBalanceDTO inventoryBalanceDTO);

    /**
     * Get all the inventoryBalances.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<InventoryBalanceDTO> findAll(Pageable pageable);

    /**
     * Get all the inventoryBalances with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<InventoryBalanceDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" inventoryBalance.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<InventoryBalanceDTO> findOne(Long id);

    /**
     * Delete the "id" inventoryBalance.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
