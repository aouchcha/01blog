package _blog.backend.Entitys.User;

public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private boolean follow;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setFollow(boolean follow) {
        this.follow = follow;
    }

    public boolean getFollow() {
        return follow;
    }

}
