package _blog.backend.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import _blog.backend.Entitys.Post.PostRequst;
import _blog.backend.Repos.PostRepository;
import _blog.backend.Repos.UserRepository;
import _blog.backend.helpers.JwtUtil;
import _blog.backend.Entitys.Post.Post;
import _blog.backend.Entitys.User.User;

@Service
public class CreatePostService {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    public ResponseEntity<?> create(PostRequst postRequst, String token) {
        // System.err.println(header.getHeader());
        // jwtUtil.validateToken(header.getJwt())
        System.err.println("|||||||||||||||||||||||||||||||||||||||||||||  " + token);
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message","your token isn't valid"));
        }
        final String username = jwtUtil.getUsername(token);
        if (postRequst.getDescription().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message","the description field should not be empty"));
        }
        
        if (postRequst.getDescription().length() > 250) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message","the description should not be more than 250 letter"));
        }
        final String description = postRequst.getDescription();

        if (!userRepository.existsByUsername(username)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message","the user is not valid"));
        }
        User u = userRepository.findByUsername(username);
        Post newpost = new Post();
        newpost.setDescription(description);
        newpost.setCreatedAt(LocalDateTime.now());
        newpost.setUser(u);

        postRepository.save(newpost);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message","post created with success"));
    }
}
