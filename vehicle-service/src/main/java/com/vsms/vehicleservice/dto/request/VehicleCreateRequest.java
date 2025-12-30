package com.vsms.vehicleservice.dto.request;

import com.vsms.vehicleservice.enums.FuelType;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleCreateRequest {

    @NotNull(message = "Customer ID is required")
    private Integer customerId;

    @NotBlank(message = "Plate number is required")
    @Size(max = 20, message = "Plate number must not exceed 20 characters")
    private String plateNumber;

    @NotBlank(message = "Brand is required")
    @Size(max = 50, message = "Brand must not exceed 50 characters")
    private String brand;

    @NotBlank(message = "Model is required")
    @Size(max = 50, message = "Model must not exceed 50 characters")
    private String model;

    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be valid")
    @Max(value = 2100, message = "Year must be valid")
    private Integer year;

    @NotNull(message = "Fuel type is required")
    private FuelType fuelType;
}
