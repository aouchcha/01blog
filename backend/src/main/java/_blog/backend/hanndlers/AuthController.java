package _blog.backend.hanndlers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import _blog.backend.Entitys.User.LoginRequest;
import _blog.backend.Entitys.User.RegisterRequest;
import _blog.backend.helpers.JwtUtil;
import _blog.backend.service.LoginService;
import _blog.backend.service.RegisterService;

@RestController
@CrossOrigin(origins = { "http://localhost:4200" })
@RequestMapping("/api")
public class AuthController {

    private final JwtUtil jwtUtil;

    private final RegisterService registerservice;

    private final LoginService loginService;

    public AuthController(JwtUtil jwtUtil, RegisterService registerService, LoginService loginService) {
        this.jwtUtil = jwtUtil;
        this.registerservice = registerService;
        this.loginService = loginService;
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer ", "");
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid token", "valid", false));
        }
        return ResponseEntity.ok(Map.of("valid", true));
    }

    @PostMapping("/register")
    public ResponseEntity<?> create(@RequestBody RegisterRequest registerRequest) {
        return registerservice.register(registerRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<?> signin(@RequestBody LoginRequest loginRequest) {
        return loginService.signin(loginRequest);
    }
}
