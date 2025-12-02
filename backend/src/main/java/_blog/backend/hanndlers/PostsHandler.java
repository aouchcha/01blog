package _blog.backend.hanndlers;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import _blog.backend.Entitys.Post.PostRequst;
import _blog.backend.helpers.JwtUtil;
import _blog.backend.service.CreatePostService;
import _blog.backend.service.PostsService;

@Controller
@CrossOrigin(origins = { "http://localhost:4200" })
@RequestMapping("/api/post")
public class PostsHandler {
    @Autowired
    private PostsService postsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CreatePostService createPostService;

    @GetMapping
    public ResponseEntity<?> getPosts(@RequestHeader("Authorization") String header,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastDate,
            @RequestParam(required = false) Long lastId) {
        return postsService.getPosts(lastDate, lastId);
    }

    @GetMapping("/{post_id}")
    public ResponseEntity<?> getSinglePost(
            @PathVariable Long post_id,
            @RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid token"));
        }
        return postsService.getSinglePost(post_id);
    }

    @PostMapping
    public ResponseEntity<?> CreatePost(@ModelAttribute PostRequst postRequst,
            @RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid token"));
        }
        return createPostService.create(postRequst);
    }

    @DeleteMapping("/{post_id}")
    public ResponseEntity<?> deletePost(
            @PathVariable Long post_id,
            @RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid token"));
        }
        return postsService.delete(post_id);
    }

    @PutMapping("/{post_id}")
    public ResponseEntity<?> Update(
            @PathVariable Long post_id, @ModelAttribute PostRequst postRequst,
            @RequestHeader("Authorization") String header,
            @RequestParam(required = false) boolean removed) {
        String token = header.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid token"));
        }
        return postsService.update(post_id, postRequst, removed);
    }

    @PatchMapping("/hide/{post_id}")
    public ResponseEntity<?> HidePost(
            @PathVariable Long post_id,
            @RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid token"));
        }
        return postsService.HidePost(post_id); 
    }
}
