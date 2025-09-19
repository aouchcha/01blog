package _blog.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.util.*;

import _blog.backend.Entitys.User.User;
import _blog.backend.Repos.UserRepository;

import _blog.backend.helpers.JwtUtil;
@Service
@Transactional
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;
    
    public ResponseEntity<?> getData(String token) {
        final String username = jwtUtil.getUsername(token);
        if (!userRepository.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "invalide user"));
        }
        User u = userRepository.findByUsername(username);
        return ResponseEntity.ok().body(Map.of("me",u));
    }

    public ResponseEntity<?> getUsers(String token) {
        final String username = jwtUtil.getUsername(token);
        if (!userRepository.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "invalide user"));
        }
        List<User> users = userRepository.findByUsernameNot(username);
        return ResponseEntity.ok().body(Map.of("users",users));
    }
}
