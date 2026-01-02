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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private NotificationPublisher notificationPublisher;
    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerService customerService;

    private Customer testCustomer;
    private AppUser testUser;

    @BeforeEach
    void setUp() {
        testUser = new AppUser();
        testUser.setId(1);
        testUser.setEmail("customer@test.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setPhone("1234567890");
        testUser.setRole(Role.CUSTOMER);
        testUser.setStatus(UserStatus.ACTIVE);

        testCustomer = new Customer();
        testCustomer.setId(1);
        testCustomer.setUser(testUser);
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Doe");
        testCustomer.setAddress("123 Main St");
        testCustomer.setCity("TestCity");
        testCustomer.setState("TS");
        testCustomer.setZipCode("12345");
        testCustomer.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createCustomer_Success() {
        CustomerCreateRequest request = new CustomerCreateRequest();
        request.setEmail("new@test.com");
        request.setPassword("password123");
        request.setPhone("1234567890");
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setAddress("456 Oak St");
        request.setCity("NewCity");
        request.setState("NC");
        request.setZipCode("67890");

        when(appUserRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            c.setId(2);
            c.setCreatedAt(LocalDateTime.now());
            return c;
        });
        doNothing().when(notificationPublisher).publishCustomerWelcome(anyString(), anyString());

        CustomerResponse response = customerService.createCustomer(request);

        assertNotNull(response);
        assertEquals("Jane", response.getFirstName());
        assertEquals("new@test.com", response.getEmail());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void createCustomer_DuplicateEmail_ThrowsException() {
        CustomerCreateRequest request = new CustomerCreateRequest();
        request.setEmail("existing@test.com");

        when(appUserRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> customerService.createCustomer(request));
    }

    @Test
    void getCustomerById_Success() {
        when(customerRepository.findById(1)).thenReturn(Optional.of(testCustomer));

        CustomerResponse response = customerService.getCustomerById(1);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("John", response.getFirstName());
    }

    @Test
    void getCustomerById_NotFound_ThrowsException() {
        when(customerRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerById(999));
    }

    @Test
    void getAllCustomers_Success() {
        when(customerRepository.findAll()).thenReturn(List.of(testCustomer));

        List<CustomerResponse> responses = customerService.getAllCustomers();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("John", responses.get(0).getFirstName());
    }

    @Test
    void updateCustomer_Success() {
        CustomerUpdateRequest request = new CustomerUpdateRequest();
        request.setFirstName("UpdatedName");
        request.setPhone("9876543210");

        when(customerRepository.findById(1)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        CustomerResponse response = customerService.updateCustomer(1, request);

        assertNotNull(response);
        verify(customerRepository, times(1)).save(testCustomer);
    }

    @Test
    void updateCustomer_NotFound_ThrowsException() {
        CustomerUpdateRequest request = new CustomerUpdateRequest();
        when(customerRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.updateCustomer(999, request));
    }

    @Test
    void deleteCustomer_Success() {
        when(customerRepository.findById(1)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        customerService.deleteCustomer(1);

        assertEquals(UserStatus.INACTIVE, testCustomer.getUser().getStatus());
        verify(customerRepository, times(1)).save(testCustomer);
    }

    @Test
    void deleteCustomer_NotFound_ThrowsException() {
        when(customerRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.deleteCustomer(999));
    }

    @Test
    void getCustomerCount_ReturnsCount() {
        when(customerRepository.count()).thenReturn(10L);

        long count = customerService.getCustomerCount();

        assertEquals(10L, count);
    }
}
