package _blog.backend.service;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import _blog.backend.Entitys.Interactions.Follow.Follow;
import _blog.backend.Entitys.Interactions.Follow.FollowRequest;
import _blog.backend.Entitys.User.Role;
import _blog.backend.Entitys.User.User;
import _blog.backend.Repos.FollowRepositry;
import _blog.backend.Repos.UserRepository;
import _blog.backend.helpers.JwtUtil;

@Service
public class FollowService {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final FollowRepositry followRepositry;
    private final RateLimiterService rateLimiterService;

    public FollowService(JwtUtil jwtUtil, UserRepository userRepository, FollowRepositry followRepositry, RateLimiterService rateLimiterService) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.followRepositry = followRepositry;
        this.rateLimiterService = rateLimiterService;
    }

    @PreAuthorize("hasRole('User')")
    public ResponseEntity<?> follow(FollowRequest followRequest, String token) {
        String username = jwtUtil.getUsername(token);

        if (!rateLimiterService.isAllowed(username)) {
            return ResponseEntity.status(429).body(Map.of("error", "Rate limit exceeded. Try again later."));
        }

        User follower = userRepository.findByUsername(username);

        User followed = userRepository.findById(followRequest.getFollowed_id()).orElse(null);

        if (follower == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "the follower user isn't valid"));
        }

        if (followed == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "the followed user isn't valid"));
        }

        if (followed.getRole().equals(Role.Admin)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "you can follow admin"));
        }

        if (follower.getId().equals(followed.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "you can't follow yourself"));
        }

        if (follower.getisbaned()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You are banned"));
        }

        Follow follow = followRepositry.findByFollower_IdAndFollowed_Id(follower.getId(), followed.getId());
        String status;

        if (follow == null) {
            status = "follow";
            follow = new Follow();
            follow.setFollower(follower);
            follow.setFollowed(followed);
            followRepositry.save(follow);
        } else {
            status = "unfollow";
            followRepositry.delete(follow);
        }

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", status + " applied"));
    }
}
