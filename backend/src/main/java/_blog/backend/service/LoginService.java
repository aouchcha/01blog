package _blog.backend.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;

// import java.util.Optional;

// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import _blog.backend.Entitys.User.User;
import _blog.backend.Entitys.User.LoginRequest;
import _blog.backend.Repos.UserRepository;
import _blog.backend.helpers.JwtUtil;
import _blog.backend.helpers.PasswordUtils;
import jakarta.transaction.Transactional;

@Service
@Transactional

public class LoginService {
    @Autowired
    private UserRepository userRepositry;
    @Autowired
    private JwtUtil jwtUtils;

    // @Autowired
    // private PasswordUtils passwordUtils;

    // public LoginService(UserRepository repo, JwtUtil jwtUtils) {
    //     this.userRepositry = repo;
    //     this.jwtUtils = jwtUtils;
    // }

    public ResponseEntity<?> signin(LoginRequest request) {
        // System.err.println(request.getUsername());
        // System.err.println(request.getPassword());
        // Optional<User> MaybeUser = userRepositry.findByUsername;
        if (!userRepositry.existsByUsername(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("incorrect username");
        }
        User u = userRepositry.findByUsername(request.getUsername());
        if (!PasswordUtils.checkPassword(request.getPassword(), u.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("incorrect password");
        }
        String token = jwtUtils.generateToken(u.getUsername(),u.getRole());
        return ResponseEntity.ok(Map.of("token", token, "user", u));
    }
}
