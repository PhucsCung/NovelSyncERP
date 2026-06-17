package com.mycompany.myapp.service.dto;

import java.io.Serializable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class AdminUserWithEmployeeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Valid
    @NotNull
    private AdminUserDTO user;

    @Valid
    private EmployeeProfileRequestDTO employee;

    public AdminUserDTO getUser() {
        return user;
    }

    public void setUser(AdminUserDTO user) {
        this.user = user;
    }

    public EmployeeProfileRequestDTO getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeProfileRequestDTO employee) {
        this.employee = employee;
    }
}
