package _blog.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

import _blog.backend.Entitys.Post.PostRequst;
// import _blog.backend.Entitys.Post.PostResponse;
import _blog.backend.Repos.FollowRepositry;
import _blog.backend.Repos.PostRepository;
import _blog.backend.Repos.UserRepository;
import _blog.backend.Entitys.Post.Post;
import _blog.backend.Entitys.User.User;
// import _blog.backend.Entitys.User.UserResponse;
import _blog.backend.Entitys.Interactions.Follow.Follow;


// import _blog.backend.helpers.JwtUtil;
import _blog.backend.helpers.ContextHelpers;
import _blog.backend.helpers.HandleMedia;

@Service
public class CreatePostService {

    @Autowired
    private ContextHelpers contextHelpers;

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
    
    @PreAuthorize("hasRole('User')")
    public ResponseEntity<?> create(PostRequst postRequest) {

        // if (!jwtUtil.validateToken(token)) {
        //     return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        //             .body(Map.of("message", "your token isn't valid"));
        // }

        final String username = contextHelpers.getUsername();   

         if (!rateLimiterService.isAllowed(username)) {
            return ResponseEntity.status(429).body(Map.of("error", "Rate limit exceeded. Try again later."));
        }

         if (postRequest.getTitle().trim().isEmpty() ||
            postRequest.getTitle().length() > 100) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Description is invalid"));
        }

        if (postRequest.getDescription().trim().isEmpty() ||
            postRequest.getDescription().length() > 1000) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Description is invalid"));
        }

        User u = userRepository.findByUsername(username);
        if (u == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "the user is not valid"));
        }

        Post newPost = new Post();
        newPost.setDescription(postRequest.getDescription());
        newPost.setTitle(postRequest.getTitle());
        newPost.setCreatedAt(LocalDateTime.now());
        newPost.setUser(u);

        if (!mediaUtils.save(newPost, postRequest)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error"));
        }
        
        final Post p = postRepository.save(newPost);
    
        List<Follow> followers = followRepositry.findByFollowed_Id(u.getId());
        followers.forEach(f -> notificationService.sendNotification(f.getFollower(), u, p));

        return ResponseEntity.ok(Map.of("message", "post created successfully", "newpost", p));
    }

}
