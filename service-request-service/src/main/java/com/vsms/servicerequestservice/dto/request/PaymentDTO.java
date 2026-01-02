package com.vsms.servicerequestservice.dto.request;

import com.vsms.servicerequestservice.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}
