package com.vsms.authservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Customer entity - vehicle owners who book services
 */
@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customers_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

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

    @Column(length = 500)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(name = "zip_code", length = 20)
    private String zipCode;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
