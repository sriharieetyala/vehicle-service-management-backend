package com.vsms.servicerequestservice.repository;

import com.vsms.servicerequestservice.entity.ServiceRequest;
import com.vsms.servicerequestservice.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Integer> {

    List<ServiceRequest> findByCustomerId(Integer customerId);

    List<ServiceRequest> findByTechnicianId(Integer technicianId);

    List<ServiceRequest> findByStatus(RequestStatus status);

    List<ServiceRequest> findByTechnicianIdAndStatus(Integer technicianId, RequestStatus status);

    long countByStatus(RequestStatus status);

    // Bay tracking queries
    @Query("SELECT sr.bayNumber FROM ServiceRequest sr WHERE sr.status IN ('ASSIGNED', 'IN_PROGRESS')")
    List<Integer> findOccupiedBays();

    Optional<ServiceRequest> findByBayNumberAndStatusIn(Integer bayNumber, List<RequestStatus> statuses);
}
