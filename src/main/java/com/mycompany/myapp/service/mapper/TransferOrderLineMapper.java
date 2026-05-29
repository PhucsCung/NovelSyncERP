package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.Product;
import com.mycompany.myapp.domain.TransferOrder;
import com.mycompany.myapp.domain.TransferOrderLine;
import com.mycompany.myapp.service.dto.ProductDTO;
import com.mycompany.myapp.service.dto.TransferOrderDTO;
import com.mycompany.myapp.service.dto.TransferOrderLineDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link TransferOrderLine} and its DTO {@link TransferOrderLineDTO}.
 */
@Mapper(componentModel = "spring")
public interface TransferOrderLineMapper extends EntityMapper<TransferOrderLineDTO, TransferOrderLine> {
    @Mapping(target = "product", source = "product", qualifiedByName = "productName")
    @Mapping(target = "transferOrder", source = "transferOrder", qualifiedByName = "transferOrderId")
    TransferOrderLineDTO toDto(TransferOrderLine s);

    @Named("productName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    ProductDTO toDtoProductName(Product product);

    @Named("transferOrderId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    TransferOrderDTO toDtoTransferOrderId(TransferOrder transferOrder);
}
