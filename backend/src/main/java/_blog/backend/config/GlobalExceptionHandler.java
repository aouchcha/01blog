package _blog.backend.config;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import _blog.backend.helpers.InvalidJwtException;
import io.jsonwebtoken.io.IOException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            org.springframework.jdbc.CannotGetJdbcConnectionException.class,
            org.springframework.dao.DataAccessResourceFailureException.class,
            com.zaxxer.hikari.pool.HikariPool.PoolInitializationException.class,
            IOException.class,
            java.sql.SQLTransientConnectionException.class
    })
    public ResponseEntity<?> handleDatabaseErrors(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Database is unavailable. Please try later"));
    }

    @ExceptionHandler({
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
            InvalidJwtException.class
    })
    public ResponseEntity<?> handleRuntimeExceptions(RuntimeException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Can not find the resource requested"));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<?> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Conflict occurred while processing your request. Please try again."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String errorMessage = "Data integrity violation occurred.";

        // Check if it's a duplicate key error
        if (ex.getMessage() != null && ex.getMessage().contains("duplicate key")) {
            errorMessage = "You have already reacted to this post.";
        }

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", errorMessage));
    }
}
