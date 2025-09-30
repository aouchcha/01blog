package _blog.backend.hanndlers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ResponseEntity<?> getPosts(@RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "invalid token"));
        }
        return postsService.getPosts(token);
    }

    @GetMapping("/{post_id}")
    public ResponseEntity<?> getSinglePost(
            @PathVariable Long post_id,
            @RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "invalid token"));
        }
        return postsService.getSinglePost(post_id, token);
    }

    @PostMapping
    public ResponseEntity<?> CreatePost(@ModelAttribute PostRequst postRequst,
            @RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "invalid token"));
        }
        return createPostService.create(postRequst, token);
    }

    @DeleteMapping("/{post_id}")
    public ResponseEntity<?> deletePost(
            @PathVariable Long post_id,
            @RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer ", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "invalid token"));
        }
        return postsService.delete(post_id);
    }
}
