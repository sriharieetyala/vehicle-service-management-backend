package com.vsms.billingservice.repository;

import com.vsms.billingservice.entity.Invoice;
import com.vsms.billingservice.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

    Optional<Invoice> findByServiceRequestId(Integer serviceRequestId);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByCustomerId(Integer customerId);

    List<Invoice> findByStatus(InvoiceStatus status);

    boolean existsByServiceRequestId(Integer serviceRequestId);
}
