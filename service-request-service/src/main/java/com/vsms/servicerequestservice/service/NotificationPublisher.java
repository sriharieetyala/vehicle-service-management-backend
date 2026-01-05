package com.vsms.servicerequestservice.service;

import com.vsms.servicerequestservice.config.RabbitMQConfig;
import com.vsms.servicerequestservice.dto.event.InvoiceGeneratedEvent;
import com.vsms.servicerequestservice.dto.event.InvoicePaidEvent;
import com.vsms.servicerequestservice.dto.event.ServiceCompletedEvent;
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

        public void publishServiceCompleted(String customerName, String customerEmail,
                        String vehicleInfo, String serviceName, Long serviceRequestId) {
                ServiceCompletedEvent event = ServiceCompletedEvent.builder()
                                .customerName(customerName)
                                .customerEmail(customerEmail)
                                .vehicleInfo(vehicleInfo)
                                .serviceName(serviceName)
                                .serviceRequestId(serviceRequestId)
                                .build();

                rabbitTemplate.convertAndSend(
                                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                                RabbitMQConfig.SERVICE_COMPLETED_KEY,
                                event);
                log.info("Published SERVICE_COMPLETED event for request: {}", serviceRequestId);
        }

        public void publishInvoiceGenerated(String invoiceNumber, String customerName,
                        String customerEmail, BigDecimal amount) {
                InvoiceGeneratedEvent event = InvoiceGeneratedEvent.builder()
                                .invoiceNumber(invoiceNumber)
                                .customerName(customerName)
                                .customerEmail(customerEmail)
                                .totalAmount(amount)
                                .build();

                rabbitTemplate.convertAndSend(
                                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                                "invoice.generated",
                                event);
                log.info("Published INVOICE_GENERATED event for invoice: {}", invoiceNumber);
        }

        public void publishInvoicePaid(String invoiceNumber, String customerName,
                        String managerEmail, BigDecimal amount, String paymentMethod) {
                InvoicePaidEvent event = InvoicePaidEvent.builder()
                                .invoiceNumber(invoiceNumber)
                                .customerName(customerName)
                                .managerEmail(managerEmail)
                                .amount(amount)
                                .paymentMethod(paymentMethod)
                                .build();

                rabbitTemplate.convertAndSend(
                                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                                "invoice.paid",
                                event);
                log.info("Published INVOICE_PAID event for invoice: {}", invoiceNumber);
        }
}
