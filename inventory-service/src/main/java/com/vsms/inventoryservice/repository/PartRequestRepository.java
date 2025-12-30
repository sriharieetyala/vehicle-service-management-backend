package com.vsms.inventoryservice.repository;

import com.vsms.inventoryservice.entity.PartRequest;
import com.vsms.inventoryservice.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartRequestRepository extends JpaRepository<PartRequest, Integer> {

    List<PartRequest> findByStatus(RequestStatus status);

    List<PartRequest> findByServiceRequestId(Integer serviceRequestId);

    List<PartRequest> findByTechnicianId(Integer technicianId);

    List<PartRequest> findByPartId(Integer partId);
}
