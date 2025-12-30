package com.vsms.inventoryservice.dto.request;

import com.vsms.inventoryservice.enums.PartCategory;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartUpdateDTO {

    @Size(max = 100, message = "Part name must be at most 100 characters")
    private String name;

    private String description;

    private PartCategory category;

    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
    private BigDecimal unitPrice;

    @Min(value = 1, message = "Reorder level must be at least 1")
    private Integer reorderLevel;
}
