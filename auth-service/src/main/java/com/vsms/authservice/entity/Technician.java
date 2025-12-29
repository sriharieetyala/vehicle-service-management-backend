package com.vsms.authservice.entity;

import com.vsms.authservice.enums.Specialization;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Technician entity - service personnel who perform vehicle repairs
 */
@Entity
@Table(name = "technicians", indexes = {
        @Index(name = "idx_tech_user", columnList = "user_id"),
        @Index(name = "idx_tech_spec", columnList = "specialization"),
        @Index(name = "idx_tech_duty", columnList = "on_duty"),
        @Index(name = "idx_tech_available", columnList = "on_duty, current_workload")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Technician {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "employee_id", unique = true, length = 50)
    private String employeeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private Specialization specialization;

    @Column(name = "experience_years")
    @Builder.Default
    private Integer experienceYears = 0;

    @Column(name = "on_duty")
    @Builder.Default
    private Boolean onDuty = true;

    @Column(name = "current_workload")
    @Builder.Default
    private Integer currentWorkload = 0;

    @Column(name = "max_capacity")
    @Builder.Default
    private Integer maxCapacity = 5;

    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
