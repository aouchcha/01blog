package _blog.backend.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
// import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public ResponseEntity<?> getPosts() {
        final String username = contextHelpers.getUsername();

        List<Post> posts = postRepository.findAllPostsByUserAndFollowedUsers(userRepository.findIdByUsername(username));

        if (posts == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "the user is not valid"));
        }

        return ResponseEntity.ok().body(Map.of("posts", posts));
    }

    public ResponseEntity<?> getSinglePost(Long post_id) {

        // Post p = postRepository.findById(post_id).orElse(null);

        // if (p == null) {
        // return ResponseEntity.badRequest().body(null);
        // }

        List<Comment> comments = commentRepository.findAllByPost_id(post_id);

        if (comments == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "the user is not valid"));
        }

        return ResponseEntity.ok().body(Map.of("comments", comments));

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

        return ResponseEntity.ok().body(Map.of("message", "post removed"));
    }

    @Autowired
    private HandleMedia MediaUtils;

    public ResponseEntity<?> update(Long post_id, PostRequst postRequst) {
        // if (!postRepository.existsById(post_id)) {
        // return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message",
        // "invalid post id"));
        // }
        Post up = postRepository.findById(post_id).orElse(null);

        if (up == null) {
            return ResponseEntity.badRequest().body(null);
        }

        up.setTitle(postRequst.getTitle());
        up.setDescription(postRequst.getDescription());

        if (up.getMedia() != null) {
            Path path = Paths.get("/home/aouchcha/Desktop/01blog/backend/uploads", up.getMedia());
            try {
                Files.deleteIfExists(path);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
            }
        }

        up.setMedia(null);

        if (!MediaUtils.save(up, postRequst)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal Server Error"));
        }

        postRepository.save(up);
        
        return ResponseEntity.ok().body(Map.of("message", "post updated with sucess", "post", up));
    }
}
