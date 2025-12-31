package com.vsms.notificationservice.listener;

import com.vsms.notificationservice.config.RabbitMQConfig;
import com.vsms.notificationservice.dto.*;
import com.vsms.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.MANAGER_CREATED_QUEUE)
    public void handleManagerCreated(ManagerCreatedEvent event) {
        log.info("Received MANAGER_CREATED event for: {}", event.getEmail());
        try {
            emailService.sendManagerCreatedEmail(
                    event.getEmail(),
                    event.getManagerName(),
                    event.getUsername(),
                    event.getTemporaryPassword());
            log.info("Email sent successfully for MANAGER_CREATED");
        } catch (Exception e) {
            log.error("Failed to send email for MANAGER_CREATED: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.TECHNICIAN_APPROVED_QUEUE)
    public void handleTechnicianApproved(TechnicianApprovedEvent event) {
        log.info("Received TECHNICIAN_APPROVED event for: {}", event.getEmail());
        try {
            emailService.sendTechnicianApprovedEmail(
                    event.getEmail(),
                    event.getTechnicianName(),
                    event.getUsername());
            log.info("Email sent successfully for TECHNICIAN_APPROVED");
        } catch (Exception e) {
            log.error("Failed to send email for TECHNICIAN_APPROVED: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.TECHNICIAN_REJECTED_QUEUE)
    public void handleTechnicianRejected(TechnicianRejectedEvent event) {
        log.info("Received TECHNICIAN_REJECTED event for: {}", event.getEmail());
        try {
            emailService.sendTechnicianRejectedEmail(
                    event.getEmail(),
                    event.getTechnicianName(),
                    event.getReason());
            log.info("Email sent successfully for TECHNICIAN_REJECTED");
        } catch (Exception e) {
            log.error("Failed to send email for TECHNICIAN_REJECTED: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.SERVICE_COMPLETED_QUEUE)
    public void handleServiceCompleted(ServiceCompletedEvent event) {
        log.info("Received SERVICE_COMPLETED event for request: {}", event.getServiceRequestId());
        try {
            emailService.sendServiceCompletedEmail(
                    event.getCustomerEmail(),
                    event.getCustomerName(),
                    event.getVehicleInfo(),
                    event.getServiceName());
            log.info("Email sent successfully for SERVICE_COMPLETED");
        } catch (Exception e) {
            log.error("Failed to send email for SERVICE_COMPLETED: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.INVOICE_GENERATED_QUEUE)
    public void handleInvoiceGenerated(InvoiceGeneratedEvent event) {
        log.info("Received INVOICE_GENERATED event for: {}", event.getInvoiceNumber());
        try {
            emailService.sendInvoiceGeneratedEmail(
                    event.getCustomerEmail(),
                    event.getCustomerName(),
                    event.getInvoiceNumber(),
                    event.getTotalAmount().toString());
            log.info("Email sent successfully for INVOICE_GENERATED");
        } catch (Exception e) {
            log.error("Failed to send email for INVOICE_GENERATED: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.INVOICE_PAID_QUEUE)
    public void handleInvoicePaid(InvoicePaidEvent event) {
        log.info("Received INVOICE_PAID event for: {}", event.getInvoiceNumber());
        try {
            emailService.sendInvoicePaidEmail(
                    event.getManagerEmail(),
                    event.getInvoiceNumber(),
                    event.getCustomerName(),
                    event.getAmount().toString(),
                    event.getPaymentMethod());
            log.info("Email sent successfully for INVOICE_PAID");
        } catch (Exception e) {
            log.error("Failed to send email for INVOICE_PAID: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.CUSTOMER_WELCOME_QUEUE)
    public void handleCustomerWelcome(CustomerWelcomeEvent event) {
        log.info("Received CUSTOMER_WELCOME event for: {}", event.getEmail());
        try {
            emailService.sendCustomerWelcomeEmail(
                    event.getEmail(),
                    event.getCustomerName());
            log.info("Email sent successfully for CUSTOMER_WELCOME");
        } catch (Exception e) {
            log.error("Failed to send email for CUSTOMER_WELCOME: {}", e.getMessage());
        }
    }
}
