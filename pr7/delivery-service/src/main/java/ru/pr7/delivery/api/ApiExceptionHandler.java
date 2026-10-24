package ru.pr7.delivery.api;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import ru.pr7.delivery.client.RelatedOrderNotFoundException;
import ru.pr7.delivery.client.RemoteOrderServiceException;
import ru.pr7.delivery.service.DeliveryNotFoundException;
import ru.pr7.delivery.service.InvalidDeliveryStateException;
import ru.pr7.delivery.service.OrderNotReadyException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler({DeliveryNotFoundException.class, RelatedOrderNotFoundException.class})
    public ResponseEntity<ApiError> notFound(RuntimeException exception, ServerWebExchange exchange) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage(), exchange);
    }

    @ExceptionHandler({InvalidDeliveryStateException.class, OrderNotReadyException.class})
    public ResponseEntity<ApiError> conflict(RuntimeException exception, ServerWebExchange exchange) {
        return response(HttpStatus.CONFLICT, exception.getMessage(), exchange);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiError> validation(
            WebExchangeBindException exception,
            ServerWebExchange exchange) {
        String message = exception.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return response(HttpStatus.BAD_REQUEST, message, exchange);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ApiError> badRequest(
            ServerWebInputException exception,
            ServerWebExchange exchange) {
        return response(HttpStatus.BAD_REQUEST, "Некорректный формат запроса", exchange);
    }

    @ExceptionHandler({RemoteOrderServiceException.class, DataAccessException.class})
    public ResponseEntity<ApiError> unavailable(RuntimeException exception, ServerWebExchange exchange) {
        return response(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), exchange);
    }

    private ResponseEntity<ApiError> response(
            HttpStatus status,
            String message,
            ServerWebExchange exchange) {
        ApiError error = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getPath().value());
        return ResponseEntity.status(status).body(error);
    }
}
