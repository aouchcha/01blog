package _blog.backend.hanndlers;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import _blog.backend.service.FollowService;
import _blog.backend.service.ReactionService;
import _blog.backend.service.UserService;
import _blog.backend.Entitys.User.ReportRequest;
import _blog.backend.Entitys.Interactions.Follow.FollowRequest;
import _blog.backend.Entitys.Interactions.Reactions.LikeRequest;

import _blog.backend.helpers.JwtUtil;

@RestController
@CrossOrigin(origins = { "http://localhost:4200" })
@RequestMapping("/api")
public class UserController {
    private final JwtUtil jwtUtil;

    private final UserService userService;

    private final ReactionService reactionService;
    
    private final FollowService followService;

    public UserController(UserService userService, JwtUtil jwtUtil, ReactionService reactionService, FollowService followService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.followService = followService;
        this.reactionService = reactionService;
    }


    @GetMapping("/me")
    public ResponseEntity<?> getMyData(@RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "invalid token"));
        }
        return userService.getData(token);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(@RequestHeader("Authorization") String header,
            @RequestParam(required = false) Long lastUserId) {
        String token = header.replace("Bearer", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "invalid token"));
        }
        return userService.getUsers(token, lastUserId);
    }


    @PostMapping("/react")
    public ResponseEntity<?> ReactToPost(@RequestBody LikeRequest likeRequest,
            @RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "invalid token"));
        }
        return reactionService.react(likeRequest, token);
    }


    @PostMapping("/follow")
    public ResponseEntity<?> Follow(@RequestBody FollowRequest followRequest,
            @RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "invalid token"));
        }
        return followService.follow(followRequest, token);
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<?> getProfile(@PathVariable String username, @RequestHeader("Authorization") String header,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastDate,
            @RequestParam(required = false) Long lastId) {
        String token = header.replace("Bearer", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid token"));
        }
        return userService.getUserProfile(username, token, lastDate, lastId);
    }

   

    @PostMapping("/report")
    public ResponseEntity<?> Report(@RequestBody ReportRequest reportRequest,
            @RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid token"));
        }
        return userService.report(reportRequest, token);
    }
}
