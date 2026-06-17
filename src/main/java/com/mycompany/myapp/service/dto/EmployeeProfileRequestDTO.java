package com.mycompany.myapp.service.dto;

import java.io.Serializable;
import javax.validation.constraints.Size;

public class EmployeeProfileRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Size(max = 255)
    private String fullName;

    @Size(max = 20)
    private String phone;

    private Long departmentId;

    private Long scopedWarehouseId;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getScopedWarehouseId() {
        return scopedWarehouseId;
    }

    public void setScopedWarehouseId(Long scopedWarehouseId) {
        this.scopedWarehouseId = scopedWarehouseId;
    }
}
