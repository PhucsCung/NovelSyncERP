package com.mycompany.myapp.service;

import com.mycompany.myapp.service.dto.WarehouseDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.mycompany.myapp.domain.Warehouse}.
 */
public interface WarehouseService {
    /**
     * Save a warehouse.
     *
     * @param warehouseDTO the entity to save.
     * @return the persisted entity.
     */
    WarehouseDTO save(WarehouseDTO warehouseDTO);

    /**
     * Updates a warehouse.
     *
     * @param warehouseDTO the entity to update.
     * @return the persisted entity.
     */
    WarehouseDTO update(WarehouseDTO warehouseDTO);

    /**
     * Partially updates a warehouse.
     *
     * @param warehouseDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<WarehouseDTO> partialUpdate(WarehouseDTO warehouseDTO);

    /**
     * Get all the warehouses.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<WarehouseDTO> findAll(Pageable pageable);

    /**
     * Get the "id" warehouse.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<WarehouseDTO> findOne(Long id);

    /**
     * Delete the "id" warehouse.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
