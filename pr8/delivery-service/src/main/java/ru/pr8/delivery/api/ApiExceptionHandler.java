package ru.pr8.delivery.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import ru.pr8.delivery.service.DeliveryNotFoundException;
import ru.pr8.delivery.service.InvalidDeliveryStateException;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(DeliveryNotFoundException.class)
    ResponseEntity<ApiError> notFound(DeliveryNotFoundException exception, ServerWebExchange exchange) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage(), exchange);
    }

    @ExceptionHandler(InvalidDeliveryStateException.class)
    ResponseEntity<ApiError> conflict(InvalidDeliveryStateException exception, ServerWebExchange exchange) {
        return response(HttpStatus.CONFLICT, exception.getMessage(), exchange);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    ResponseEntity<ApiError> validation(WebExchangeBindException exception, ServerWebExchange exchange) {
        return response(HttpStatus.BAD_REQUEST, "Invalid request body", exchange);
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String message, ServerWebExchange exchange) {
        return ResponseEntity.status(status).body(new ApiError(
                Instant.now(), status.value(), message, exchange.getRequest().getPath().value()));
    }
}
