package com.xdata.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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
        log.warn("Zugriff verweigert auf {}: {}", request.getDescription(false), ex.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "Zugriff verweigert: Sie haben nicht die erforderlichen Berechtigungen.");
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    // ---- Client errors (bad input) -> proper 4xx instead of 500 -----------

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class,
                       HttpMessageNotReadableException.class})
    public ResponseEntity<Object> handleBadRequest(Exception ex, WebRequest request) {
        log.warn("Ungültige Anfrage bei {}: {}", request.getDescription(false), ex.getMessage());
        return respond(HttpStatus.BAD_REQUEST, "Ungültige oder unvollständige Anfrage.", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.warn("Parameter-Typfehler bei {}: {}", request.getDescription(false), ex.getMessage());
        return respond(HttpStatus.BAD_REQUEST, "Ungültiger Parameter '" + ex.getName() + "'.", null);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Object> handleNotFound(NoSuchElementException ex, WebRequest request) {
        log.warn("Ressource nicht gefunden bei {}: {}", request.getDescription(false), ex.getMessage());
        return respond(HttpStatus.NOT_FOUND, "Die angeforderte Ressource wurde nicht gefunden.", null);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex, WebRequest request) {
        log.error("Unerwarteter Laufzeitfehler bei {}: ", request.getDescription(false), ex);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "Ein interner Fehler ist aufgetreten.");
        body.put("details", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Interner Systemfehler bei {}: ", request.getDescription(false), ex);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "Ein interner Fehler ist aufgetreten.");
        body.put("details", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
