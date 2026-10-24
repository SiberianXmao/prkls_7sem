package ru.pr5.order.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.pr5.order.service.InvalidOrderStateException;
import ru.pr5.order.service.OrderNotFoundException;

import java.net.URI;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(OrderNotFoundException.class)
    ProblemDetail handleNotFound(OrderNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Заказ не найден", exception.getMessage());
    }

    @ExceptionHandler(InvalidOrderStateException.class)
    ProblemDetail handleInvalidState(InvalidOrderStateException exception) {
        return problem(HttpStatus.CONFLICT, "Недопустимое состояние заказа", exception.getMessage());
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
