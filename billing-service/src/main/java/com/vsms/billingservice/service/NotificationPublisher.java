package com.vsms.billingservice.service;

import com.vsms.billingservice.config.RabbitMQConfig;
import com.vsms.billingservice.dto.event.InvoiceGeneratedEvent;
import com.vsms.billingservice.dto.event.InvoicePaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishInvoiceGenerated(String invoiceNumber, String customerName, String customerEmail,
            BigDecimal totalAmount) {
        InvoiceGeneratedEvent event = InvoiceGeneratedEvent.builder()
                .invoiceNumber(invoiceNumber)
                .customerName(customerName)
                .customerEmail(customerEmail)
                .totalAmount(totalAmount)
                .serviceName("Vehicle Service")
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.INVOICE_GENERATED_KEY,
                event);
        log.info("Published INVOICE_GENERATED event for: {}", invoiceNumber);
    }

    public void publishInvoicePaid(String invoiceNumber, String customerName, String managerEmail, BigDecimal amount,
            String paymentMethod) {
        InvoicePaidEvent event = InvoicePaidEvent.builder()
                .invoiceNumber(invoiceNumber)
                .customerName(customerName)
                .managerEmail(managerEmail)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.INVOICE_PAID_KEY,
                event);
        log.info("Published INVOICE_PAID event for: {}", invoiceNumber);
    }
}
