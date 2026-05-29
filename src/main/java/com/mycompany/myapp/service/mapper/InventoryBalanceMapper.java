package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.InventoryBalance;
import com.mycompany.myapp.domain.Product;
import com.mycompany.myapp.domain.Warehouse;
import com.mycompany.myapp.service.dto.InventoryBalanceDTO;
import com.mycompany.myapp.service.dto.ProductDTO;
import com.mycompany.myapp.service.dto.WarehouseDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link InventoryBalance} and its DTO {@link InventoryBalanceDTO}.
 */
@Mapper(componentModel = "spring")
public interface InventoryBalanceMapper extends EntityMapper<InventoryBalanceDTO, InventoryBalance> {
    @Mapping(target = "product", source = "product", qualifiedByName = "productName")
    @Mapping(target = "warehouse", source = "warehouse", qualifiedByName = "warehouseName")
    InventoryBalanceDTO toDto(InventoryBalance s);

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
