package _blog.backend.hanndlers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

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
// import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import _blog.backend.helpers.InvalidJwtException;
// import _blog.backend.Repos.ReportRepository;
import _blog.backend.helpers.JwtUtil;
import _blog.backend.service.NotificationService;
// import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = { "http://localhost:4200" })

public class NotificationsController {
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtUtil jwtUtil;



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
            // System.out.println("VVVVVVVVVVVVVVVVVVVVVv   " + token);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid SSE token\"}");
            response.getWriter().flush();
            return null; // Exit early
        }

        // if (jwtUtil.get)

        try {
            return notificationService.connect(userId, lastEventId, token);
            // Spring handles SSE from here
        } catch (InvalidJwtException e) {
            // Handle user not found
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
            response.getWriter().flush();
            return null;
        }
    }

    // @GetMapping("/disconnect/{userId}")
    // public void disconnect(@PathVariable Long userId) {
    // notificationService.disconnect(userId);
    // }

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
        return notificationService.markAsRead(notification_id);
    }

}
