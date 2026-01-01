package com.vsms.authservice.repository;

import com.vsms.authservice.entity.Technician;
import com.vsms.authservice.enums.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TechnicianRepository extends JpaRepository<Technician, Integer> {

        Optional<Technician> findByUserId(Integer userId);

        Optional<Technician> findByUserEmail(String email);

        Optional<Technician> findByEmployeeId(String employeeId);

        // Find available technicians (on duty + has capacity)
        @Query("SELECT t FROM Technician t WHERE t.onDuty = true " +
                        "AND t.currentWorkload < t.maxCapacity " +
                        "AND t.user.status = 'ACTIVE'")
        List<Technician> findAvailableTechnicians();

        // Find by specialization (for assignment matching)
        @Query("SELECT t FROM Technician t WHERE t.specialization = :spec " +
                        "AND t.onDuty = true AND t.currentWorkload < t.maxCapacity " +
                        "AND t.user.status = 'ACTIVE' " +
                        "ORDER BY t.currentWorkload ASC")
        List<Technician> findAvailableBySpecialization(@Param("spec") Specialization specialization);

        // Find pending technicians awaiting approval
        @Query("SELECT t FROM Technician t WHERE t.user.status = 'PENDING'")
        List<Technician> findPendingApproval();

        // Find all ACTIVE (approved) technicians - excludes PENDING and INACTIVE
        @Query("SELECT t FROM Technician t WHERE t.user.status = 'ACTIVE'")
        List<Technician> findAllActive();

        // Increment workload
        @Modifying
        @Query("UPDATE Technician t SET t.currentWorkload = t.currentWorkload + 1 WHERE t.id = :id")
        void incrementWorkload(@Param("id") Integer id);

        // Decrement workload
        @Modifying
        @Query("UPDATE Technician t SET t.currentWorkload = t.currentWorkload - 1 WHERE t.id = :id AND t.currentWorkload > 0")
        void decrementWorkload(@Param("id") Integer id);

        // Count only ACTIVE technicians (approved and part of the team)
        @Query("SELECT COUNT(t) FROM Technician t WHERE t.user.status = 'ACTIVE'")
        long countActive();

        @Query("SELECT COUNT(t) FROM Technician t WHERE t.user.status = 'PENDING'")
        long countPending();
}
