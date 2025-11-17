package _blog.backend.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import _blog.backend.Entitys.Interactions.Reactions.Like;
import _blog.backend.Entitys.Notifications.NotificationDTO;
import _blog.backend.Entitys.Notifications.NotificationEntity;
import _blog.backend.Entitys.Post.Post;
import _blog.backend.Entitys.User.User;
import _blog.backend.Repos.NotificationRepository;
import _blog.backend.Repos.UserRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository repo;
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter connect(Long userId, String lastEventIdHeader) {
        SseEmitter emitter = new SseEmitter(10 * 60 * 1000L);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> {
            emitters.remove(userId);
            System.out.println("Siiiiiiiiiiiiiiiiiiiiiiiiiiiize comp " + emitters.size());
        });
        emitter.onTimeout(() -> {
            emitters.remove(userId);
            System.out.println("Siiiiiiiiiiiiiiiiiiiiiiiiiiiize timeout " + emitters.size());
        });
        emitter.onError((e) -> {
            emitters.remove(userId);
            System.out.println("Siiiiiiiiiiiiiiiiiiiiiiiiiiiize Err " + emitters.size());
        });

        return emitter;
    }

    // @Async
    public void sendNotification(User recipient, User creator, Post p) {
        final NotificationDTO data = SaveNotif(recipient, creator, p);

        SseEmitter emitter = emitters.get(recipient.getId());
        if (emitter != null)
            sendEvent(emitter, data);
    }

    @Transactional
    public NotificationDTO SaveNotif(User recipient, User creator, Post p) {
        NotificationEntity notif = new NotificationEntity();
        notif.setRecipient(recipient);
        notif.setCreator(creator);
        notif.setPost(p);
        NotificationEntity saved = repo.save(notif);

        int count = repo.countByRecipient_IdAndSeenFalse(recipient.getId());
        NotificationDTO n = new NotificationDTO(saved.getId(), saved.getCreator().getUsername(), count, saved.getCreatedAt(), saved.isSeen());
        return n;
    }

    private void sendEvent(SseEmitter emitter, NotificationDTO notif) {
        try {
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(notif.getId()))
                    .name("notification")
                    .data(Map.of("message", String.format( "%s publish a post",notif.getCreatorUsername()), "count", notif.getCount())));
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    public void sendReaction(Long UserId, Like l) {
        SseEmitter emitter = emitters.get(UserId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .id(String.valueOf(l.getId()))
                        .name("reaction")
                        .data(Map.of("post", l.getPost())));
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }
    }

    // @PreDestroy
    // public void onShutdown() {
    // System.out.println("🧹 Closing all SSE emitters before shutdown...");
    // for (SseEmitter emitter : emitters.values()) {
    // try {
    // emitter.complete();
    // } catch (Exception ignored) {
    // }
    // }
    // emitters.clear();
    // System.out.println("✅ All SSE emitters closed.");
    // }

    public void disconnect(Long userId) {
        SseEmitter emitter = emitters.remove(userId);
        if (emitter != null) {
            emitter.complete();
        }
    }

    @Autowired UserRepository userRepository;

    public ResponseEntity<?> getNotifs(String username) {
        User u = userRepository.findByUsername(username);
        if (u == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Ivalid Cridential"));
        }
        List<NotificationEntity> notifs = repo.findByRecipient_IdAndSeenFalse(u.getId());
        return ResponseEntity.ok(Map.of("notifs", notifs));
    }

    public ResponseEntity<?> markAsRead(Long notification_id) {
        NotificationEntity notif = repo.findById(notification_id).orElse(null);
        if (notif == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Notification not found"));
        }
        notif.setSeen(true);
        final NotificationEntity n = repo.save(notif);
        return ResponseEntity.ok(Map.of("message", "Notification marked as read", "notification", n));
    }
}
