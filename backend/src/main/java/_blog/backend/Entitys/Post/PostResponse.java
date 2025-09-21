package _blog.backend.Entitys.Post;

import java.time.LocalDateTime;

import _blog.backend.Entitys.User.UserResponse;

public class PostResponse {
    private Long id;
    private String description;
    private String media;
    private LocalDateTime createdAt;
    private Long LikeCount = 0L;
    private Long CommentsCount = 0L;

    private UserResponse userResponse;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public Long getLikeCount() {
        return LikeCount;
    }

    public void setLikeCount(Long likeCount) {
        LikeCount = likeCount;
    }

    public Long getCommentsCount() {
        return CommentsCount;
    }

    public void setCommentsCount(Long commentsCount) {
        CommentsCount = commentsCount;
    }

    public UserResponse getUserResponse() {
        return userResponse;
    }

    public void setUserResponse(UserResponse userResponse) {
        this.userResponse = userResponse;
    }

}
