package com.vsms.vehicleservice.repository;

import com.vsms.vehicleservice.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {

    // Find all vehicles by customer ID
    List<Vehicle> findByCustomerId(Integer customerId);

    // Find vehicle by plate number
    Optional<Vehicle> findByPlateNumber(String plateNumber);

    // Check if plate number exists
    boolean existsByPlateNumber(String plateNumber);

    // Count vehicles by customer
    long countByCustomerId(Integer customerId);
}
