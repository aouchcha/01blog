package _blog.backend.hanndlers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import _blog.backend.service.RegisterService;
import _blog.backend.service.LoginService;
import _blog.backend.service.CreatePostService;
import _blog.backend.service.PostsService;
import _blog.backend.service.UserService;
import _blog.backend.Entitys.User.RegisterRequest;
import _blog.backend.Entitys.User.LoginRequest;
import _blog.backend.Entitys.Post.PostRequst;

import _blog.backend.helpers.JwtUtil;

@RestController
@CrossOrigin(origins = { "http://localhost:4200" })
@RequestMapping("/api")
public class MainController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RegisterService registerservice;

    @PostMapping("/register")
    public ResponseEntity<?> create(@RequestBody RegisterRequest registerRequest) {
        return registerservice.register(registerRequest);
    }

    @Autowired
    private LoginService loginservice;

    @PostMapping("/login")
    public ResponseEntity<?> signin(@RequestBody LoginRequest loginRequest) {
        return loginservice.signin(loginRequest);
    }
    

    @Autowired PostsService postsService;
    @GetMapping("/post")
    public ResponseEntity<?> getPosts(@RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer", "");
        return postsService.getPosts(token);
    }
    
    @Autowired
    private CreatePostService createPostService;
    @PostMapping("/post")
    public ResponseEntity<?> CreatePost(@ModelAttribute PostRequst postRequst, @RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer", "");
        return createPostService.create(postRequst, token);
    }

    @Autowired
    private UserService userService;
    @GetMapping("/me")
    public ResponseEntity<?> getMyData(@RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "invalid token"));
        }
        return userService.getData(token);
    }
}
