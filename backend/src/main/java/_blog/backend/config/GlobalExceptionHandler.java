package _blog.backend.config;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
        org.springframework.jdbc.CannotGetJdbcConnectionException.class,
        org.springframework.dao.DataAccessResourceFailureException.class,
        com.zaxxer.hikari.pool.HikariPool.PoolInitializationException.class,
        java.sql.SQLTransientConnectionException.class
    })
    public ResponseEntity<?> handleDatabaseErrors(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Database is unavailable. Please try later"));
    }
}
