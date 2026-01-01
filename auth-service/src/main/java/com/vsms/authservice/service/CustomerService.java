package com.vsms.authservice.service;

import com.vsms.authservice.dto.request.CustomerCreateRequest;
import com.vsms.authservice.dto.request.CustomerUpdateRequest;
import com.vsms.authservice.dto.response.CustomerResponse;
import com.vsms.authservice.entity.AppUser;
import com.vsms.authservice.entity.Customer;
import com.vsms.authservice.enums.Role;
import com.vsms.authservice.enums.UserStatus;
import com.vsms.authservice.exception.DuplicateResourceException;
import com.vsms.authservice.exception.ResourceNotFoundException;
import com.vsms.authservice.repository.AppUserRepository;
import com.vsms.authservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AppUserRepository appUserRepository;
    private final NotificationPublisher notificationPublisher;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    /**
     * Create a new customer
     */
    public CustomerResponse createCustomer(CustomerCreateRequest request) {
        // Check if email already exists
        if (appUserRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Create AppUser first
        AppUser appUser = AppUser.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        // Create Customer
        Customer customer = Customer.builder()
                .user(appUser)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .build();

        Customer savedCustomer = customerRepository.save(customer);

        // Send welcome email
        try {
            notificationPublisher.publishCustomerWelcome(
                    request.getFirstName() + " " + request.getLastName(),
                    request.getEmail());
        } catch (Exception e) {
            System.err.println("Could not send notification: " + e.getMessage());
        }

        return mapToResponse(savedCustomer);
    }

    /**
     * Get customer by ID
     */
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Integer id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return mapToResponse(customer);
    }

    /**
     * Get all customers
     */
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update customer profile
     */
    public CustomerResponse updateCustomer(Integer id, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        // Update fields if provided
        if (request.getPhone() != null) {
            customer.getUser().setPhone(request.getPhone());
        }
        if (request.getFirstName() != null) {
            customer.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            customer.setLastName(request.getLastName());
        }
        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            customer.setCity(request.getCity());
        }
        if (request.getState() != null) {
            customer.setState(request.getState());
        }
        if (request.getZipCode() != null) {
            customer.setZipCode(request.getZipCode());
        }

        Customer updatedCustomer = customerRepository.save(customer);
        return mapToResponse(updatedCustomer);
    }

    /**
     * Deactivate customer account
     */
    public void deleteCustomer(Integer id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        customer.getUser().setStatus(UserStatus.INACTIVE);
        customerRepository.save(customer);
    }

    /**
     * Get customer count for dashboard
     */
    @Transactional(readOnly = true)
    public long getCustomerCount() {
        return customerRepository.count();
    }

    /**
     * Map entity to response DTO
     */
    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .userId(customer.getUser().getId())
                .email(customer.getUser().getEmail())
                .phone(customer.getUser().getPhone())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .address(customer.getAddress())
                .city(customer.getCity())
                .state(customer.getState())
                .zipCode(customer.getZipCode())
                .status(customer.getUser().getStatus())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
