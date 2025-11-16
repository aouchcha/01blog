package _blog.backend.hanndlers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import _blog.backend.helpers.JwtUtil;
import _blog.backend.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = { "http://localhost:4200" })

public class NotificationsController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/stream/{userId}")
    public SseEmitter stream(
            @PathVariable Long userId,
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {
        return notificationService.connect(userId, lastEventId);
    }

    @GetMapping("/disconnect/{userId}")
    public void disconnect(@PathVariable Long userId) {
        notificationService.disconnect(userId);
    }

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping()
    public ResponseEntity<?> getNotifs(@RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "invalid token"));
        }
        final String username = jwtUtil.getUsername(token); 
        return notificationService.getNotifs(username);
    }

}
