package _blog.backend.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import _blog.backend.Entitys.Interactions.Reactions.Like;
import _blog.backend.Entitys.Notifications.NotificationDTO;
import _blog.backend.Entitys.Notifications.NotificationEntity;
import _blog.backend.Entitys.Post.Post;
import _blog.backend.Entitys.User.User;
import _blog.backend.Repos.NotificationRepository;
import _blog.backend.Repos.UserRepository;
import _blog.backend.helpers.InvalidJwtException;
import _blog.backend.helpers.JwtUtil;

@Service
public class NotificationService {

    private static final long SSE_TIMEOUT = 10 * 60 * 1000L; // 10 minutes
    private static final int NOTIFICATION_PAGE_SIZE = 20;
    private static final int MAX_CONNECTIONS_PER_USER = 5; // Limit connections per user

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private final Map<Long, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter connect(Long userId, String lastEventIdHeader, String token) throws InvalidJwtException {
        User user = userRepository.findByUsername(jwtUtil.getUsername(token));
        if (user == null || !user.getId().equals(userId)) {
            throw new InvalidJwtException("Invalid user or token mismatch");
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        registerEmitterWithLimit(userId, emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));

        return emitter;
    }

    private void registerEmitterWithLimit(Long userId, SseEmitter newEmitter) {
        emitters.compute(userId, (key, existingEmitters) -> {
            if (existingEmitters == null) {
                existingEmitters = new LinkedHashSet<>();
            }

            if (existingEmitters.size() >= MAX_CONNECTIONS_PER_USER) {
                SseEmitter oldest = existingEmitters.iterator().next();
                existingEmitters.remove(oldest);

                try {
                    oldest.complete();
                } catch (RuntimeException e) {
                    System.err.println("Error closing old SSE connection: " + e.getMessage());
                }
            }

            existingEmitters.add(newEmitter);
            return existingEmitters;
        });
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        Set<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) {
                emitters.remove(userId);
                // System.out.println("All SSE connections closed for user " + userId);
            }
        }
    }

    public void sendNotification(User recipient, User creator, Post post) throws InvalidJwtException {
        if (recipient == null || creator == null || post == null) {
            throw new InvalidJwtException("Bad Request");
        }

        NotificationDTO notificationData = saveNotification(recipient, creator, post);

        Set<SseEmitter> userEmitters = emitters.get(recipient.getId());
        if (userEmitters != null && !userEmitters.isEmpty()) {
            userEmitters.forEach(emitter -> sendNotificationEvent(emitter, notificationData));
        }
    }

    @Transactional
    public NotificationDTO saveNotification(User recipient, User creator, Post post) {
        NotificationEntity notification = new NotificationEntity();
        notification.setRecipient(recipient);
        notification.setCreator(creator);
        notification.setPost(post);

        NotificationEntity savedNotification = notificationRepository.save(notification);

        int unreadCount = notificationRepository.countByRecipient_IdAndSeenFalse(recipient.getId());

        return new NotificationDTO(
                savedNotification.getId(),
                savedNotification.getCreator().getUsername(),
                unreadCount,
                savedNotification.getCreatedAt(),
                savedNotification.isSeen());
    }

    private void sendNotificationEvent(SseEmitter emitter, NotificationDTO notification) {
        try {
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(notification.getId()))
                    .name("notification")
                    .data(Map.of(
                            "message", String.format("%s published a post", notification.getCreatorUsername()),
                            "count", notification.getCount())));
        } catch (Exception e) {
            // System.err.println("Failed to send notification event: " + e.getMessage());
            emitter.completeWithError(e);
        }
    }

    public void sendReaction(Long userId, Like like) {
        if (userId == null || like == null) {
            throw new InvalidJwtException("Bad Request");
        }

        Set<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null && !userEmitters.isEmpty()) {
            userEmitters.forEach(emitter -> sendReactionEvent(emitter, like));
        }
    }

    private void sendReactionEvent(SseEmitter emitter, Like like) {
        try {
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(like.getId()))
                    .name("reaction")
                    .data(Map.of("post", like.getPost())));
        } catch (IOException e) {
            System.err.println("Failed to send reaction event: " + e.getMessage());
            emitter.completeWithError(e);
        }
    }

    public ResponseEntity<?> getNotifications(String username, LocalDateTime lastDate, Long lastId) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid credentials"));
        }

        List<NotificationEntity> notifications;
        PageRequest pageRequest = PageRequest.of(0, NOTIFICATION_PAGE_SIZE);

        if (lastDate == null || lastId == null) {
            // First page - get latest notifications
            notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDescIdDesc(
                    user.getId(),
                    pageRequest);
        } else {
            // Pagination - get older notifications
            notifications = notificationRepository
                    .findByRecipientIdAndCreatedAtLessThanOrCreatedAtEqualsAndIdLessThanOrderByCreatedAtDescIdDesc(
                            user.getId(),
                            lastDate,
                            lastId,
                            pageRequest);
        }

        return ResponseEntity.ok(Map.of("notifications", notifications));
    }


    @Transactional
    public ResponseEntity<?> markAsRead(Long notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId).orElse(null);

        if (notification == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Notification not found"));
        }

        if (notification.isSeen()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Notification already marked as read"));
        }

        notification.setSeen(true);
        NotificationEntity updatedNotification = notificationRepository.save(notification);

        return ResponseEntity.ok(Map.of(
                "message", "Notification marked as read",
                "notification", updatedNotification));
    }

  
    public int getActiveConnectionsCount() {
        return emitters.values().stream()
                .mapToInt(Set::size)
                .sum();
    }

}