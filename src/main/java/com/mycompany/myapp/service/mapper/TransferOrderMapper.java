package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.TransferOrder;
import com.mycompany.myapp.domain.Warehouse;
import com.mycompany.myapp.service.dto.TransferOrderDTO;
import com.mycompany.myapp.service.dto.WarehouseDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link TransferOrder} and its DTO {@link TransferOrderDTO}.
 */
@Mapper(componentModel = "spring", uses = { TransferOrderLineMapper.class })
public interface TransferOrderMapper extends EntityMapper<TransferOrderDTO, TransferOrder> {
    @Mapping(target = "fromWarehouse", source = "fromWarehouse", qualifiedByName = "warehouseName")
    @Mapping(target = "toWarehouse", source = "toWarehouse", qualifiedByName = "warehouseName")
    @Mapping(target = "transferOrderLines", source = "orderLines")
    TransferOrderDTO toDto(TransferOrder s);

    @Named("warehouseName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    WarehouseDTO toDtoWarehouseName(Warehouse warehouse);
}
