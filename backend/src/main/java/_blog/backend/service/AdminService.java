package _blog.backend.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import _blog.backend.Repos.UserRepository;
import jakarta.transaction.Transactional;

@Service
@Transactional

public class AdminService {
    @Autowired
    private UserRepository userRepository;
    public ResponseEntity<?> getBoard(String token) {
        List<?> users= userRepository.findUsersStates();
        return ResponseEntity.ok().body(Map.of("users", users));
    }
}
