package com.vsms.servicerequestservice.enums;

public enum RequestStatus {
    PENDING, // Customer created, waiting for assignment
    ASSIGNED, // Technician assigned
    IN_PROGRESS, // Work started
    COMPLETED, // Work done
    CANCELLED // Customer cancelled
}
