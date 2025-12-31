package com.vsms.billingservice.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueStats {
    private int totalInvoices;
    private int paidInvoices;
    private int unpaidInvoices;
    private BigDecimal totalRevenue;
    private BigDecimal collectedRevenue;
    private BigDecimal pendingRevenue;
}
