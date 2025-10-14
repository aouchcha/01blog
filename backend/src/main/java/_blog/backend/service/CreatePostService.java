package _blog.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import _blog.backend.Entitys.Post.PostRequst;
import _blog.backend.Repos.FollowRepositry;
import _blog.backend.Repos.PostRepository;
import _blog.backend.Repos.UserRepository;
import _blog.backend.Entitys.Post.Post;
import _blog.backend.Entitys.User.User;
import _blog.backend.Entitys.Interactions.Follow.Follow;


import _blog.backend.helpers.JwtUtil;
import _blog.backend.helpers.HandleMedia;

@Service
public class CreatePostService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private HandleMedia mediaUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private FollowRepositry followRepositry;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RateLimiterService rateLimiterService;

    public ResponseEntity<?> create(PostRequst postRequest, String token) {

        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "your token isn't valid"));
        }

        String username = jwtUtil.getUsername(token);

         if (!rateLimiterService.isAllowed(username)) {
            return ResponseEntity.status(429).body(Map.of("message", "Rate limit exceeded. Try again later."));
        }

        if (postRequest.getDescription().trim().isEmpty() ||
            postRequest.getDescription().length() > 250) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Description is invalid"));
        }

        User u = userRepository.findByUsername(username);
        if (u == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "the user is not valid"));
        }

        Post newPost = new Post();
        newPost.setDescription(postRequest.getDescription());
        newPost.setCreatedAt(LocalDateTime.now());
        newPost.setUser(u);

        if (!mediaUtils.save(newPost, postRequest)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Internal Server Error"));
        }
        
        PostCreationResult pr = savePostAndGetFollowers(newPost, u.getId());

        pr.followers.forEach(f -> notificationService.sendNotification(f.getFollower(), u));

        return ResponseEntity.ok(Map.of("message", "post created successfully"));
    }

    @Transactional
    private PostCreationResult savePostAndGetFollowers(Post post, Long userId) {
        postRepository.save(post);
        List<Follow> followers = followRepositry.findByFollowed_Id(userId);
        
        return new PostCreationResult(post, followers);
    }

    private static class PostCreationResult {
        Post post;
        List<Follow> followers;
        
        PostCreationResult(Post post, List<Follow> followers) {
            this.post = post;
            this.followers = followers;
        }
    }
}
