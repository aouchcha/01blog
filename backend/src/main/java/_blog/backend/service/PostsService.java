package _blog.backend.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import _blog.backend.Repos.CommentRepository;
import _blog.backend.Repos.PostRepository;
import _blog.backend.Repos.UserRepository;
import _blog.backend.Entitys.Comment.Comment;
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

    @Autowired
    private CommentRepository commentRepository;

    public ResponseEntity<?> getPosts(String token) {
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message","your token isn't valid"));
        }
        final String username = jwtUtil.getUsername(token);

        if (!userRepository.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "the user is not valid"));
        }

        List<Post> posts = postRepository.findAllPostsByUserAndFollowedUsers(userRepository.findIdByUsername(username));
    
        return ResponseEntity.ok().body(Map.of("posts", posts));
    }

    public ResponseEntity<?> getSinglePost(Long post_id, String token) {
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message","your token isn't valid"));
        }

        final String username = jwtUtil.getUsername(token);

        if (!userRepository.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "the user is not valid"));
        }

        Optional<Post> p = postRepository.findById(post_id);
    
        List<Comment> comments = commentRepository.findAllByPost_id(post_id);
       return ResponseEntity.ok().body(Map.of("post", p.get(), "comments", comments));

    }

    public ResponseEntity<?> delete(Long post_id) {
        if (!postRepository.existsById(post_id)) {
            return ResponseEntity.badRequest().body(null);
        }
        Optional<Post> p = postRepository.findById(post_id);
        postRepository.delete(p.get());
        return ResponseEntity.ok().body(Map.of("message", "post removed"));
    }
}
