package _blog.backend.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import _blog.backend.Entitys.Comment.Comment;
import _blog.backend.Entitys.Comment.CommentRequest;
import _blog.backend.Entitys.Post.Post;
import _blog.backend.Entitys.User.User;
import _blog.backend.Repos.CommentRepository;
import _blog.backend.Repos.PostRepository;
import _blog.backend.Repos.UserRepository;
import _blog.backend.helpers.JwtUtil;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class CommentsService {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    public ResponseEntity<?> create(CommentRequest request, String token) {
        final String username = jwtUtil.getUsername(token);
        final Long post_id = request.getPost_id();
        if (!userRepository.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "invalid user"));
        }

        if (request.getContent().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "comment content is mendatory"));
        }

        if (request.getContent().trim().length() > 250) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "comment content should be less than 250 letter"));
        }

        if (!postRepository.existsById(post_id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "invalid post_id"));
        }
        final User u = userRepository.findByUsername(username);
        final Optional<Post> p = postRepository.findById(post_id);
        Comment c = new Comment();
        c.setContent(request.getContent());
        c.setCreatedAt(LocalDateTime.now());
        c.setUser(u);
        c.setPost(p.get());
        commentRepository.save(c);
        System.out.println("count  "+commentRepository.countByPost_id(post_id));
        p.get().setCommentsCount(commentRepository.countByPost_id(post_id));
        // p.get().setMedia("http://localhost:8080/uploads/" + p.get().getMedia());
        // p.get().setCommentsCount(commentRepository.countByPost_id(post_id));   

        return ResponseEntity.ok(Map.of("message", "comment added succesfuly", "post", p.get()));
    }
}
