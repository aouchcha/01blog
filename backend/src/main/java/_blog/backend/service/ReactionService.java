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
// @Transactional
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

        Post p = postRepository.findById(likeRequest.getPost_id()).orElse(null);

        if (p == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "the post is not valid"));
        }

        if (p.getIsHidden()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You cannot react to a hidden post"));
        }

        ReactionSaveResult rr = SaveReactEntity(p, me);

        for (Follow f : rr.followers) {
            System.out.println("notifying follower: " + f.getFollower().getUsername());
            notificationService.sendReaction(f.getFollower().getId(), rr.l, rr.p);
        }
        System.out.println("notifying the post owner : " + me.getUsername());
        notificationService.sendReaction(p.getUser().getId(), rr.l, rr.p);
        return ResponseEntity.ok().body(Map.of("message", "post " + rr.status + " with success", "post", rr.p));
    }

    // @Transactional
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
