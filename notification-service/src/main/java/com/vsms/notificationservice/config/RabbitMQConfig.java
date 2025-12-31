package com.vsms.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange
    public static final String NOTIFICATION_EXCHANGE = "vsms.notification.exchange";

    // Queues
    public static final String MANAGER_CREATED_QUEUE = "vsms.manager.created.queue";
    public static final String TECHNICIAN_APPROVED_QUEUE = "vsms.technician.approved.queue";
    public static final String TECHNICIAN_REJECTED_QUEUE = "vsms.technician.rejected.queue";
    public static final String SERVICE_COMPLETED_QUEUE = "vsms.service.completed.queue";
    public static final String INVOICE_GENERATED_QUEUE = "vsms.invoice.generated.queue";
    public static final String INVOICE_PAID_QUEUE = "vsms.invoice.paid.queue";
    public static final String CUSTOMER_WELCOME_QUEUE = "vsms.customer.welcome.queue";

    // Routing Keys
    public static final String MANAGER_CREATED_KEY = "manager.created";
    public static final String TECHNICIAN_APPROVED_KEY = "technician.approved";
    public static final String TECHNICIAN_REJECTED_KEY = "technician.rejected";
    public static final String SERVICE_COMPLETED_KEY = "service.completed";
    public static final String INVOICE_GENERATED_KEY = "invoice.generated";
    public static final String INVOICE_PAID_KEY = "invoice.paid";
    public static final String CUSTOMER_WELCOME_KEY = "customer.welcome";

    // Exchange
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    // Queues
    @Bean
    public Queue managerCreatedQueue() {
        return new Queue(MANAGER_CREATED_QUEUE, true);
    }

    @Bean
    public Queue technicianApprovedQueue() {
        return new Queue(TECHNICIAN_APPROVED_QUEUE, true);
    }

    @Bean
    public Queue technicianRejectedQueue() {
        return new Queue(TECHNICIAN_REJECTED_QUEUE, true);
    }

    @Bean
    public Queue serviceCompletedQueue() {
        return new Queue(SERVICE_COMPLETED_QUEUE, true);
    }

    @Bean
    public Queue invoiceGeneratedQueue() {
        return new Queue(INVOICE_GENERATED_QUEUE, true);
    }

    @Bean
    public Queue invoicePaidQueue() {
        return new Queue(INVOICE_PAID_QUEUE, true);
    }

    @Bean
    public Queue customerWelcomeQueue() {
        return new Queue(CUSTOMER_WELCOME_QUEUE, true);
    }

    // Bindings
    @Bean
    public Binding managerCreatedBinding(Queue managerCreatedQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(managerCreatedQueue).to(notificationExchange).with(MANAGER_CREATED_KEY);
    }

    @Bean
    public Binding technicianApprovedBinding(Queue technicianApprovedQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(technicianApprovedQueue).to(notificationExchange).with(TECHNICIAN_APPROVED_KEY);
    }

    @Bean
    public Binding technicianRejectedBinding(Queue technicianRejectedQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(technicianRejectedQueue).to(notificationExchange).with(TECHNICIAN_REJECTED_KEY);
    }

    @Bean
    public Binding serviceCompletedBinding(Queue serviceCompletedQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(serviceCompletedQueue).to(notificationExchange).with(SERVICE_COMPLETED_KEY);
    }

    @Bean
    public Binding invoiceGeneratedBinding(Queue invoiceGeneratedQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(invoiceGeneratedQueue).to(notificationExchange).with(INVOICE_GENERATED_KEY);
    }

    @Bean
    public Binding invoicePaidBinding(Queue invoicePaidQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(invoicePaidQueue).to(notificationExchange).with(INVOICE_PAID_KEY);
    }

    @Bean
    public Binding customerWelcomeBinding(Queue customerWelcomeQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(customerWelcomeQueue).to(notificationExchange).with(CUSTOMER_WELCOME_KEY);
    }

    // JSON Message Converter
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
