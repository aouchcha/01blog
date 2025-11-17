package _blog.backend.Entitys.Notifications;

import java.time.LocalDateTime;

public class NotificationDTO {
    private final Long id;
    private final String creatorUsername;
    private final int count;
    private final LocalDateTime createdAt;
    private final Boolean seen;

    public NotificationDTO(Long id, String creatorUsername, int count, LocalDateTime createdAt, Boolean seen) {
        this.id = id;
        this.creatorUsername = creatorUsername;
        this.count = count;
        this.createdAt = createdAt;
        this.seen = seen;
    }

    public Long getId() {
        return id;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }

    public int getCount() {
        return count;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Boolean getSeen() {
        return seen;
    }
}