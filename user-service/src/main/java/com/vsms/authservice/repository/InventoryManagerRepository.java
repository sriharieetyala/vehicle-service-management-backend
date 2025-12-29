package com.vsms.authservice.repository;

import com.vsms.authservice.entity.InventoryManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryManagerRepository extends JpaRepository<InventoryManager, Integer> {

    Optional<InventoryManager> findByUserId(Integer userId);

    Optional<InventoryManager> findByUserEmail(String email);

    Optional<InventoryManager> findByEmployeeId(String employeeId);

    boolean existsByEmployeeId(String employeeId);

    long count();
}
