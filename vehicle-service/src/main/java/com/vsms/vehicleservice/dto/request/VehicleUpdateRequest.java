package com.vsms.vehicleservice.dto.request;

import com.vsms.vehicleservice.enums.FuelType;
import com.vsms.vehicleservice.enums.VehicleType;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleUpdateRequest {

    @Size(max = 50, message = "Brand must not exceed 50 characters")
    private String brand;

    @Size(max = 50, message = "Model must not exceed 50 characters")
    private String model;

    @Min(value = 1900, message = "Year must be valid")
    @Max(value = 2100, message = "Year must be valid")
    private Integer year;

    private FuelType fuelType;

    private VehicleType vehicleType;
}
