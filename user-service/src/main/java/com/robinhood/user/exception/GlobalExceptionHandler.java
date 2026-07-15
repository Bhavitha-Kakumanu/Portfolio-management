package com.robinhood.user.exception;

import com.robinhood.user.exception.WatchlistAlreadyExistsException;
import com.robinhood.user.exception.WatchlistNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

// @RestControllerAdvice intercepts exceptions thrown anywhere in the app
// and lets us return structured JSON error responses instead of stack traces.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 409 Conflict — user tried to register with an existing email/username
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ProblemDetail handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    // 404 Not Found
    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(WatchlistNotFoundException.class)
    public ProblemDetail handleWatchlistNotFound(WatchlistNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(WatchlistAlreadyExistsException.class)
    public ProblemDetail handleWatchlistAlreadyExists(WatchlistAlreadyExistsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    // 400 Bad Request — triggered by @Valid failures on request DTOs
    // Returns a map of field -> error message, e.g. {"email": "Must be a valid email address"}
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage,
                (existing, replacement) -> existing   // keep first message if duplicate field
            ));
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setDetail("Validation failed");
        detail.setProperty("errors", errors);
        return detail;
    }
}
