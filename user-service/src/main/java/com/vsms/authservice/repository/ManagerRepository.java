package com.vsms.authservice.repository;

import com.vsms.authservice.entity.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Integer> {

    Optional<Manager> findByUserId(Integer userId);

    Optional<Manager> findByUserEmail(String email);

    Optional<Manager> findByEmployeeId(String employeeId);

    boolean existsByEmployeeId(String employeeId);

    long count();
}
