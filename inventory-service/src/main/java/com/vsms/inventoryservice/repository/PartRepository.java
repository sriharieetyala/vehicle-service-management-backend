package com.vsms.inventoryservice.repository;

import com.vsms.inventoryservice.entity.Part;
import com.vsms.inventoryservice.enums.PartCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartRepository extends JpaRepository<Part, Integer> {

    Optional<Part> findByPartNumber(String partNumber);

    List<Part> findByCategory(PartCategory category);

    @Query("SELECT p FROM Part p WHERE p.quantity <= p.reorderLevel")
    List<Part> findLowStockParts();

    boolean existsByPartNumber(String partNumber);
}
