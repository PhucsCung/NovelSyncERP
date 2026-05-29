package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.SalesOrderLine;
import com.mycompany.myapp.repository.SalesOrderLineRepository;
import com.mycompany.myapp.service.SalesOrderLineService;
import com.mycompany.myapp.service.dto.SalesOrderLineDTO;
import com.mycompany.myapp.service.mapper.SalesOrderLineMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link SalesOrderLine}.
 */
@Service
@Transactional
public class SalesOrderLineServiceImpl implements SalesOrderLineService {

    private final Logger log = LoggerFactory.getLogger(SalesOrderLineServiceImpl.class);

    private final SalesOrderLineRepository salesOrderLineRepository;

    private final SalesOrderLineMapper salesOrderLineMapper;

    public SalesOrderLineServiceImpl(SalesOrderLineRepository salesOrderLineRepository, SalesOrderLineMapper salesOrderLineMapper) {
        this.salesOrderLineRepository = salesOrderLineRepository;
        this.salesOrderLineMapper = salesOrderLineMapper;
    }

    @Override
    public SalesOrderLineDTO save(SalesOrderLineDTO salesOrderLineDTO) {
        log.debug("Request to save SalesOrderLine : {}", salesOrderLineDTO);
        SalesOrderLine salesOrderLine = salesOrderLineMapper.toEntity(salesOrderLineDTO);
        salesOrderLine = salesOrderLineRepository.save(salesOrderLine);
        return salesOrderLineMapper.toDto(salesOrderLine);
    }

    @Override
    public SalesOrderLineDTO update(SalesOrderLineDTO salesOrderLineDTO) {
        log.debug("Request to update SalesOrderLine : {}", salesOrderLineDTO);
        SalesOrderLine salesOrderLine = salesOrderLineMapper.toEntity(salesOrderLineDTO);
        salesOrderLine = salesOrderLineRepository.save(salesOrderLine);
        return salesOrderLineMapper.toDto(salesOrderLine);
    }

    @Override
    public Optional<SalesOrderLineDTO> partialUpdate(SalesOrderLineDTO salesOrderLineDTO) {
        log.debug("Request to partially update SalesOrderLine : {}", salesOrderLineDTO);

        return salesOrderLineRepository
            .findById(salesOrderLineDTO.getId())
            .map(existingSalesOrderLine -> {
                salesOrderLineMapper.partialUpdate(existingSalesOrderLine, salesOrderLineDTO);

                return existingSalesOrderLine;
            })
            .map(salesOrderLineRepository::save)
            .map(salesOrderLineMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SalesOrderLineDTO> findAll(Pageable pageable) {
        log.debug("Request to get all SalesOrderLines");
        return salesOrderLineRepository.findAll(pageable).map(salesOrderLineMapper::toDto);
    }

    public Page<SalesOrderLineDTO> findAllWithEagerRelationships(Pageable pageable) {
        return salesOrderLineRepository.findAllWithEagerRelationships(pageable).map(salesOrderLineMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SalesOrderLineDTO> findOne(Long id) {
        log.debug("Request to get SalesOrderLine : {}", id);
        return salesOrderLineRepository.findOneWithEagerRelationships(id).map(salesOrderLineMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete SalesOrderLine : {}", id);
        salesOrderLineRepository.deleteById(id);
    }
}
