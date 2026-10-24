package ru.pr5.delivery.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.pr5.delivery.service.DeliveryNotFoundException;
import ru.pr5.delivery.service.DuplicateDeliveryException;
import ru.pr5.delivery.service.InvalidDeliveryStateException;
import ru.pr5.delivery.service.RelatedOrderNotFoundException;
import ru.pr5.delivery.service.RemoteOrderServiceException;

import java.net.URI;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(DeliveryNotFoundException.class)
    ProblemDetail handleNotFound(DeliveryNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Доставка не найдена", exception.getMessage());
    }

    @ExceptionHandler(RelatedOrderNotFoundException.class)
    ProblemDetail handleOrderNotFound(RelatedOrderNotFoundException exception) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Заказ не найден", exception.getMessage());
    }

    @ExceptionHandler({DuplicateDeliveryException.class, InvalidDeliveryStateException.class})
    ProblemDetail handleConflict(RuntimeException exception) {
        return problem(HttpStatus.CONFLICT, "Операция невозможна", exception.getMessage());
    }

    @ExceptionHandler(RemoteOrderServiceException.class)
    ProblemDetail handleRemoteService(RemoteOrderServiceException exception) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE,
                "Order Service недоступен", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        String detail = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Некорректные параметры запроса");
        return problem(HttpStatus.BAD_REQUEST, "Ошибка валидации", detail);
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create("about:blank"));
        return problem;
    }
}
