package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.InventoryTransaction;
import com.mycompany.myapp.domain.Product;
import com.mycompany.myapp.domain.Warehouse;
import com.mycompany.myapp.service.dto.InventoryTransactionDTO;
import com.mycompany.myapp.service.dto.ProductDTO;
import com.mycompany.myapp.service.dto.WarehouseDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link InventoryTransaction} and its DTO {@link InventoryTransactionDTO}.
 */
@Mapper(componentModel = "spring")
public interface InventoryTransactionMapper extends EntityMapper<InventoryTransactionDTO, InventoryTransaction> {
    @Mapping(target = "product", source = "product", qualifiedByName = "productName")
    @Mapping(target = "warehouse", source = "warehouse", qualifiedByName = "warehouseName")
    InventoryTransactionDTO toDto(InventoryTransaction s);

    @Named("productName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    ProductDTO toDtoProductName(Product product);

    @Named("warehouseName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    WarehouseDTO toDtoWarehouseName(Warehouse warehouse);
}
