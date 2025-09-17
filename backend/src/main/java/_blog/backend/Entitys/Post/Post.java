package _blog.backend.Entitys.Post;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import _blog.backend.Entitys.User.User;

@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment in PostgreSQL
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String description;

    // private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Post() {};


    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    // public void setContent(String content) {
    //     this.content = content;
    // }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // public void setUser(User user) {
    //     this.user = user;
    // }

//     public String getContent() {
//         return content;
//     }
}
