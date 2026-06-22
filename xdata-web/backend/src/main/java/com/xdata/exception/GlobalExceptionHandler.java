package com.xdata.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private ResponseEntity<Object> respond(HttpStatus status, String message, String details) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", message);
        if (details != null) body.put("details", details);
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied on {}: {}", request.getDescription(false), ex.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "Access denied: you do not have the required permissions.");
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    // ---- Client errors (bad input) -> proper 4xx instead of 500 -----------

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class,
                       HttpMessageNotReadableException.class})
    public ResponseEntity<Object> handleBadRequest(Exception ex, WebRequest request) {
        log.warn("Invalid request at {}: {}", request.getDescription(false), ex.getMessage());
        return respond(HttpStatus.BAD_REQUEST, "Invalid or incomplete request.", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.warn("Parameter type error at {}: {}", request.getDescription(false), ex.getMessage());
        return respond(HttpStatus.BAD_REQUEST, "Invalid parameter '" + ex.getName() + "'.", null);
    }

    @ExceptionHandler({NoSuchElementException.class, NoResourceFoundException.class})
    public ResponseEntity<Object> handleNotFound(Exception ex, WebRequest request) {
        log.warn("Resource not found at {}: {}", request.getDescription(false), ex.getMessage());
        return respond(HttpStatus.NOT_FOUND, "The requested resource was not found.", null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Object> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, WebRequest request) {
        log.warn("Method not allowed at {}: {}", request.getDescription(false), ex.getMessage());
        return respond(HttpStatus.METHOD_NOT_ALLOWED, "This HTTP method is not supported for the resource.", null);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex, WebRequest request) {
        log.error("Unexpected runtime error at {}: ", request.getDescription(false), ex);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "An internal error occurred.");
        body.put("details", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Internal system error at {}: ", request.getDescription(false), ex);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "An internal error occurred.");
        body.put("details", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
