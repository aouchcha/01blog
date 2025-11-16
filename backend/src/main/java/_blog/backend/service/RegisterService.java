package _blog.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import _blog.backend.Entitys.User.RegisterRequest;
import _blog.backend.Entitys.User.Role;
import _blog.backend.Entitys.User.User;
import _blog.backend.Repos.UserRepository;
import _blog.backend.helpers.PasswordUtils;
import jakarta.transaction.Transactional;

@Service
@Transactional

public class RegisterService {
    private final UserRepository userRepositry;
    public RegisterService(UserRepository repo) { this.userRepositry = repo; }
    public ResponseEntity<?> register(RegisterRequest request) {
        if (request.getUsername().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The username is required");
        }

        if (!isValidName(request.getUsername().trim())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The username is malformed");
        }
        if (request.getEmail().trim().isEmpty() || !isValidEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The email is required or malformed");
        }
        if (request.getPassword().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The password is required");
        }

        if (userRepositry.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The email is already present");
        }

         if (userRepositry.existsByUsername(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The username is already present");
        }

        try {
            String hashedPassword = PasswordUtils.hashPassword(request.getPassword());
            User u = new User(request.getUsername(), request.getEmail(), hashedPassword, Role.User);
            userRepositry.save(u);
            return ResponseEntity.status(HttpStatus.CREATED).body(u);
        }catch(Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
        }
    }

    public boolean isValidEmail(String email) {
        String emailRegex = "[a-zA-W0-9_-]+@[a-zA-Z]+.[a-zA-Z]+";
        return email.matches(emailRegex);
    }

    public boolean isValidName(String username) {
        String UsernameRegex = "[a-zA-Z]+";
        return username.matches(UsernameRegex);
    }
}
