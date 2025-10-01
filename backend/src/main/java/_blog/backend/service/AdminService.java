package _blog.backend.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import _blog.backend.Entitys.User.Role;
import _blog.backend.Repos.UserRepository;
import _blog.backend.helpers.JwtUtil;
import jakarta.transaction.Transactional;

@Service
@Transactional

public class AdminService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    public ResponseEntity<?> getBoard(String token) {
        System.out.println(jwtUtil.getRole(token));
        // if (jwtUtil.getRole(token) != Role.Admin) {

        // }
        List<?> users= userRepository.findUsersStates();
        return ResponseEntity.ok().body(Map.of("users", users));
    }
}
