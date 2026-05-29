package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.Product;
import com.mycompany.myapp.domain.SalesOrder;
import com.mycompany.myapp.domain.SalesOrderLine;
import com.mycompany.myapp.service.dto.ProductDTO;
import com.mycompany.myapp.service.dto.SalesOrderDTO;
import com.mycompany.myapp.service.dto.SalesOrderLineDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link SalesOrderLine} and its DTO {@link SalesOrderLineDTO}.
 */
@Mapper(componentModel = "spring")
public interface SalesOrderLineMapper extends EntityMapper<SalesOrderLineDTO, SalesOrderLine> {
    @Mapping(target = "product", source = "product", qualifiedByName = "productName")
    @Mapping(target = "salesOrder", source = "salesOrder", qualifiedByName = "salesOrderId")
    SalesOrderLineDTO toDto(SalesOrderLine s);

    @Named("productName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    ProductDTO toDtoProductName(Product product);

    @Named("salesOrderId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    SalesOrderDTO toDtoSalesOrderId(SalesOrder salesOrder);
}
