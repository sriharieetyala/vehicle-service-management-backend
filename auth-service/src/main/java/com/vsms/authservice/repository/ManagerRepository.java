package com.vsms.authservice.repository;

import com.vsms.authservice.entity.Manager;
import com.vsms.authservice.enums.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Integer> {

    Optional<Manager> findByUserId(Integer userId);

    Optional<Manager> findByUserEmail(String email);

    Optional<Manager> findByEmployeeId(String employeeId);

    boolean existsByEmployeeId(String employeeId);

    // Find managers by department (SERVICE_BAY or INVENTORY)
    List<Manager> findByDepartment(Department department);

    long count();

    // Count by department
    long countByDepartment(Department department);
}
