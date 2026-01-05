package com.vsms.servicerequestservice.dto.event;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoicePaidEvent {
    private String invoiceNumber;
    private String customerName;
    private String managerEmail;
    private BigDecimal amount;
    private String paymentMethod;
}
