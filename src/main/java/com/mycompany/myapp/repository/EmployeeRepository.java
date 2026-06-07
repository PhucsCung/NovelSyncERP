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
        value = "select distinct employee from Employee employee left join fetch employee.user left join fetch employee.scopedWarehouse",
        countQuery = "select count(distinct employee) from Employee employee"
    )
    Page<Employee> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        // Đã xóa: left join fetch employee.manager
        "select distinct employee from Employee employee left join fetch employee.user left join fetch employee.scopedWarehouse"
    )
    List<Employee> findAllWithToOneRelationships();

    @Query(
        // Đã xóa: left join fetch employee.manager
        "select employee from Employee employee left join fetch employee.user left join fetch employee.scopedWarehouse where employee.id =:id"
    )
    Optional<Employee> findOneWithToOneRelationships(@Param("id") Long id);

    Optional<Employee> findByUserLogin(String login);
}
