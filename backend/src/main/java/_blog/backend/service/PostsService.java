package _blog.backend.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import _blog.backend.Repos.CommentRepository;
import _blog.backend.Repos.PostRepository;
import _blog.backend.Repos.UserRepository;
import _blog.backend.Entitys.Comment.Comment;
import _blog.backend.Entitys.Post.Post;
import _blog.backend.Entitys.Post.PostRequst;
import _blog.backend.Entitys.User.User;
import _blog.backend.helpers.ContextHelpers;
import _blog.backend.helpers.HandleMedia;
import _blog.backend.Entitys.User.Role;

@Service
public class PostsService {
    private final PostRepository postRepository;
    private final ContextHelpers contextHelpers;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final HandleMedia MediaUtils;

    public PostsService(PostRepository postRepository, ContextHelpers contextHelpers,
            UserRepository userRepository, CommentRepository commentRepository, HandleMedia MediaUtils) {
        this.postRepository = postRepository;
        this.contextHelpers = contextHelpers;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.MediaUtils = MediaUtils;
    }

    public ResponseEntity<?> getPosts(LocalDateTime lastDate, Long lastId) {
        final String username = contextHelpers.getUsername();
        final User me = userRepository.findByUsername(username);

        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "the user is not valid"));
        }

        if (me.getisbaned()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You are banned"));
        }

        List<Post> posts;

        PageRequest limit = PageRequest.of(0, 10);

        if (lastDate == null || lastId == null) {
            posts = postRepository.findInitialFeedPosts(me.getId(), limit);
        } else {
            posts = postRepository.findNextFeedPosts(me.getId(), lastDate, lastId, limit);
        }

        return ResponseEntity.ok().body(Map.of("posts", posts));
    }

    public ResponseEntity<?> getSinglePost(Long post_id) {
        final String username = contextHelpers.getUsername();

        final User me = userRepository.findByUsername(username);

        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "the user is not valid"));
        }

        if (me.getisbaned()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You are banned"));
        }

        final Post p = postRepository.findById(post_id).orElse(null);

        if (p == null) {
            return ResponseEntity.badRequest().body(null);
        }

        if (p.getIsHidden() && !me.getRole().equals(Role.Admin)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "This post is hidden"));
        }

        List<Comment> comments = commentRepository.findAllByPost_id(post_id);

        if (comments == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "the user is not valid"));
        }

        return ResponseEntity.ok().body(Map.of("comments", comments, "post", p));

    }

    public ResponseEntity<?> delete(Long post_id) {

        final User me = userRepository.findByUsername(contextHelpers.getUsername());

        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "the user is not valid"));
        }

        if (me.getisbaned()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You are banned"));
        }

        Post p = postRepository.findById(post_id).orElse(null);

        if (p == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Post doesn't removed"));
        }

        if (p.getUser().getId() != me.getId() && me.getRole().equals(Role.User)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You are not allowed to delete this post"));
        }

        if (p.getMedia() != null) {
            Path path = Paths.get("../backend/uploads", p.getMedia());
            try {
                Files.deleteIfExists(path);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
            }
        }

        postRepository.delete(p);

        return ResponseEntity.ok().body(Map.of("message", "post removed", "post", p));
    }

    @PreAuthorize("hasRole('User')")
    public ResponseEntity<?> update(Long post_id, PostRequst postRequst, boolean removed) {

        final User me = userRepository.findByUsername(contextHelpers.getUsername());

        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "the user is not valid"));
        }

        if (me.getisbaned()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You are banned"));
        }

        Post up = postRepository.findById(post_id).orElse(null);

        if (up == null) {
            return ResponseEntity.badRequest().body(null);
        }

        if (up.getUser().getId() != me.getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You are not allowed to update this post"));
        }

        if (up.getIsHidden()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You are not allowed to update a hidden post"));
        }

        if (postRequst.getTitle().trim().isEmpty() ||
                postRequst.getTitle().length() > 255) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Title is invalid"));
        }

        if (postRequst.getDescription().trim().isEmpty() ||
                postRequst.getDescription().length() > 1000) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Description is invalid"));
        }

        up.setTitle(postRequst.getTitle());
        up.setDescription(postRequst.getDescription());
        if (removed) {
            if (up.getMedia() != null) {
                Path path = Paths.get("../backend/uploads", up.getMedia());
                try {
                    Files.deleteIfExists(path);
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", e.getMessage()));
                }
            }

            up.setMedia(null);

        }
        if (!MediaUtils.save(up, postRequst)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error"));
        }

        postRepository.save(up);

        return ResponseEntity.ok().body(Map.of("message", "post updated with sucess", "post", up));
    }

    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> HidePost(Long post_id) {
        final User me = userRepository.findByUsername(contextHelpers.getUsername());

        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "you doesn't exist"));
        }

        if (!me.getRole().equals(Role.Admin)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "you don't have acess to hide this post"));
        }

        Post p = postRepository.findById(post_id).orElse(null);

        if (p == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Post doesn't exist"));
        }

        p.setIsHidden(true);
        postRepository.save(p);

        return ResponseEntity.ok().body(Map.of("message", "post hidden", "post", p));
    }

    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> UnhidePost(Long post_id) {

        final User me = userRepository.findByUsername(contextHelpers.getUsername());

        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "you doesn't exist"));
        }

        if (!me.getRole().equals(Role.Admin)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "you don't have acess to hide this post"));
        }

        Post p = postRepository.findById(post_id).orElse(null);

        if (p == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Post doesn't exist"));
        }

        p.setIsHidden(false);
        postRepository.save(p);

        return ResponseEntity.ok().body(Map.of("message", "post Unhid", "post", p));
    }
}
