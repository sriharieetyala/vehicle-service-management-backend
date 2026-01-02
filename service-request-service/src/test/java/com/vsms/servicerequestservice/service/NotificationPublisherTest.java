package com.vsms.servicerequestservice.service;

import com.vsms.servicerequestservice.config.RabbitMQConfig;
import com.vsms.servicerequestservice.dto.event.ServiceCompletedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private NotificationPublisher notificationPublisher;

    @Test
    void publishServiceCompleted_Success() {
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(ServiceCompletedEvent.class));

        notificationPublisher.publishServiceCompleted(
                "John Doe",
                "john@example.com",
                "Toyota Camry",
                "OIL_CHANGE",
                123L);

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitMQConfig.SERVICE_COMPLETED_KEY),
                any(ServiceCompletedEvent.class));
    }

    @Test
    void publishInvoiceGenerated_Success() {
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyMap());

        notificationPublisher.publishInvoiceGenerated(
                "INV-2026-0001",
                "Jane Doe",
                "jane@example.com",
                BigDecimal.valueOf(500));

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq("notification.invoice.generated"),
                anyMap());
    }

    @Test
    void publishInvoicePaid_Success() {
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyMap());

        notificationPublisher.publishInvoicePaid(
                "INV-2026-0001",
                "Jane Doe",
                "manager@vsms.com",
                BigDecimal.valueOf(500),
                "CARD");

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq("notification.invoice.paid"),
                anyMap());
    }
}
