package _blog.backend.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import _blog.backend.Repos.PostRepository;
import _blog.backend.Repos.UserRepository;
import _blog.backend.Entitys.Post.Post;
import _blog.backend.helpers.JwtUtil;

@Service
public class PostsService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<?> getPosts(String token) {
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("your token isn't valid");
        }
        final String username = jwtUtil.getUsername(token);

        if (!userRepository.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("the user is not valid");
        }

        List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();
        for (Post p : posts) {
            p.setMedia("http://localhost:8080/uploads/" + p.getMedia());
        }
        return ResponseEntity.ok().body(Map.of("posts", posts));
    }
}
