package _blog.backend.service;

import java.util.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import _blog.backend.Entitys.User.User;
import _blog.backend.Entitys.User.LoginRequest;
import _blog.backend.Repos.UserRepository;
import _blog.backend.helpers.JwtUtil;
import _blog.backend.helpers.PasswordUtils;
import jakarta.transaction.Transactional;
import _blog.backend.Entitys.User.UserResponse;

@Service
@Transactional
public class LoginService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtils;
    private final RateLimiterService rateLimiterService;

    public LoginService(UserRepository userRepository, JwtUtil jwtUtil, RateLimiterService rateLimiterService) {
        this.jwtUtils = jwtUtil;
        this.userRepository = userRepository;
        this.rateLimiterService = rateLimiterService;
    }

    public ResponseEntity<?> signin(LoginRequest request) {

        User user = null; 

         if (!rateLimiterService.isAllowed(request.getUsername())) {
            return ResponseEntity.status(429).body(Map.of("error", "Rate limit exceeded. Try again later."));
        }

        try {
            user = userRepository.findByUsername(request.getUsername());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Ivalid Cridential"));
            }
    
            if (!PasswordUtils.checkPassword(request.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid Cridential"));
            }
    
            if (user.getisbaned()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You are banned"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }

        String token = jwtUtils.generateToken(user.getUsername(), user.getRole());
        UserResponse UserDto = new UserResponse();
        UserDto.setEmail(user.getEmail());
        UserDto.setFollow(user.getFollow());
        UserDto.setId(user.getId());
        UserDto.setRole(user.getRole());
        UserDto.setUsername(user.getUsername());
        return ResponseEntity.ok(Map.of("token", token, "user", UserDto, "message", "Login Successful"));
    }
}

