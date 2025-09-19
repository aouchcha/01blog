package _blog.backend.Entitys.Interactions.Reactions;

import com.fasterxml.jackson.annotation.JsonIgnore;

import _blog.backend.Entitys.Post.Post;
import _blog.backend.Entitys.User.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Likes")
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment in PostgreSQL
    @Column(nullable = false, unique = true)
    private Long id;

    // @Column(nullable = false)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // @Column(nullable = false)
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    private Post post;

    public Long getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public User getUser() {
        return user;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
