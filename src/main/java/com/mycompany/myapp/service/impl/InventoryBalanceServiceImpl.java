package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.InventoryBalance;
import com.mycompany.myapp.repository.InventoryBalanceRepository;
import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.InventoryBalanceService;
import com.mycompany.myapp.service.dto.InventoryBalanceDTO;
import com.mycompany.myapp.service.mapper.InventoryBalanceMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link InventoryBalance}.
 */
@Service
@Transactional
public class InventoryBalanceServiceImpl implements InventoryBalanceService {

    private final Logger log = LoggerFactory.getLogger(InventoryBalanceServiceImpl.class);

    private final InventoryBalanceRepository inventoryBalanceRepository;

    private final InventoryBalanceMapper inventoryBalanceMapper;

    public InventoryBalanceServiceImpl(
        InventoryBalanceRepository inventoryBalanceRepository,
        InventoryBalanceMapper inventoryBalanceMapper
    ) {
        this.inventoryBalanceRepository = inventoryBalanceRepository;
        this.inventoryBalanceMapper = inventoryBalanceMapper;
    }

    @Override
    public InventoryBalanceDTO save(InventoryBalanceDTO inventoryBalanceDTO) {
        log.debug("Request to save InventoryBalance : {}", inventoryBalanceDTO);
        InventoryBalance inventoryBalance = inventoryBalanceMapper.toEntity(inventoryBalanceDTO);
        inventoryBalance = inventoryBalanceRepository.save(inventoryBalance);
        return inventoryBalanceMapper.toDto(inventoryBalance);
    }

    @Override
    public InventoryBalanceDTO update(InventoryBalanceDTO inventoryBalanceDTO) {
        log.debug("Request to update InventoryBalance : {}", inventoryBalanceDTO);
        InventoryBalance inventoryBalance = inventoryBalanceMapper.toEntity(inventoryBalanceDTO);
        inventoryBalance = inventoryBalanceRepository.save(inventoryBalance);
        return inventoryBalanceMapper.toDto(inventoryBalance);
    }

    @Override
    public Optional<InventoryBalanceDTO> partialUpdate(InventoryBalanceDTO inventoryBalanceDTO) {
        log.debug("Request to partially update InventoryBalance : {}", inventoryBalanceDTO);

        return inventoryBalanceRepository
            .findById(inventoryBalanceDTO.getId())
            .map(existingInventoryBalance -> {
                inventoryBalanceMapper.partialUpdate(existingInventoryBalance, inventoryBalanceDTO);

                return existingInventoryBalance;
            })
            .map(inventoryBalanceRepository::save)
            .map(inventoryBalanceMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryBalanceDTO> findAll(Pageable pageable) {
        log.debug("Request to get all InventoryBalances with Data Filtering");
        return getFilteredInventoryBalances(pageable, false);
    }

    public Page<InventoryBalanceDTO> findAllWithEagerRelationships(Pageable pageable) {
        log.debug("Request to get all InventoryBalances with eager relationships and Data Filtering");
        return getFilteredInventoryBalances(pageable, true);
    }

    // Hàm private dùng chung để lọc dữ liệu 2 tầng
    private Page<InventoryBalanceDTO> getFilteredInventoryBalances(Pageable pageable, boolean eager) {
        boolean isAdmin = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
        boolean isManager = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER);

        // TẦNG 1: Admin hoặc Manager -> Xem tồn kho toàn bộ các chi nhánh
        if (isAdmin || isManager) {
            if (eager) {
                return inventoryBalanceRepository.findAllWithEagerRelationships(pageable).map(inventoryBalanceMapper::toDto);
            }
            return inventoryBalanceRepository.findAll(pageable).map(inventoryBalanceMapper::toDto);
        }

        // TẦNG 2: Thủ kho (Warehouse) -> Chỉ xem tồn kho thuộc chi nhánh mình (scopedWarehouse)
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Chưa đăng nhập!"));
        return inventoryBalanceRepository.findAllByEmployeeScopedWarehouse(currentUserLogin, pageable).map(inventoryBalanceMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InventoryBalanceDTO> findOne(Long id) {
        log.debug("Request to get InventoryBalance : {}", id);

        boolean isAdmin = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
        boolean isManager = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.MANAGER);

        // TẦNG 1: Admin hoặc Manager
        if (isAdmin || isManager) {
            return inventoryBalanceRepository.findOneWithEagerRelationships(id).map(inventoryBalanceMapper::toDto);
        }

        // TẦNG 2: Bắt buộc id truyền lên phải thuộc kho của User đó
        String currentUserLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Chưa đăng nhập!"));
        return inventoryBalanceRepository.findOneByIdAndUserLogin(id, currentUserLogin).map(inventoryBalanceMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete InventoryBalance : {}", id);

        inventoryBalanceRepository
            .findById(id)
            .ifPresent(inventoryBalance -> {
                // Chỉ cần gọi xóa, Hibernate sẽ tự động UPDATE is_active = false dựa theo version
                inventoryBalanceRepository.delete(inventoryBalance);
            });
    }
}
