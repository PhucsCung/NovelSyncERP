package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.TransferOrderLine;
import com.mycompany.myapp.repository.TransferOrderLineRepository;
import com.mycompany.myapp.service.TransferOrderLineService;
import com.mycompany.myapp.service.dto.TransferOrderLineDTO;
import com.mycompany.myapp.service.mapper.TransferOrderLineMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link TransferOrderLine}.
 */
@Service
@Transactional
public class TransferOrderLineServiceImpl implements TransferOrderLineService {

    private final Logger log = LoggerFactory.getLogger(TransferOrderLineServiceImpl.class);

    private final TransferOrderLineRepository transferOrderLineRepository;

    private final TransferOrderLineMapper transferOrderLineMapper;

    public TransferOrderLineServiceImpl(
        TransferOrderLineRepository transferOrderLineRepository,
        TransferOrderLineMapper transferOrderLineMapper
    ) {
        this.transferOrderLineRepository = transferOrderLineRepository;
        this.transferOrderLineMapper = transferOrderLineMapper;
    }

    @Override
    public TransferOrderLineDTO save(TransferOrderLineDTO transferOrderLineDTO) {
        log.debug("Request to save TransferOrderLine : {}", transferOrderLineDTO);
        TransferOrderLine transferOrderLine = transferOrderLineMapper.toEntity(transferOrderLineDTO);
        transferOrderLine = transferOrderLineRepository.save(transferOrderLine);
        return transferOrderLineMapper.toDto(transferOrderLine);
    }

    @Override
    public TransferOrderLineDTO update(TransferOrderLineDTO transferOrderLineDTO) {
        log.debug("Request to update TransferOrderLine : {}", transferOrderLineDTO);
        TransferOrderLine transferOrderLine = transferOrderLineMapper.toEntity(transferOrderLineDTO);
        transferOrderLine = transferOrderLineRepository.save(transferOrderLine);
        return transferOrderLineMapper.toDto(transferOrderLine);
    }

    @Override
    public Optional<TransferOrderLineDTO> partialUpdate(TransferOrderLineDTO transferOrderLineDTO) {
        log.debug("Request to partially update TransferOrderLine : {}", transferOrderLineDTO);

        return transferOrderLineRepository
            .findById(transferOrderLineDTO.getId())
            .map(existingTransferOrderLine -> {
                transferOrderLineMapper.partialUpdate(existingTransferOrderLine, transferOrderLineDTO);

                return existingTransferOrderLine;
            })
            .map(transferOrderLineRepository::save)
            .map(transferOrderLineMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransferOrderLineDTO> findAll(Pageable pageable) {
        log.debug("Request to get all TransferOrderLines");
        return transferOrderLineRepository.findAll(pageable).map(transferOrderLineMapper::toDto);
    }

    public Page<TransferOrderLineDTO> findAllWithEagerRelationships(Pageable pageable) {
        return transferOrderLineRepository.findAllWithEagerRelationships(pageable).map(transferOrderLineMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TransferOrderLineDTO> findOne(Long id) {
        log.debug("Request to get TransferOrderLine : {}", id);
        return transferOrderLineRepository.findOneWithEagerRelationships(id).map(transferOrderLineMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete TransferOrderLine : {}", id);
        transferOrderLineRepository.deleteById(id);
    }
}
