package _blog.backend.Entitys.User;

public class RegisterRequest {

    private String username;
    private String email;

    private String password;

    public String getUsername() {
        return this.username;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }
}
