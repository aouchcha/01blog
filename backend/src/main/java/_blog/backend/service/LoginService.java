package _blog.backend.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import _blog.backend.Entitys.User.User;
import _blog.backend.Entitys.User.LoginRequest;
import _blog.backend.Repos.UserRepository;
import _blog.backend.helpers.JwtUtil;
import _blog.backend.helpers.PasswordUtils;

@Service
public class LoginService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtils;

    public ResponseEntity<?> signin(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "incorrect username"));
        }

        if (!PasswordUtils.checkPassword(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "incorrect password"));
        }

        String token = jwtUtils.generateToken(user.getUsername(), user.getRole());
        return ResponseEntity.ok(Map.of("token", token, "user", user));
    }
}

