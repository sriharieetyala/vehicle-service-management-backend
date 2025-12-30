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
public class PartCreateDTO {

    @NotBlank(message = "Part number is required")
    @Size(max = 50, message = "Part number must be at most 50 characters")
    private String partNumber;

    @NotBlank(message = "Part name is required")
    @Size(max = 100, message = "Part name must be at most 100 characters")
    private String name;

    private String description;

    @NotNull(message = "Category is required")
    private PartCategory category;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
    private BigDecimal unitPrice;

    @NotNull(message = "Reorder level is required")
    @Min(value = 1, message = "Reorder level must be at least 1")
    private Integer reorderLevel;
}
