package com.vsms.inventoryservice.dto.response;

import com.vsms.inventoryservice.enums.PartCategory;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartResponse {

    private Integer id;
    private String partNumber;
    private String name;
    private String description;
    private PartCategory category;
    private Integer quantity;
    private BigDecimal unitPrice;
    private Integer reorderLevel;
    private boolean lowStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
