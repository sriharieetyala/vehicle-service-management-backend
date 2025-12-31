package com.vsms.servicerequestservice.service;

import com.vsms.servicerequestservice.config.RabbitMQConfig;
import com.vsms.servicerequestservice.dto.event.ServiceCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

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
}
