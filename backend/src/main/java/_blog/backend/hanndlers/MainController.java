package _blog.backend.hanndlers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import _blog.backend.service.RegisterService;
import _blog.backend.service.LoginService;
import _blog.backend.service.AdminService;
import _blog.backend.service.CommentsService;
import _blog.backend.service.FollowService;
import _blog.backend.service.ReactionService;
import _blog.backend.service.UserService;
import _blog.backend.Entitys.User.RegisterRequest;
import _blog.backend.Entitys.User.ReportRequest;
import _blog.backend.Entitys.User.LoginRequest;
import _blog.backend.Entitys.Comment.CommentRequest;
import _blog.backend.Entitys.Interactions.Follow.FollowRequest;
import _blog.backend.Entitys.Interactions.Reactions.LikeRequest;

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

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(@RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "invalid token"));
        }
        return userService.getUsers(token);
    }

    @Autowired
    private ReactionService reactionService;

    @PostMapping("/react")
    public ResponseEntity<?> ReactToPost(@RequestBody LikeRequest likeRequest,
            @RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "invalid token"));
        }
        return reactionService.react(likeRequest, token);
    }

    @Autowired
    private FollowService followService;

    @PostMapping("/follow")
    public ResponseEntity<?> Follow(@RequestBody FollowRequest followRequest,
            @RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "invalid token"));
        }
        return followService.follow(followRequest, token);
    }

    @Autowired
    private CommentsService commentsService;

    @PostMapping("/comment")
    public ResponseEntity<?> CreateComments(@RequestBody CommentRequest commentRequest,
            @RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "invalid token"));
        }
        return commentsService.create(commentRequest, token);
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<?> getProfile(@PathVariable String username, @RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "invalid token"));
        }
        return userService.getUserProfile(username, token);
    }

    @DeleteMapping("/user/{username}")
    public ResponseEntity<?> RemoveUser(@PathVariable String username, @RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "invalid token"));
        }
        return userService.Remove(username, token);
    }

    @PostMapping("/report")
    public ResponseEntity<?> Report(@RequestBody ReportRequest reportRequest,
            @RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "invalid token"));
        }
        return userService.report(reportRequest, token);
    }

    @Autowired
    private AdminService adminService;
    @GetMapping("/admin")
    public ResponseEntity<?> AdminBoardContent(@RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer", "");
        if (!jwtUtil.validateToken(token) || !jwtUtil.getRole(token).equals("Admin")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "invalid token"));
        }
        return adminService.getBoard(token);
    }

    @GetMapping("/admin/reports")
    public ResponseEntity<?> LoadReports(@RequestHeader("Authorization") String header) {
         String token = header.replace("Bearer", "");
        if (!jwtUtil.validateToken(token) || !jwtUtil.getRole(token).equals("Admin")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "invalid token"));
        }
        return adminService.loadReports();
    }
}
