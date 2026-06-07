package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.InventoryBalance;
import com.mycompany.myapp.domain.Product;
import com.mycompany.myapp.domain.Warehouse;
import com.mycompany.myapp.repository.InventoryBalanceRepository;
import com.mycompany.myapp.repository.ProductRepository;
import com.mycompany.myapp.repository.WarehouseRepository;
import com.mycompany.myapp.service.ProductService;
import com.mycompany.myapp.service.dto.ProductDTO;
import com.mycompany.myapp.service.mapper.ProductMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Product}.
 */
@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;

    private final ProductMapper productMapper;
    private final WarehouseRepository warehouseRepository;
    private final InventoryBalanceRepository inventoryBalanceRepository;

    public ProductServiceImpl(
        ProductRepository productRepository,
        ProductMapper productMapper,
        WarehouseRepository warehouseRepository,
        InventoryBalanceRepository inventoryBalanceRepository
    ) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.warehouseRepository = warehouseRepository;
        this.inventoryBalanceRepository = inventoryBalanceRepository;
    }

    @Override
    @Transactional
    public ProductDTO save(ProductDTO productDTO) {
        log.debug("Request to save Product : {}", productDTO);

        Product product = productMapper.toEntity(productDTO);
        product = productRepository.save(product);

        List<Warehouse> allWarehouses = warehouseRepository.findAll();
        List<InventoryBalance> initialBalances = new ArrayList<>();

        for (Warehouse warehouse : allWarehouses) {
            InventoryBalance balance = new InventoryBalance();
            balance.setProduct(product); // Trỏ vào Sản phẩm vừa tạo
            balance.setWarehouse(warehouse); // Trỏ vào từng Kho
            balance.setQuantity(0); // Số lượng khởi điểm: 0

            initialBalances.add(balance);
        }

        if (!initialBalances.isEmpty()) {
            inventoryBalanceRepository.saveAll(initialBalances);
        }

        return productMapper.toDto(product);
    }

    @Override
    public ProductDTO update(ProductDTO productDTO) {
        log.debug("Request to update Product : {}", productDTO);
        Product product = productMapper.toEntity(productDTO);
        product = productRepository.save(product);
        return productMapper.toDto(product);
    }

    @Override
    public Optional<ProductDTO> partialUpdate(ProductDTO productDTO) {
        log.debug("Request to partially update Product : {}", productDTO);

        return productRepository
            .findById(productDTO.getId())
            .map(existingProduct -> {
                productMapper.partialUpdate(existingProduct, productDTO);

                return existingProduct;
            })
            .map(productRepository::save)
            .map(productMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Products");
        return productRepository.findAll(pageable).map(productMapper::toDto);
    }

    public Page<ProductDTO> findAllWithEagerRelationships(Pageable pageable) {
        return productRepository.findAllWithEagerRelationships(pageable).map(productMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductDTO> findOne(Long id) {
        log.debug("Request to get Product : {}", id);
        return productRepository.findOneWithEagerRelationships(id).map(productMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Product : {}", id);

        productRepository
            .findById(id)
            .ifPresent(product -> {
                // Chú ý: Product dùng SKU thay vì CODE
                String deletedSku = product.getSku() + "_DELETED_" + java.time.Instant.now().toEpochMilli();
                product.setSku(deletedSku);

                productRepository.saveAndFlush(product);
                productRepository.delete(product);
            });
    }
}
