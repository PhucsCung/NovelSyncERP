package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.PurchaseOrderLine;
import com.mycompany.myapp.repository.PurchaseOrderLineRepository;
import com.mycompany.myapp.service.PurchaseOrderLineService;
import com.mycompany.myapp.service.dto.PurchaseOrderLineDTO;
import com.mycompany.myapp.service.mapper.PurchaseOrderLineMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link PurchaseOrderLine}.
 */
@Service
@Transactional
public class PurchaseOrderLineServiceImpl implements PurchaseOrderLineService {

    private final Logger log = LoggerFactory.getLogger(PurchaseOrderLineServiceImpl.class);

    private final PurchaseOrderLineRepository purchaseOrderLineRepository;

    private final PurchaseOrderLineMapper purchaseOrderLineMapper;

    public PurchaseOrderLineServiceImpl(
        PurchaseOrderLineRepository purchaseOrderLineRepository,
        PurchaseOrderLineMapper purchaseOrderLineMapper
    ) {
        this.purchaseOrderLineRepository = purchaseOrderLineRepository;
        this.purchaseOrderLineMapper = purchaseOrderLineMapper;
    }

    @Override
    public PurchaseOrderLineDTO save(PurchaseOrderLineDTO purchaseOrderLineDTO) {
        log.debug("Request to save PurchaseOrderLine : {}", purchaseOrderLineDTO);
        PurchaseOrderLine purchaseOrderLine = purchaseOrderLineMapper.toEntity(purchaseOrderLineDTO);
        purchaseOrderLine = purchaseOrderLineRepository.save(purchaseOrderLine);
        return purchaseOrderLineMapper.toDto(purchaseOrderLine);
    }

    @Override
    public PurchaseOrderLineDTO update(PurchaseOrderLineDTO purchaseOrderLineDTO) {
        log.debug("Request to update PurchaseOrderLine : {}", purchaseOrderLineDTO);
        PurchaseOrderLine purchaseOrderLine = purchaseOrderLineMapper.toEntity(purchaseOrderLineDTO);
        purchaseOrderLine = purchaseOrderLineRepository.save(purchaseOrderLine);
        return purchaseOrderLineMapper.toDto(purchaseOrderLine);
    }

    @Override
    public Optional<PurchaseOrderLineDTO> partialUpdate(PurchaseOrderLineDTO purchaseOrderLineDTO) {
        log.debug("Request to partially update PurchaseOrderLine : {}", purchaseOrderLineDTO);

        return purchaseOrderLineRepository
            .findById(purchaseOrderLineDTO.getId())
            .map(existingPurchaseOrderLine -> {
                purchaseOrderLineMapper.partialUpdate(existingPurchaseOrderLine, purchaseOrderLineDTO);

                return existingPurchaseOrderLine;
            })
            .map(purchaseOrderLineRepository::save)
            .map(purchaseOrderLineMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseOrderLineDTO> findAll(Pageable pageable) {
        log.debug("Request to get all PurchaseOrderLines");
        return purchaseOrderLineRepository.findAll(pageable).map(purchaseOrderLineMapper::toDto);
    }

    public Page<PurchaseOrderLineDTO> findAllWithEagerRelationships(Pageable pageable) {
        return purchaseOrderLineRepository.findAllWithEagerRelationships(pageable).map(purchaseOrderLineMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PurchaseOrderLineDTO> findOne(Long id) {
        log.debug("Request to get PurchaseOrderLine : {}", id);
        return purchaseOrderLineRepository.findOneWithEagerRelationships(id).map(purchaseOrderLineMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete PurchaseOrderLine : {}", id);
        purchaseOrderLineRepository.deleteById(id);
    }
}
