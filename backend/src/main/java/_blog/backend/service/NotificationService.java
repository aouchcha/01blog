package _blog.backend.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import _blog.backend.Entitys.Interactions.Reactions.Like;
import _blog.backend.Entitys.Notifications.NotificationDTO;
import _blog.backend.Entitys.Notifications.NotificationEntity;
import _blog.backend.Entitys.User.User;
import _blog.backend.Repos.NotificationRepository;
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
    public void sendNotification(User recipient, User creator) {
        final NotificationDTO data = SaveNotif(recipient, creator);

        SseEmitter emitter = emitters.get(recipient.getId());
        if (emitter != null)
            sendEvent(emitter, data);
    }

    @Transactional
    public NotificationDTO SaveNotif(User recipient, User creator) {
        NotificationEntity notif = new NotificationEntity();
        notif.setRecipient(recipient);
        notif.setCreator(creator);
        NotificationEntity saved = repo.save(notif);

        int count = repo.countByRecipient_IdAndSeenFalse(recipient.getId());
        NotificationDTO n = new NotificationDTO(saved.getId(), saved.getCreator().getUsername(), count);
        return n;
    }

    private void sendEvent(SseEmitter emitter, NotificationDTO notif) {
        try {
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(notif.getId()))
                    .name("notification")
                    .data(Map.of("message", notif.getCreatorUsername() + " publish a post", "count", notif.getCount())));
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
}
