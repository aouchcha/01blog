package _blog.backend.service;

import java.time.LocalDateTime;
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

    private static final long SSE_TIMEOUT = 10 * 60 * 1000L;
    private static final int NOTIFICATION_PAGE_SIZE = 20;
    private static final int MAX_CONNECTIONS_PER_USER = 5;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // Fully thread-safe
    private final Map<Long, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter connect(Long userId, String lastEventIdHeader, String token) {
        User user = userRepository.findByUsername(jwtUtil.getUsername(token));
        if (user == null || !user.getId().equals(userId)) {
            throw new InvalidJwtException("Invalid user or token mismatch");
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        registerEmitter(userId, emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));

        return emitter;
    }

    private void registerEmitter(Long userId, SseEmitter newEmitter) {

        emitters.compute(userId, (id, set) -> {
            if (set == null)
                set = ConcurrentHashMap.newKeySet();

            if (set.size() >= MAX_CONNECTIONS_PER_USER) {
                SseEmitter oldest = set.iterator().next();
                set.remove(oldest);
                try {
                    oldest.complete();
                } catch (Exception ignore) {
                }
            }

            set.add(newEmitter);
            return set;
        });
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        Set<SseEmitter> set = emitters.get(userId);
        if (set != null) {
            set.remove(emitter);
            if (set.isEmpty())
                emitters.remove(userId);
        }
    }

    public void sendNotification(User recipient, User creator, Post post) {
        if (recipient == null || creator == null || post == null) {
            throw new InvalidJwtException("Bad Request");
        }

        NotificationDTO dto = saveNotification(recipient, creator, post);

        Set<SseEmitter> set = emitters.get(recipient.getId());
        if (set == null || set.isEmpty())
            return;

        List<SseEmitter> safeEmitters = List.copyOf(set);

        safeEmitters.forEach(emitter -> sendNotificationEvent(emitter, dto));
    }

    private void sendNotificationEvent(SseEmitter emitter, NotificationDTO dto) {
        try {
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(dto.getId()))
                    .name("notification")
                    .data(Map.of(
                            "message", dto.getCreatorUsername() + " published a post",
                            "count", dto.getCount())));
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    public void sendReaction(Long userId, Like like, Post p) {
        if (userId == null || like == null) {
            throw new InvalidJwtException("Bad Request");
        }

        Set<SseEmitter> set = emitters.get(userId);
        if (set == null || set.isEmpty())
            return;

        List<SseEmitter> safeEmitters = List.copyOf(set);

        safeEmitters.forEach(emitter -> sendReactionEvent(emitter, like, p));
    }

    private void sendReactionEvent(SseEmitter emitter, Like like, Post p) {
        System.out.println("like sent in the sse " + p.getLikeCount());
        try {
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(like.getId()))
                    .name("reaction")
                    .data(Map.of("post", p)));
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }


    @Transactional
    public NotificationDTO saveNotification(User recipient, User creator, Post post) {
        NotificationEntity n = new NotificationEntity();
        n.setRecipient(recipient);
        n.setCreator(creator);
        n.setPost(post);

        NotificationEntity saved = notificationRepository.save(n);
        int unread = notificationRepository.countByRecipient_IdAndSeenFalse(recipient.getId());

        return new NotificationDTO(
                saved.getId(),
                saved.getCreator().getUsername(),
                unread,
                saved.getCreatedAt(),
                saved.isSeen()
            );
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
            notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDescIdDesc(
                    user.getId(),
                    pageRequest);
        } else {
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

}