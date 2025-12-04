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
import jakarta.transaction.Transactional;
import _blog.backend.Entitys.User.UserResponse;

@Service
@Transactional
public class LoginService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtils;

    public ResponseEntity<?> signin(LoginRequest request) {
        // System.out.println(request.getUsername());
        // System.out.println(request.getPassword());
        User user = null; 

        try {
            user = userRepository.findByUsername(request.getUsername());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Ivalid Cridential"));
            }
    
            if (!PasswordUtils.checkPassword(request.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Invalid Cridential"));
            }
    
            if (user.getisbaned()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "You are banned"));
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

