package com.vsms.servicerequestservice.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceCreatedResponse {
    private String invoiceNumber;
}
