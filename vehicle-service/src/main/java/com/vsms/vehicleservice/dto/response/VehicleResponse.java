package com.vsms.vehicleservice.dto.response;

import com.vsms.vehicleservice.enums.FuelType;
import com.vsms.vehicleservice.enums.VehicleType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResponse {
    private Integer id;
    private Integer customerId;
    private String plateNumber;
    private String brand;
    private String model;
    private Integer year;
    private FuelType fuelType;
    private VehicleType vehicleType;
    private LocalDateTime createdAt;
}
