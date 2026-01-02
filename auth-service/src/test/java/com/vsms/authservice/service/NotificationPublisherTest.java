package com.vsms.authservice.service;

import com.vsms.authservice.config.RabbitMQConfig;
import com.vsms.authservice.dto.event.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private NotificationPublisher notificationPublisher;

    @Test
    void publishManagerCreated_Success() {
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(ManagerCreatedEvent.class));

        notificationPublisher.publishManagerCreated("John Doe", "john@test.com", "john@test.com", "tempPass123");

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitMQConfig.MANAGER_CREATED_KEY),
                any(ManagerCreatedEvent.class));
    }

    @Test
    void publishTechnicianApproved_Success() {
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(TechnicianApprovedEvent.class));

        notificationPublisher.publishTechnicianApproved("Tech User", "tech@test.com", "tech@test.com");

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitMQConfig.TECHNICIAN_APPROVED_KEY),
                any(TechnicianApprovedEvent.class));
    }

    @Test
    void publishTechnicianRejected_Success() {
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(TechnicianRejectedEvent.class));

        notificationPublisher.publishTechnicianRejected("Tech User", "tech@test.com", "Not qualified");

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitMQConfig.TECHNICIAN_REJECTED_KEY),
                any(TechnicianRejectedEvent.class));
    }

    @Test
    void publishCustomerWelcome_Success() {
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(CustomerWelcomeEvent.class));

        notificationPublisher.publishCustomerWelcome("Customer Name", "customer@test.com");

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitMQConfig.CUSTOMER_WELCOME_KEY),
                any(CustomerWelcomeEvent.class));
    }
}
