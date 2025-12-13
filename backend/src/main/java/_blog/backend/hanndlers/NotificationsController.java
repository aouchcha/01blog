package _blog.backend.hanndlers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import org.apache.catalina.filters.RateLimitFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import _blog.backend.helpers.InvalidJwtException;
import _blog.backend.helpers.JwtUtil;
import _blog.backend.service.NotificationService;
import _blog.backend.service.RateLimiterService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = { "http://localhost:4200" })

public class NotificationsController {
    
    private final NotificationService notificationService;

    
    private final JwtUtil jwtUtil;

    private final RateLimiterService RateLimiterservice;

    public NotificationsController(NotificationService notificationService, JwtUtil jwtUtil, RateLimiterService RateLimiterservice) {
        this.notificationService = notificationService;
        this.jwtUtil = jwtUtil;
        this.RateLimiterservice = RateLimiterservice;
    }

    @GetMapping("/stream/{userId}")
    public SseEmitter stream(
            @PathVariable(required = false) Long userId,
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId,
            @RequestParam(required = false) String token,
            HttpServletResponse response) throws IOException {

        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"User ID is required\"}");
            response.getWriter().flush();
            return null;
        }

        // Validate BEFORE any SSE setup
        if (token == null || !jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid SSE token\"}");
            response.getWriter().flush();
            return null; // Exit early
        }


        try {
            return notificationService.connect(userId, lastEventId, token);
        } catch (InvalidJwtException e) {
            // Handle user not found
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
            response.getWriter().flush();
            return null;
        }
    }

    @GetMapping()
    public ResponseEntity<?> getNotifs(@RequestHeader("Authorization") String header,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastDate,
            @RequestParam(required = false) Long lastId) {
        String token = header.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid token"));
        }
        final String username = jwtUtil.getUsername(token);
        return notificationService.getNotifications(username, lastDate, lastId);
    }

    @PutMapping("/{notification_id}")
    public ResponseEntity<?> UpdateNotifs(@PathVariable Long notification_id,
            @RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid token"));
        }
          if (!RateLimiterservice.isAllowed(jwtUtil.getUsername(token))) {
            return ResponseEntity.status(429).body(Map.of("error", "Rate limit exceeded. Try again later."));
        }
        return notificationService.markAsRead(notification_id);
    }

}
