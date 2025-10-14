package _blog.backend.Entitys.Notifications;

public class NotificationDTO {
    private final Long id;
    private final String creatorUsername;
    private final int count;

    public NotificationDTO(Long id, String creatorUsername, int count) {
        this.id = id;
        this.creatorUsername = creatorUsername;
        this.count = count;
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
}