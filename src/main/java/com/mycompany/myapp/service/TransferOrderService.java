package com.mycompany.myapp.service;

import com.mycompany.myapp.service.dto.TransferOrderDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.mycompany.myapp.domain.TransferOrder}.
 */
public interface TransferOrderService {
    /**
     * Save a transferOrder.
     *
     * @param transferOrderDTO the entity to save.
     * @return the persisted entity.
     */
    TransferOrderDTO save(TransferOrderDTO transferOrderDTO);

    /**
     * Updates a transferOrder.
     *
     * @param transferOrderDTO the entity to update.
     * @return the persisted entity.
     */
    TransferOrderDTO update(TransferOrderDTO transferOrderDTO);

    /**
     * Partially updates a transferOrder.
     *
     * @param transferOrderDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<TransferOrderDTO> partialUpdate(TransferOrderDTO transferOrderDTO);

    /**
     * Get all the transferOrders.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<TransferOrderDTO> findAll(Pageable pageable);

    /**
     * Get all the transferOrders with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<TransferOrderDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" transferOrder.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<TransferOrderDTO> findOne(Long id);

    /**
     * Delete the "id" transferOrder.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    TransferOrderDTO approveOrder(Long id);

    TransferOrderDTO cancelOrder(Long id);

    TransferOrderDTO startDelivery(Long id); // Shipper bắt đầu chở
    TransferOrderDTO confirmDelivery(Long id); // Shipper báo tới nơi
    TransferOrderDTO completeOrder(Long id); // Quản lý Kho Nhập bấm nhận hàng
}
