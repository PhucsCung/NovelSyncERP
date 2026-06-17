package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.Department;
import com.mycompany.myapp.domain.Employee;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.Warehouse;
import com.mycompany.myapp.service.dto.DepartmentDTO;
import com.mycompany.myapp.service.dto.EmployeeDTO;
import com.mycompany.myapp.service.dto.UserDTO;
import com.mycompany.myapp.service.dto.WarehouseDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Employee} and its DTO {@link EmployeeDTO}.
 */
@Mapper(componentModel = "spring")
public interface EmployeeMapper extends EntityMapper<EmployeeDTO, Employee> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    //    @Mapping(target = "manager", source = "manager", qualifiedByName = "employeeFullName")
    @Mapping(target = "scopedWarehouse", source = "scopedWarehouse", qualifiedByName = "warehouseName")
    @Mapping(target = "department", source = "department", qualifiedByName = "departmentName")
    EmployeeDTO toDto(Employee s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    //    @Named("employeeFullName")
    //    @BeanMapping(ignoreByDefault = true)
    //    @Mapping(target = "id", source = "id")
    //    @Mapping(target = "fullName", source = "fullName")
    //    EmployeeDTO toDtoEmployeeFullName(Employee employee);

    @Named("warehouseName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    WarehouseDTO toDtoWarehouseName(Warehouse warehouse);

    @Named("departmentName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    DepartmentDTO toDtoDepartmentName(Department department);
}
