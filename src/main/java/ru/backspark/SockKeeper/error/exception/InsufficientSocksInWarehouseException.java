package ru.backspark.SockKeeper.error.exception;

public class InsufficientSocksInWarehouseException extends RuntimeException {
    public InsufficientSocksInWarehouseException(String message) {
        super(message);
    }
}
