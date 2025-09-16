package _blog.backend.Entitys.User;

// import jakarta.persistence.Entity;

// import org.springframework.stereotype.Component;

// import com.fasterxml.jackson.annotation.JsonProperty;

// @Component
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
