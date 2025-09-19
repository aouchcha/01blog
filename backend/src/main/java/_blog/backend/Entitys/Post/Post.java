package _blog.backend.Entitys.Post;

import java.time.LocalDateTime;

import jakarta.annotation.Nonnull;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import _blog.backend.Entitys.Interactions.Reactions.Like;
import _blog.backend.Entitys.User.User;

import java.util.*;

@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment in PostgreSQL
    @Column(unique = true, nullable = false)
    @Nonnull
    private Long id;

    @Column(nullable = false)
    private String description;

    private String media;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Long LikeCount = 0L;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes;

    public Post() {};


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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        System.err.println("SETMEDIA ====================>   "+ media);
        this.media = media;
    }

    public Long getLikeCount() {
        return LikeCount;
    }

    public void setLikeCount(Long likeCount) {
        LikeCount = likeCount;
    }
}
