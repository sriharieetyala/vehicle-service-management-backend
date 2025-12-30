package com.vsms.inventoryservice.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String partNumber, int available, int requested) {
        super(String.format("Insufficient stock for part '%s': available=%d, requested=%d",
                partNumber, available, requested));
    }
}
