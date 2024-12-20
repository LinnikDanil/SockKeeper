package ru.backspark.SockKeeper.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.backspark.SockKeeper.error.exception.FileProcessingException;
import ru.backspark.SockKeeper.error.exception.InsufficientSocksInWarehouseException;
import ru.backspark.SockKeeper.error.exception.InvalidDataFormatException;
import ru.backspark.SockKeeper.error.exception.SocksNotFoundInWarehouse;

@RestControllerAdvice(assignableTypes = {
        FileProcessingException.class,
        InsufficientSocksInWarehouseException.class,
        InvalidDataFormatException.class,
        SocksNotFoundInWarehouse.class
})
@Slf4j
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handlerFileProcessingException(final FileProcessingException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handlerSocksNotFoundInWarehouse(final SocksNotFoundInWarehouse e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handlerInvalidDataFormatException(final InvalidDataFormatException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handlerInsufficientSocksInWarehouseException(final InsufficientSocksInWarehouseException e) {
        return new ErrorResponse(e.getMessage());
    }

}
