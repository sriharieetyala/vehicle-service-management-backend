package com.vsms.servicerequestservice.service;

import com.vsms.servicerequestservice.config.RabbitMQConfig;
import com.vsms.servicerequestservice.dto.event.ServiceCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

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
                Map<String, Object> event = new HashMap<>();
                event.put("type", "INVOICE_GENERATED");
                event.put("invoiceNumber", invoiceNumber);
                event.put("customerName", customerName);
                event.put("customerEmail", customerEmail);
                event.put("amount", amount);

                rabbitTemplate.convertAndSend(
                                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                                "notification.invoice.generated",
                                event);
                log.info("Published INVOICE_GENERATED event for invoice: {}", invoiceNumber);
        }

        public void publishInvoicePaid(String invoiceNumber, String customerName,
                        String managerEmail, BigDecimal amount, String paymentMethod) {
                Map<String, Object> event = new HashMap<>();
                event.put("type", "INVOICE_PAID");
                event.put("invoiceNumber", invoiceNumber);
                event.put("customerName", customerName);
                event.put("managerEmail", managerEmail);
                event.put("amount", amount);
                event.put("paymentMethod", paymentMethod);

                rabbitTemplate.convertAndSend(
                                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                                "notification.invoice.paid",
                                event);
                log.info("Published INVOICE_PAID event for invoice: {}", invoiceNumber);
        }
}
