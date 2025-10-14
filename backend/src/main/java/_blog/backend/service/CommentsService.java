package _blog.backend.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import _blog.backend.Entitys.Comment.Comment;
import _blog.backend.Entitys.Comment.CommentRequest;
import _blog.backend.Entitys.Post.Post;
import _blog.backend.Entitys.User.User;
import _blog.backend.Repos.CommentRepository;
import _blog.backend.Repos.PostRepository;
import _blog.backend.Repos.UserRepository;
import _blog.backend.helpers.JwtUtil;

@Service
public class CommentsService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Transactional
    public ResponseEntity<?> create(CommentRequest request, String token) {

        User u = userRepository.findByUsername(jwtUtil.getUsername(token));
        if (u == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "invalid user"));
        }

        if (request.getContent().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "comment content is mandatory"));
        }

        if (request.getContent().trim().length() > 250) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "comment content should be less than 250 letters"));
        }

        Post p = postRepository.findById(request.getPost_id()).orElse(null);
        if (p == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "invalid post_id"));
        }

        Comment c = new Comment();
        c.setContent(request.getContent());
        c.setCreatedAt(LocalDateTime.now());
        c.setUser(u);
        c.setPost(p);
        commentRepository.save(c);

        p.setCommentsCount(p.getCommentsCount() + 1);
        postRepository.save(p);

        return ResponseEntity.ok(Map.of("message", "comment added successfully", "post", p));
    }
}
