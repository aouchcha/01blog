package _blog.backend.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import _blog.backend.Entitys.Comment.Comment;
import _blog.backend.Entitys.Comment.CommentRequest;
import _blog.backend.Entitys.Post.Post;
import _blog.backend.Entitys.User.User;
import _blog.backend.Repos.CommentRepository;
import _blog.backend.Repos.PostRepository;
import _blog.backend.Repos.UserRepository;
import _blog.backend.helpers.ContextHelpers;

@Service
public class CommentsService {
    private final ContextHelpers contextHelpers;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final RateLimiterService rateLimiterService;

    public CommentsService(ContextHelpers contextHelpers, UserRepository userRepository,
            PostRepository postRepository, CommentRepository commentRepository, RateLimiterService rateLimiterService) {
        this.contextHelpers = contextHelpers;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.rateLimiterService = rateLimiterService;
    }

    @PreAuthorize("hasRole('User')")
    public ResponseEntity<?> create(CommentRequest request) {

        final String username = contextHelpers.getUsername();

        if (!rateLimiterService.isAllowed(username)) {
            return ResponseEntity.status(429).body(Map.of("error", "Rate limit exceeded. Try again later."));
        }

        final User u = userRepository.findByUsername(username);

        if (u == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "invalid user"));
        }

        if (u.getisbaned()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You are banned"));
        }

        if (request.getContent().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "comment content is mandatory"));
        }

        if (request.getContent().trim().length() > 100) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "comment content should be less than 250 letters"));
        }

        Post p = postRepository.findById(request.getPost_id()).orElse(null);

        if (p == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "invalid post_id"));
        }

        Comment c = new Comment();
        c.setContent(request.getContent());
        c.setCreatedAt(LocalDateTime.now());
        c.setUser(u);
        c.setPost(p);
        commentRepository.save(c);

        p.setCommentsCount(p.getCommentsCount() + 1);
        postRepository.save(p);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "comment added successfully", "post", p));
    }

    @PreAuthorize("hasRole('User')")
    public ResponseEntity<?> delete(Long comment_id) {

        final String username = contextHelpers.getUsername();
        
        if (!rateLimiterService.isAllowed(username)) {
            return ResponseEntity.status(429).body(Map.of("error", "Rate limit exceeded. Try again later."));
        }

        final User u = userRepository.findByUsername(username);

        if (u == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "invalid user"));
        }

        if (u.getisbaned()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You are banned"));
        }

        final Comment comment = commentRepository.findById(comment_id).orElse(null);

        if (comment == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "comment doesn't removed"));
        }

        Post p = postRepository.findById(comment.getPost().getId()).orElse(null);

        if (p == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "comment doesn't removed"));
        }

        p.setCommentsCount(p.getCommentsCount() - 1);

        postRepository.save(p);
        commentRepository.delete(comment);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "comment removed with success"));
    }
}
