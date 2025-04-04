package com.strk.common.exception;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleOrderNotFoundException(
            OrderNotFoundException ex, ServerWebExchange exchange) {

        log.error("Order not found: {}", ex.getMessage());

        ErrorResponse errorResponse = buildErrorResponse(
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                exchange.getRequest().getPath().value(),
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            WebExchangeBindException ex, ServerWebExchange exchange) {

        List<String> details = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof FieldError ? ((FieldError) error).getField() : error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            details.add(fieldName + ": " + errorMessage);
        });

        log.error("Validation error: {}", details);

        ErrorResponse errorResponse = buildErrorResponse(
                "Validation error",
                HttpStatus.BAD_REQUEST,
                exchange.getRequest().getPath().value(),
                details
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, ServerWebExchange exchange) {

        log.error("Unhandled exception: ", ex);

        ErrorResponse errorResponse = buildErrorResponse(
                "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR,
                exchange.getRequest().getPath().value(),
                List.of(ex.getMessage())
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private ErrorResponse buildErrorResponse(
            String message,
            HttpStatus status,
            String path,
            List<String> details) {

        return ErrorResponse.builder()
                .message(message)
                .status(status.value())
                .error(status.getReasonPhrase())
                .path(path)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();
    }
}