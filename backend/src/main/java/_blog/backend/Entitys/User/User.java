package _blog.backend.Entitys.User;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import _blog.backend.Entitys.Comment.Comment;
import _blog.backend.Entitys.Interactions.Follow.Follow;
import _blog.backend.Entitys.Interactions.Reactions.Like;
import _blog.backend.Entitys.Notifications.NotificationEntity;
import _blog.backend.Entitys.Post.Post;
import _blog.backend.Entitys.Report.ReportEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(nullable = false)
    private Role role;

    private boolean follow;

    private boolean isbaned;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("user")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "repported", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("repported")
    private List<ReportEntity> reports_against_me_list = new ArrayList<>();

    @OneToMany(mappedBy = "repporter", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("repporter")
    private List<ReportEntity> reports_from_me = new ArrayList<>();

    @OneToMany(mappedBy = "followed", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("followed")
    private List<Follow> followers_list = new ArrayList<>();

    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("follower")
    private List<Follow> followeds_list = new ArrayList<>();

    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("creator")
    private List<NotificationEntity> created_byme_list = new ArrayList<>();

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("recipient")
    private List<NotificationEntity> Follows_Forme_list = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("user")
    private List<Like> Like_List = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("user")
    private List<Comment> Comments_list = new ArrayList<>();

    public User() {
    }

    public User(String username, String email, String password, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return this.role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean getFollow() {
        return this.follow;
    }

    public void setFollow(boolean follow) {
        this.follow = follow;
    }

    public boolean getisbaned() {
        return this.isbaned;
    }

    public void setisbaned(boolean isbaned) {
        this.isbaned = isbaned;
    }
}
