package _blog.backend.service;

import java.time.LocalDateTime;
// import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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
    private final Map<Long, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter connect(Long userId, String lastEventIdHeader) {
        SseEmitter emitter = new SseEmitter(10 * 60 * 1000L);
        if (emitters.containsKey(userId)) {
            emitters.get(userId).add(emitter);
        }else {
            Set<SseEmitter> list = new HashSet<>();
            list.add(emitter);
            emitters.put(userId, list);
        }

        emitter.onCompletion(() -> {
            System.out.println("complete");
            emitters.remove(userId);
            // System.out.println("Siiiiiiiiiiiiiiiiiiiiiiiiiiiize comp " + emitters.size());
        });
        emitter.onTimeout(() -> {
            System.out.println("timeout");

            emitters.remove(userId);
            // System.out.println("Siiiiiiiiiiiiiiiiiiiiiiiiiiiize timeout " + emitters.size());
        });
        emitter.onError((e) -> {
            System.out.println("error");
            emitters.remove(userId);
            // System.out.println("Siiiiiiiiiiiiiiiiiiiiiiiiiiiize Err " + emitters.size());
        });
        System.out.println("Siiiiiiiiiiiiiiiiiiiiiiiiiz   =    " + emitters.get(userId).size());
        return emitter;
    }

    // @Async
    public void sendNotification(User recipient, User creator, Post p) {
        final NotificationDTO data = SaveNotif(recipient, creator, p);

        Set<SseEmitter> emitter = emitters.get(recipient.getId());
        if (emitter != null) {
            for( SseEmitter e : emitter) {
                sendEvent(e, data);
            }
        }
    }

    @Transactional
    public NotificationDTO SaveNotif(User recipient, User creator, Post p) {
        NotificationEntity notif = new NotificationEntity();
        notif.setRecipient(recipient);
        notif.setCreator(creator);
        notif.setPost(p);
        NotificationEntity saved = repo.save(notif);

        int count = repo.countByRecipient_IdAndSeenFalse(recipient.getId());
        NotificationDTO n = new NotificationDTO(saved.getId(), saved.getCreator().getUsername(), count,
                saved.getCreatedAt(), saved.isSeen());
        return n;
    }

    private void sendEvent(SseEmitter emitter, NotificationDTO notif) {
        try {
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(notif.getId()))
                    .name("notification")
                    .data(Map.of("message", String.format("%s publish a post", notif.getCreatorUsername()), "count",
                            notif.getCount())));
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    public void sendReaction(Long UserId, Like l) {
        Set<SseEmitter> emitter = emitters.get(UserId);
        if (emitter != null) {
            for (SseEmitter em : emitter) {
                try {
                    em.send(SseEmitter.event()
                            .id(String.valueOf(l.getId()))
                            .name("reaction")
                            .data(Map.of("post", l.getPost())));
                } catch (Exception e) {
                    em.completeWithError(e);
                }
            }
        }
    }

    // public void disconnect(Long userId) {
    //     Set<SseEmitter> emitter = emitters.remove(userId);
    //     for (SseEmitter s : emitter) {
    //         if (s != null) {
    //             s.complete();
    //         }
    //     }
    // }

    @Autowired
    UserRepository userRepository;

    public ResponseEntity<?> getNotifs(String username, LocalDateTime lastDate, Long lastId) {
        User u = userRepository.findByUsername(username);
        if (u == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Ivalid Cridential"));
        }
        List<NotificationEntity> notifs;
        PageRequest limit = PageRequest.of(0, 20);

        if (lastDate == null || lastId == null) {
            notifs = repo.findByRecipientIdOrderByCreatedAtDescIdDesc(
                    u.getId(),
                    limit);
        } else {
            notifs = repo.findByRecipientIdAndCreatedAtLessThanOrCreatedAtEqualsAndIdLessThanOrderByCreatedAtDescIdDesc(
                    u.getId(),
                    lastDate,
                    lastId,
                    limit);
        }
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
