package _blog.backend.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
// import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;

import _blog.backend.Repos.CommentRepository;
import _blog.backend.Repos.PostRepository;
import _blog.backend.Repos.UserRepository;
import _blog.backend.Entitys.Comment.Comment;
import _blog.backend.Entitys.Post.Post;
import _blog.backend.Entitys.Post.PostRequst;
// import _blog.backend.Entitys.User.User;
import _blog.backend.helpers.ContextHelpers;
import _blog.backend.helpers.HandleMedia;
// import _blog.backend.helpers.JwtUtil;

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

    @PreAuthorize("hasRole('User')")
    public ResponseEntity<?> getPosts(LocalDateTime lastDate, Long lastId) {
        final String username = contextHelpers.getUsername();
        Long userId = userRepository.findIdByUsername(username);

        // Validation (kept from your code)
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "the user is not valid"));
        }

        List<Post> posts;

        // We enforce a limit of 10
        PageRequest limit = PageRequest.of(0, 10);

        if (lastDate == null || lastId == null) {
            // Case 1: First load (Top 10)
            posts = postRepository.findInitialFeedPosts(userId, limit);
        } else {
            // Case 2: Scrolling (Next 10 after the cursor)
            posts = postRepository.findNextFeedPosts(userId, lastDate, lastId, limit);
        }

        return ResponseEntity.ok().body(Map.of("posts", posts));
    }

    @PreAuthorize("hasRole('User')")
    public ResponseEntity<?> getSinglePost(Long post_id) {
        // final String username = contextHelpers.getUsername();


        Post p = postRepository.findById(post_id).orElse(null);

        if (p == null) {
            return ResponseEntity.badRequest().body(null);
        }

        List<Comment> comments = commentRepository.findAllByPost_id(post_id);

        if (comments == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "the user is not valid"));
        }

        return ResponseEntity.ok().body(Map.of("comments", comments, "post", p));

    }

    public ResponseEntity<?> delete(Long post_id) {

        Post p = postRepository.findById(post_id).orElse(null);

        if (p == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Post doesn't removed"));
        }

        if (p.getMedia() != null) {
            Path path = Paths.get("/home/aouchcha/Desktop/01blog/backend/uploads", p.getMedia());
            try {
                Files.deleteIfExists(path);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
            }
        }

        postRepository.delete(p);

        return ResponseEntity.ok().body(Map.of("message", "post removed", "post", p));
    }

    @Autowired
    private HandleMedia MediaUtils;

    @PreAuthorize("hasRole('User')")
    public ResponseEntity<?> update(Long post_id, PostRequst postRequst, boolean removed) {
        // if (!postRepository.existsById(post_id)) {
        // return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message",
        // "invalid post id"));
        // }
        Post up = postRepository.findById(post_id).orElse(null);

        if (up == null) {
            return ResponseEntity.badRequest().body(null);
        }

        if (postRequst.getTitle().trim().isEmpty() ||
            postRequst.getTitle().length() > 100) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Description is invalid"));
        }

        if (postRequst.getDescription().trim().isEmpty() ||
            postRequst.getDescription().length() > 1000) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Description is invalid"));
        }


        up.setTitle(postRequst.getTitle());
        up.setDescription(postRequst.getDescription());
        if (removed) {
            if (up.getMedia() != null) {
                Path path = Paths.get("/home/aouchcha/Desktop/01blog/backend/uploads", up.getMedia());
                try {
                    Files.deleteIfExists(path);
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
                }
            }
    
            up.setMedia(null);
    
        }
        if (!MediaUtils.save(up, postRequst)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal Server Error"));
        }


        postRepository.save(up);

        return ResponseEntity.ok().body(Map.of("message", "post updated with sucess", "post", up));
    }
}
