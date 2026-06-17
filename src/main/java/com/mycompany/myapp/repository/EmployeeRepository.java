package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Employee;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Employee entity.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    default Optional<Employee> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Employee> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Employee> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        // Đã xóa: left join fetch employee.manager
        value = "select distinct employee from Employee employee left join fetch employee.user left join fetch employee.scopedWarehouse left join fetch employee.department",
        countQuery = "select count(distinct employee) from Employee employee"
    )
    Page<Employee> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        // Đã xóa: left join fetch employee.manager
        "select distinct employee from Employee employee left join fetch employee.user left join fetch employee.scopedWarehouse left join fetch employee.department"
    )
    List<Employee> findAllWithToOneRelationships();

    @Query(
        // Đã xóa: left join fetch employee.manager
        "select employee from Employee employee left join fetch employee.user left join fetch employee.scopedWarehouse left join fetch employee.department where employee.id =:id"
    )
    Optional<Employee> findOneWithToOneRelationships(@Param("id") Long id);

    Optional<Employee> findByUserLogin(String login);

    Optional<Employee> findByUserId(Long userId);

    //QUERY NÀY ĐỂ TÌM QUẢN LÝ CÙNG CHI NHÁNH & PHÒNG BAN
    @Query(
        "SELECT e FROM Employee e JOIN e.user u JOIN u.authorities a " +
        "WHERE e.scopedWarehouse.id = :warehouseId " +
        "AND e.department.id = :departmentId " +
        "AND a.name = :authority " +
        "AND e.isActive = true"
    )
    List<Employee> findManagersByBranchAndDepartment(
        @Param("warehouseId") Long warehouseId,
        @Param("departmentId") Long departmentId,
        @Param("authority") String authority
    );

    //QUERY NÀY ĐỂ TÌM TOÀN BỘ SHIPPER CỦA CHI NHÁNH
    @Query(
        "SELECT e FROM Employee e JOIN e.user u JOIN u.authorities a " +
        "WHERE e.scopedWarehouse.id = :warehouseId " +
        "AND a.name = :authority " +
        "AND e.isActive = true"
    )
    List<Employee> findShippersByBranch(@Param("warehouseId") Long warehouseId, @Param("authority") String authority);

    // QUERY NÀY: Lấy tất cả Kế toán toàn hệ thống (Không lọc theo chi nhánh)
    @Query("SELECT e FROM Employee e JOIN e.user u JOIN u.authorities a " + "WHERE a.name = :authority AND e.isActive = true")
    List<Employee> findAllAccountants(@Param("authority") String authority);

    // QUERY NÀY: Lấy tất cả ADMIN toàn hệ thống
    @Query("SELECT e FROM Employee e JOIN e.user u JOIN u.authorities a " + "WHERE a.name = :authority AND e.isActive = true")
    List<Employee> findAllAdmins(@Param("authority") String authority);

    // QUERY NÀY: Tìm toàn bộ nhân viên Kho của một chi nhánh
    @Query(
        "SELECT e FROM Employee e JOIN e.user u JOIN u.authorities a " +
        "WHERE e.scopedWarehouse.id = :warehouseId " +
        "AND a.name = :authority " +
        "AND e.isActive = true"
    )
    List<Employee> findWarehouseStaffByBranch(@Param("warehouseId") Long warehouseId, @Param("authority") String authority);

    // QUERY NÀY: Tìm Quản lý phòng Mua hàng của một chi nhánh cụ thể
    @Query(
        "SELECT e FROM Employee e JOIN e.user u JOIN u.authorities a " +
        "WHERE e.scopedWarehouse.id = :warehouseId " +
        "AND e.department.name = 'PURCHASE' " +
        "AND a.name = :authority AND e.isActive = true"
    )
    List<Employee> findPurchaseManagersByBranch(@Param("warehouseId") Long warehouseId, @Param("authority") String authority);
}
