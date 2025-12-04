package _blog.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
import jakarta.transaction.Transactional;

@Service
@Transactional
public class ReactionService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FollowRepositry followRepositry;

    @Autowired
    private RateLimiterService rateLimiterService;
    
    @PreAuthorize("hasRole('User')")
    public ResponseEntity<?> react(LikeRequest likeRequest, String token) {
        final String username = jwtUtil.getUsername(token);

        if (!rateLimiterService.isAllowed(username)) {
            return ResponseEntity.status(429).body(Map.of("message", "Rate limit exceeded. Try again later."));
        }

        final User me = userRepository.findByUsername(username);

        if (me == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "the user is not valid"));
        }

        if (me.getisbaned()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You are banned"));
        }

        final Post p = postRepository.findById(likeRequest.getPost_id()).orElse(null);

        if (p == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "the post is not valid"));
        }

        if (p.getIsHidden()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You cannot react to a hidden post"));
        }

        // final Long post_id = likeRequest.getPost_id();
        // if (!userRepository.existsByUsername(username)) {
        //     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "user not found"));
        // }
        // if (!postRepository.existsById(post_id)) {
        //     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "post not found"));
        // }

        ReactionSaveResult rr = SaveReactEntity(likeRequest.getPost_id(), username);

        for (Follow f : rr.followers) {
            notificationService.sendReaction(f.getFollower().getId(), rr.l);
        }
        notificationService.sendReaction(rr.p.getUser().getId(), rr.l);
        return ResponseEntity.ok().body(Map.of("message", "post " + rr.status + " with success", "post", rr.p));
    }

    @Transactional
    public ReactionSaveResult SaveReactEntity(Long post_id, String username) {
        String status = "";

        final Optional<Post> p = postRepository.findById(post_id);
        final User u = userRepository.findByUsername(username);
        Like l = reactionRepository.findByPost_IdAndUser_Username(post_id, username);

        // List<Like> existingReactions = reactionRepository.findAllByPost_IdAndUser_Username(post_id, username);

        if (l == null) {
            status = "Liked";
            l = new Like();
            l.setUser(u);
            l.setPost(p.get());
            reactionRepository.save(l);
        } else {
            status = "UnLiked";
            reactionRepository.delete(l);
            // l = existingReactions.get(0);
            // reactionRepository.deleteAll(existingReactions);
        }

        p.get().setLikeCount(reactionRepository.countByPost_id(post_id));

        List<Follow> followers = followRepositry.findByFollowed_Id(p.get().getUser().getId());

        return new ReactionSaveResult(l, followers, status, p.get());
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
