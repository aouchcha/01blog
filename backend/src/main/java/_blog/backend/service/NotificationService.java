package _blog.backend.service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import _blog.backend.Entitys.Interactions.Reactions.Like;
import _blog.backend.Entitys.Notifications.NotificationEntity;
import _blog.backend.Entitys.User.User;
import _blog.backend.Repos.NotificationRepository;
import jakarta.annotation.PreDestroy;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository repo;
    private final Map<Long, SseEmitter> emitters = new HashMap<>();

    public SseEmitter connect(Long userId, String lastEventIdHeader) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> {
            emitters.remove(userId);
            System.out.println("Siiiiiiiiiiiiiiiiiiiiiiiiiiiize " + emitters.size());
        });
        emitter.onTimeout(() -> {
            emitters.remove(userId);
            System.out.println("Siiiiiiiiiiiiiiiiiiiiiiiiiiiize " + emitters.size());
        });
        emitter.onError((e) -> {
            emitters.remove(userId);
            System.out.println("Siiiiiiiiiiiiiiiiiiiiiiiiiiiize " + emitters.size());
        });

        // If client reconnects, send missed notifications
        if (lastEventIdHeader != null) {
            try {
                Long lastId = Long.parseLong(lastEventIdHeader);
                List<NotificationEntity> missed = repo.findByCreator_IdAndIdGreaterThan(userId, lastId);
                int count = repo.countByRecipient_IdAndSeenFalse(userId);
                missed.forEach(n -> sendEvent(emitter, n, count));
            } catch (NumberFormatException ignored) {
            }
        }

        return emitter;
    }

    public void sendNotification(User recipient, User creator) {
        // 1. Save notification in DB
        NotificationEntity notif = new NotificationEntity();
        notif.setRecipient(recipient);
        notif.setCreator(creator);
        NotificationEntity saved = repo.save(notif);

        int count = repo.countByRecipient_IdAndSeenFalse(recipient.getId());

        // 2. Push to connected client if exists
        SseEmitter emitter = emitters.get(recipient.getId());
        if (emitter != null)
            sendEvent(emitter, saved, count);
    }

    private void sendEvent(SseEmitter emitter, NotificationEntity notif, int count) {
        try {
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(notif.getId())) // 👈 Use DB ID as event ID
                    .name("notification")
                    .data(Map.of("message", notif.getCreator().getUsername() + " publish a post", "count", count)));
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    public void sendReaction(Long UserId, Like l) {
        System.out.println("Siiiiiiiiiiiiiiiiiiiiiiiiiiiize " + emitters.size());

        SseEmitter emitter = emitters.get(UserId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .id(String.valueOf(l.getId())) // 👈 Use DB ID as event ID
                        .name("reaction")
                        .data(Map.of("post", l.getPost())));
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }
    }

    @PreDestroy
    public void onShutdown() {
        System.out.println("🧹 Closing all SSE emitters before shutdown...");
        for (SseEmitter emitter : emitters.values()) {
            try {
                emitter.complete();
            } catch (Exception ignored) {
            }
        }
        emitters.clear();
        System.out.println("✅ All SSE emitters closed.");
    }
}
