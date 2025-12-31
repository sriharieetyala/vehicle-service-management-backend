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
        emailService.sendManagerCreatedEmail(
                event.getEmail(),
                event.getManagerName(),
                event.getUsername(),
                event.getTemporaryPassword());
    }

    @RabbitListener(queues = RabbitMQConfig.TECHNICIAN_APPROVED_QUEUE)
    public void handleTechnicianApproved(TechnicianApprovedEvent event) {
        log.info("Received TECHNICIAN_APPROVED event for: {}", event.getEmail());
        emailService.sendTechnicianApprovedEmail(
                event.getEmail(),
                event.getTechnicianName(),
                event.getUsername());
    }

    @RabbitListener(queues = RabbitMQConfig.TECHNICIAN_REJECTED_QUEUE)
    public void handleTechnicianRejected(TechnicianRejectedEvent event) {
        log.info("Received TECHNICIAN_REJECTED event for: {}", event.getEmail());
        emailService.sendTechnicianRejectedEmail(
                event.getEmail(),
                event.getTechnicianName(),
                event.getReason());
    }

    @RabbitListener(queues = RabbitMQConfig.SERVICE_COMPLETED_QUEUE)
    public void handleServiceCompleted(ServiceCompletedEvent event) {
        log.info("Received SERVICE_COMPLETED event for request: {}", event.getServiceRequestId());
        emailService.sendServiceCompletedEmail(
                event.getCustomerEmail(),
                event.getCustomerName(),
                event.getVehicleInfo(),
                event.getServiceName());
    }

    @RabbitListener(queues = RabbitMQConfig.INVOICE_GENERATED_QUEUE)
    public void handleInvoiceGenerated(InvoiceGeneratedEvent event) {
        log.info("Received INVOICE_GENERATED event for: {}", event.getInvoiceNumber());
        emailService.sendInvoiceGeneratedEmail(
                event.getCustomerEmail(),
                event.getCustomerName(),
                event.getInvoiceNumber(),
                event.getTotalAmount().toString());
    }

    @RabbitListener(queues = RabbitMQConfig.INVOICE_PAID_QUEUE)
    public void handleInvoicePaid(InvoicePaidEvent event) {
        log.info("Received INVOICE_PAID event for: {}", event.getInvoiceNumber());
        emailService.sendInvoicePaidEmail(
                event.getManagerEmail(),
                event.getInvoiceNumber(),
                event.getCustomerName(),
                event.getAmount().toString(),
                event.getPaymentMethod());
    }

    @RabbitListener(queues = RabbitMQConfig.CUSTOMER_WELCOME_QUEUE)
    public void handleCustomerWelcome(CustomerWelcomeEvent event) {
        log.info("Received CUSTOMER_WELCOME event for: {}", event.getEmail());
        emailService.sendCustomerWelcomeEmail(
                event.getEmail(),
                event.getCustomerName());
    }
}
