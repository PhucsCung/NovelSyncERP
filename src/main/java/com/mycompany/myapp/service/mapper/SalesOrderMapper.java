package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.Customer;
import com.mycompany.myapp.domain.Employee;
import com.mycompany.myapp.domain.SalesOrder;
import com.mycompany.myapp.domain.Warehouse;
import com.mycompany.myapp.service.dto.CustomerDTO;
import com.mycompany.myapp.service.dto.EmployeeDTO;
import com.mycompany.myapp.service.dto.SalesOrderDTO;
import com.mycompany.myapp.service.dto.WarehouseDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link SalesOrder} and its DTO {@link SalesOrderDTO}.
 */
@Mapper(componentModel = "spring")
public interface SalesOrderMapper extends EntityMapper<SalesOrderDTO, SalesOrder> {
    @Mapping(target = "customer", source = "customer", qualifiedByName = "customerName")
    @Mapping(target = "employee", source = "employee", qualifiedByName = "employeeFullName")
    @Mapping(target = "warehouse", source = "warehouse", qualifiedByName = "warehouseName")
    SalesOrderDTO toDto(SalesOrder s);

    @Named("customerName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    CustomerDTO toDtoCustomerName(Customer customer);

    @Named("employeeFullName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "fullName", source = "fullName")
    EmployeeDTO toDtoEmployeeFullName(Employee employee);

    @Named("warehouseName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    WarehouseDTO toDtoWarehouseName(Warehouse warehouse);
}
