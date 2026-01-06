package com.vsms.servicerequestservice.service;

import com.vsms.servicerequestservice.config.RabbitMQConfig;
import com.vsms.servicerequestservice.dto.event.InvoiceGeneratedEvent;
import com.vsms.servicerequestservice.dto.event.InvoicePaidEvent;
import com.vsms.servicerequestservice.dto.event.ServiceCompletedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationPublisherTest {

        @Mock
        private RabbitTemplate rabbitTemplate;

        @InjectMocks
        private NotificationPublisher notificationPublisher;

        @Captor
        private ArgumentCaptor<ServiceCompletedEvent> serviceCompletedCaptor;

        @Captor
        private ArgumentCaptor<InvoiceGeneratedEvent> invoiceGeneratedCaptor;

        @Captor
        private ArgumentCaptor<InvoicePaidEvent> invoicePaidCaptor;

        @Test
        void publishServiceCompleted_Success() {
                String customerName = "John Doe";
                String customerEmail = "john@test.com";
                String vehicleInfo = "Toyota Camry (ABC123)";
                String serviceName = "REGULAR_SERVICE";
                Long serviceRequestId = 1L;

                notificationPublisher.publishServiceCompleted(
                                customerName, customerEmail, vehicleInfo, serviceName, serviceRequestId);

                verify(rabbitTemplate, times(1)).convertAndSend(
                                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                                eq(RabbitMQConfig.SERVICE_COMPLETED_KEY),
                                serviceCompletedCaptor.capture());

                ServiceCompletedEvent event = serviceCompletedCaptor.getValue();
                assertEquals(customerName, event.getCustomerName());
                assertEquals(customerEmail, event.getCustomerEmail());
                assertEquals(vehicleInfo, event.getVehicleInfo());
                assertEquals(serviceName, event.getServiceName());
                assertEquals(serviceRequestId, event.getServiceRequestId());
        }

        @Test
        void publishInvoiceGenerated_Success() {
                String invoiceNumber = "INV-2026-0001";
                String customerName = "Jane Doe";
                String customerEmail = "jane@test.com";
                BigDecimal amount = BigDecimal.valueOf(500);

                notificationPublisher.publishInvoiceGenerated(
                                invoiceNumber, customerName, customerEmail, amount);

                verify(rabbitTemplate, times(1)).convertAndSend(
                                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                                eq("invoice.generated"),
                                invoiceGeneratedCaptor.capture());

                InvoiceGeneratedEvent event = invoiceGeneratedCaptor.getValue();
                assertEquals(invoiceNumber, event.getInvoiceNumber());
                assertEquals(customerName, event.getCustomerName());
                assertEquals(customerEmail, event.getCustomerEmail());
                assertEquals(amount, event.getTotalAmount());
        }

        @Test
        void publishInvoicePaid_Success() {
                String invoiceNumber = "INV-2026-0002";
                String customerName = "Bob Smith";
                String managerEmail = "manager@test.com";
                BigDecimal amount = BigDecimal.valueOf(750);
                String paymentMethod = "CARD";

                notificationPublisher.publishInvoicePaid(
                                invoiceNumber, customerName, managerEmail, amount, paymentMethod);

                verify(rabbitTemplate, times(1)).convertAndSend(
                                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                                eq("invoice.paid"),
                                invoicePaidCaptor.capture());

                InvoicePaidEvent event = invoicePaidCaptor.getValue();
                assertEquals(invoiceNumber, event.getInvoiceNumber());
                assertEquals(customerName, event.getCustomerName());
                assertEquals(managerEmail, event.getManagerEmail());
                assertEquals(amount, event.getAmount());
                assertEquals(paymentMethod, event.getPaymentMethod());
        }

        @Test
        void publishServiceCompleted_WithNullFields_Success() {
                notificationPublisher.publishServiceCompleted(
                                null, null, null, null, null);

                verify(rabbitTemplate, times(1)).convertAndSend(
                                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                                eq(RabbitMQConfig.SERVICE_COMPLETED_KEY),
                                serviceCompletedCaptor.capture());

                ServiceCompletedEvent event = serviceCompletedCaptor.getValue();
                assertNull(event.getCustomerName());
                assertNull(event.getCustomerEmail());
        }

        @Test
        void publishInvoiceGenerated_WithZeroAmount_Success() {
                String invoiceNumber = "INV-2026-0003";
                String customerName = "Test Customer";
                String customerEmail = "test@test.com";
                BigDecimal amount = BigDecimal.ZERO;

                notificationPublisher.publishInvoiceGenerated(
                                invoiceNumber, customerName, customerEmail, amount);

                verify(rabbitTemplate, times(1)).convertAndSend(
                                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                                eq("invoice.generated"),
                                invoiceGeneratedCaptor.capture());

                assertEquals(BigDecimal.ZERO, invoiceGeneratedCaptor.getValue().getTotalAmount());
        }

        @Test
        void publishInvoicePaid_WithCashPayment_Success() {
                notificationPublisher.publishInvoicePaid(
                                "INV-2026-0004", "Customer", "manager@test.com",
                                BigDecimal.valueOf(100), "CASH");

                verify(rabbitTemplate, times(1)).convertAndSend(
                                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                                eq("invoice.paid"),
                                invoicePaidCaptor.capture());

                assertEquals("CASH", invoicePaidCaptor.getValue().getPaymentMethod());
        }

        @Test
        void publishInvoicePaid_WithUPIPayment_Success() {
                notificationPublisher.publishInvoicePaid(
                                "INV-2026-0005", "Customer", "manager@test.com",
                                BigDecimal.valueOf(200), "UPI");

                verify(rabbitTemplate, times(1)).convertAndSend(
                                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                                eq("invoice.paid"),
                                invoicePaidCaptor.capture());

                assertEquals("UPI", invoicePaidCaptor.getValue().getPaymentMethod());
        }
}
