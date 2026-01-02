package com.vsms.servicerequestservice.dto.response;

import com.vsms.servicerequestservice.enums.InvoiceStatus;
import com.vsms.servicerequestservice.enums.PaymentMethod;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {

    private Integer id;
    private String invoiceNumber;
    private Integer serviceRequestId;
    private Integer customerId;
    private BigDecimal laborCost;
    private BigDecimal partsCost;
    private BigDecimal totalAmount;
    private InvoiceStatus status;
    private PaymentMethod paymentMethod;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
