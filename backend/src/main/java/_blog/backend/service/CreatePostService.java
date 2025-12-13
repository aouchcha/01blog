package _blog.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import _blog.backend.Entitys.Post.PostRequst;
import _blog.backend.Repos.FollowRepositry;
import _blog.backend.Repos.PostRepository;
import _blog.backend.Repos.UserRepository;
import _blog.backend.Entitys.Post.Post;
import _blog.backend.Entitys.User.User;
import _blog.backend.Entitys.Interactions.Follow.Follow;


import _blog.backend.helpers.ContextHelpers;
import _blog.backend.helpers.HandleMedia;

@Service
public class CreatePostService {
    private final ContextHelpers contextHelpers;
        private final HandleMedia mediaUtils;
        private final UserRepository userRepository;
        private final PostRepository postRepository;
        private final FollowRepositry followRepositry;
        private final NotificationService notificationService;
        private final RateLimiterService rateLimiterService;

        public CreatePostService(ContextHelpers contextHelpers, HandleMedia mediaUtils,
                UserRepository userRepository, PostRepository postRepository,
                FollowRepositry followRepositry, NotificationService notificationService,
                RateLimiterService rateLimiterService) {
            this.contextHelpers = contextHelpers;
            this.mediaUtils = mediaUtils;
            this.userRepository = userRepository;
            this.postRepository = postRepository;
            this.followRepositry = followRepositry;
            this.notificationService = notificationService;
            this.rateLimiterService = rateLimiterService;
        }
    
    @PreAuthorize("hasRole('User')")
    public ResponseEntity<?> create(PostRequst postRequest) {
        final String username = contextHelpers.getUsername();   

         if (!rateLimiterService.isAllowed(username)) {
            return ResponseEntity.status(429).body(Map.of("error", "Rate limit exceeded. Try again later."));
        }

         if (postRequest.getTitle().trim().isEmpty() ||
            postRequest.getTitle().length() > 255) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Title is invalid"));
        }

        if (postRequest.getDescription().trim().isEmpty() ||
            postRequest.getDescription().length() > 1000) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Description is invalid"));
        }

        final User u = userRepository.findByUsername(username);

        if (u == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "the user is not valid"));
        }

        if (u.getisbaned()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You are banned"));
        }

        Post newPost = new Post();
        newPost.setDescription(postRequest.getDescription());
        newPost.setTitle(postRequest.getTitle());
        newPost.setCreatedAt(LocalDateTime.now());
        newPost.setUser(u);

        if (postRequest.getMedia() != null) {
            String contentType = postRequest.getMedia().getContentType();
            if (contentType == null || (!contentType.startsWith("image/") && !contentType.startsWith("video/"))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error","The media should be image or video")); 
            }
        }

        if (!mediaUtils.save(newPost, postRequest)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal Server Error"));
        }
        
        final Post p = postRepository.save(newPost);
    
        List<Follow> followers = followRepositry.findByFollowed_Id(u.getId());
        followers.forEach(f -> notificationService.sendNotification(f.getFollower(), u, p));

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "post created successfully", "newpost", p));
    }

}
