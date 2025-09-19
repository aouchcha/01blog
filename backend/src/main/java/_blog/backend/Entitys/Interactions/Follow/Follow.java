package _blog.backend.Entitys.Interactions.Follow;

import _blog.backend.Entitys.User.User;
// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
import jakarta.persistence.*;

@Entity
@Table(name = "follow")
public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment in PostgreSQL
    @Column(nullable = false, unique = true)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "followed_id", nullable = false)
    private User followed;

    @ManyToOne
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setFollower(User follower) {
        this.follower = follower;
    }

    public User getFollower() {
        return follower;
    }

    public void setFollowed(User followed_id) {
        this.followed = followed_id;
    }

    public User getFollowed() {
        return followed;
    }

}
