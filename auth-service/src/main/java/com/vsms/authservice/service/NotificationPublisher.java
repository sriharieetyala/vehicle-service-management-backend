package com.vsms.authservice.service;

import com.vsms.authservice.config.RabbitMQConfig;
import com.vsms.authservice.dto.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishManagerCreated(String name, String email, String username, String temporaryPassword) {
        ManagerCreatedEvent event = ManagerCreatedEvent.builder()
                .managerName(name)
                .email(email)
                .username(username)
                .temporaryPassword(temporaryPassword)
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.MANAGER_CREATED_KEY,
                event);
        log.info("Published MANAGER_CREATED event for: {}", email);
    }

    public void publishTechnicianApproved(String name, String email, String username) {
        TechnicianApprovedEvent event = TechnicianApprovedEvent.builder()
                .technicianName(name)
                .email(email)
                .username(username)
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.TECHNICIAN_APPROVED_KEY,
                event);
        log.info("Published TECHNICIAN_APPROVED event for: {}", email);
    }

    public void publishTechnicianRejected(String name, String email, String reason) {
        TechnicianRejectedEvent event = TechnicianRejectedEvent.builder()
                .technicianName(name)
                .email(email)
                .reason(reason)
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.TECHNICIAN_REJECTED_KEY,
                event);
        log.info("Published TECHNICIAN_REJECTED event for: {}", email);
    }

    public void publishCustomerWelcome(String name, String email) {
        CustomerWelcomeEvent event = CustomerWelcomeEvent.builder()
                .customerName(name)
                .email(email)
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.CUSTOMER_WELCOME_KEY,
                event);
        log.info("Published CUSTOMER_WELCOME event for: {}", email);
    }
}
