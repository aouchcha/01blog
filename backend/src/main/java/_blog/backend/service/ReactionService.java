package _blog.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import _blog.backend.Entitys.Interactions.Follow.Follow;
import _blog.backend.Entitys.Interactions.Reactions.Like;
import _blog.backend.Entitys.Interactions.Reactions.LikeRequest;
import _blog.backend.Entitys.Post.Post;
import _blog.backend.Entitys.User.User;
import _blog.backend.Repos.FollowRepositry;
import _blog.backend.Repos.PostRepository;
import _blog.backend.Repos.ReactionRepository;
import _blog.backend.Repos.UserRepository;
import _blog.backend.helpers.JwtUtil;

@Service

public class ReactionService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final ReactionRepository reactionRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;
    private final FollowRepositry followRepositry;
    private final RateLimiterService rateLimiterService;

    public ReactionService(UserRepository userRepository, JwtUtil jwtUtil,
            ReactionRepository reactionRepository, PostRepository postRepository,
            NotificationService notificationService, FollowRepositry followRepositry,
            RateLimiterService rateLimiterService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.reactionRepository = reactionRepository;
        this.postRepository = postRepository;
        this.notificationService = notificationService;
        this.followRepositry = followRepositry;
        this.rateLimiterService = rateLimiterService;
    }

    @PreAuthorize("hasRole('User')")
    public ResponseEntity<?> react(LikeRequest likeRequest, String token) {
        final String username = jwtUtil.getUsername(token);

        if (!rateLimiterService.isAllowed(username)) {
            return ResponseEntity.status(429).body(Map.of("error", "Rate limit exceeded. Try again later."));
        }

        final User me = userRepository.findByUsername(username);

        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "the user is not valid"));
        }

        if (me.getisbaned()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You are banned"));
        }

        Post p = postRepository.findById(likeRequest.getPost_id()).orElse(null);

        if (p == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "the post is not valid"));
        }

        if (p.getIsHidden()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You cannot react to a hidden post"));
        }

        ReactionSaveResult rr = SaveReactEntity(p, me);

        for (Follow f : rr.followers) {
            notificationService.sendReaction(f.getFollower().getId(), rr.l, rr.p);
        }
        notificationService.sendReaction(p.getUser().getId(), rr.l, rr.p);
        return ResponseEntity.ok().body(Map.of("message", "post " + rr.status + " with success", "post", rr.p));
    }

    public ReactionSaveResult SaveReactEntity(Post p, User u) {
        String status = "";

        Like l = reactionRepository.findByPost_IdAndUser_Username(p.getId(), u.getUsername());

        if (l == null) {
            status = "Liked";
            l = new Like();
            l.setUser(u);
            l.setPost(p);
            l = reactionRepository.save(l);
        } else {
            status = "UnLiked";
            reactionRepository.delete(l);
        }
        p.setLikeCount(reactionRepository.countByPost_id(p.getId()));

        p = postRepository.save(p);

        List<Follow> followers = followRepositry.findByFollowed_Id(p.getUser().getId());

        return new ReactionSaveResult(l, followers, status, p);
    }

    private class ReactionSaveResult {
        List<Follow> followers = new ArrayList<>();
        Like l;
        String status;
        Post p;

        public ReactionSaveResult(Like l, List<Follow> followers, String status, Post p) {
            this.l = l;
            this.followers = followers;
            this.status = status;
            this.p = p;
        }

    }
}
