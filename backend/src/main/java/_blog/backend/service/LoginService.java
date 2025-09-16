package _blog.backend.service;

import java.util.*;

// import java.util.Optional;

// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import _blog.backend.Entitys.User.User;
import _blog.backend.Entitys.User.LoginRequest;
import _blog.backend.Repos.UserRepository;
import _blog.backend.helpers.JwtUtil;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class LoginService {
    // @Autowired
    private final UserRepository userRepositry;
    // @Autowired
    private final JwtUtil jwtUtils;
    public LoginService(UserRepository repo,  JwtUtil jwtUtils) { this.userRepositry = repo; this.jwtUtils = jwtUtils;}
    public ResponseEntity<?> signin(LoginRequest request) {
        System.err.println(request.getUsername());
        System.err.println(request.getPassword());
        // Optional<User> MaybeUser = userRepositry.findByUsername;
        if (userRepositry.existsByUsername(request.getUsername())) {
            User u = userRepositry.findByUsername(request.getUsername());
            if (u.getPassword().equals(request.getPassword())) {
                String token = jwtUtils.generateToken(u.getUsername(), u.getPassword());
                return ResponseEntity.ok(Map.of("token",token));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("incorrect password");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("incorrect username");
    }
}
