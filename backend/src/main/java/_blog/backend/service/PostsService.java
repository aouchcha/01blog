package _blog.backend.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import _blog.backend.Repos.CommentRepository;
import _blog.backend.Repos.PostRepository;
import _blog.backend.Repos.UserRepository;
import _blog.backend.Entitys.Comment.Comment;
import _blog.backend.Entitys.Post.Post;
import _blog.backend.Entitys.Post.PostRequst;
import _blog.backend.Entitys.User.User;
import _blog.backend.helpers.ContextHelpers;
import _blog.backend.helpers.HandleMedia;
import _blog.backend.helpers.JwtUtil;

@Service
@Transactional
public class PostsService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    // private JwtUtil jwtUtil;
    private ContextHelpers contextHelpers;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    public ResponseEntity<?> getPosts() {
        // if (!jwtUtil.validateToken(token)) {
        //     return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "your token isn't valid"));
        final String username = contextHelpers.getUsername();
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // System.out.println("\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\Authenticated user: " + auth.getName());
        // System.out.println("\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\Authorities: " + auth.getAuthorities().toString());
        // System.out.println(auth.isAuthenticated());

        // if (!userRepository.existsByUsername("username")) {
        //     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "the user is not valid"));
        // }

        List<Post> posts = postRepository.findAllPostsByUserAndFollowedUsers(userRepository.findIdByUsername(username));

        return ResponseEntity.ok().body(Map.of("posts", posts));
    }

    public ResponseEntity<?> getSinglePost(Long post_id) {
        // if (!jwtUtil.validateToken(token)) {
        //     return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "your token isn't valid"));
        // }

        // final String username = contextHelpers.getUsername();

        // if (!userRepository.existsByUsername(username)) {
        //     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "the user is not valid"));
        // }

        Optional<Post> p = postRepository.findById(post_id);

        List<Comment> comments = commentRepository.findAllByPost_id(post_id);
        return ResponseEntity.ok().body(Map.of("post", p.get(), "comments", comments));

    }

    public ResponseEntity<?> delete(Long post_id) {
        final String username = contextHelpers.getUsername();
        // if (!userRepository.existsByUsername(username)) {
        //     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "invalid user"));
        // }

        final User u = userRepository.findByUsername(username);
        final List<Post> posts = postRepository.findByUserId(u.getId());

        if (!postRepository.existsById(post_id)) {
            return ResponseEntity.badRequest().body(null);
        }

        Optional<Post> p = postRepository.findById(post_id);
        if (p.get().getMedia() != null) {
            Path path = Paths.get("/home/aouchcha/Desktop/01blog/backend/uploads", p.get().getMedia());
            try {
                Files.deleteIfExists(path);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
            }
        } 
        postRepository.delete(p.get());
        return ResponseEntity.ok().body(Map.of("message", "post removed", "posts", posts));
    }

    @Autowired
    private HandleMedia MediaUtils;

    public ResponseEntity<?> update(Long post_id, PostRequst postRequst) {
        if (!postRepository.existsById(post_id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "invalid post id"));
        }
        Optional<Post> up = postRepository.findById(post_id);
        up.get().setDescription(postRequst.getDescription());
        if (up.get().getMedia() != null) {
            Path path = Paths.get("/home/aouchcha/Desktop/01blog/backend/uploads", up.get().getMedia());
            try {
                Files.deleteIfExists(path);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
            }
        } 
        up.get().setMedia(null);
        if (!MediaUtils.save(up.get(), postRequst)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Internal Server Error"));
        }
        postRepository.save(up.get());
        return ResponseEntity.ok().body(Map.of("message", "post updated with sucess", "post", up.get()));
    }
}
