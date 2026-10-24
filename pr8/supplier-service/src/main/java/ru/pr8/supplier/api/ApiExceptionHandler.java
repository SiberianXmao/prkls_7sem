package ru.pr8.supplier.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.pr8.supplier.service.CatalogException;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(CatalogException.class)
    ResponseEntity<ApiError> catalog(CatalogException exception, HttpServletRequest request) {
        return ResponseEntity.status(exception.getStatus())
                .body(new ApiError(Instant.now(), exception.getStatus(), exception.getMessage(), request.getRequestURI()));
    }
}
