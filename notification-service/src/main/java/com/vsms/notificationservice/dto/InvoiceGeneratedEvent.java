package com.vsms.notificationservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceGeneratedEvent {
    private String invoiceNumber;
    private String customerName;
    private String customerEmail;
    private BigDecimal totalAmount;
    private String serviceName;
}
