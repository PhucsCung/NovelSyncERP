package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.Product;
import com.mycompany.myapp.domain.PurchaseOrder;
import com.mycompany.myapp.domain.PurchaseOrderLine;
import com.mycompany.myapp.service.dto.ProductDTO;
import com.mycompany.myapp.service.dto.PurchaseOrderDTO;
import com.mycompany.myapp.service.dto.PurchaseOrderLineDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link PurchaseOrderLine} and its DTO {@link PurchaseOrderLineDTO}.
 */
@Mapper(componentModel = "spring")
public interface PurchaseOrderLineMapper extends EntityMapper<PurchaseOrderLineDTO, PurchaseOrderLine> {
    @Mapping(target = "product", source = "product", qualifiedByName = "productName")
    @Mapping(target = "purchaseOrder", source = "purchaseOrder", qualifiedByName = "purchaseOrderId")
    PurchaseOrderLineDTO toDto(PurchaseOrderLine s);

    @Named("productName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    ProductDTO toDtoProductName(Product product);

    @Named("purchaseOrderId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    PurchaseOrderDTO toDtoPurchaseOrderId(PurchaseOrder purchaseOrder);
}
