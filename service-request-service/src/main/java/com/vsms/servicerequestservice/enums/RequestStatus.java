package com.vsms.servicerequestservice.enums;

public enum RequestStatus {
    PENDING, // Customer created, waiting for assignment
    ASSIGNED, // Technician assigned
    IN_PROGRESS, // Work started
    COMPLETED, // Work done, awaiting payment
    CLOSED, // Payment received, service closed
    CANCELLED // Customer cancelled
}
