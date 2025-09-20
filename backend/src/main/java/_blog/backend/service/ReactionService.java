package _blog.backend.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import _blog.backend.Entitys.Interactions.Reactions.Like;
import _blog.backend.Entitys.Interactions.Reactions.LikeRequest;
import _blog.backend.Entitys.Post.Post;
import _blog.backend.Entitys.User.User;
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

    public ResponseEntity<?> react(LikeRequest likeRequest, String token) {
        final String username = jwtUtil.getUsername(token);
        final Long post_id = likeRequest.getPost_id();
        String status = "";
        if (!userRepository.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "user not found"));
        }
        if (!postRepository.existsById(post_id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "post not found"));
        }
        final Optional<Post> p = postRepository.findById(post_id);
        if (!reactionRepository.existsByPost_IdAndUser_Username(post_id, username)) {
            status = "Liked";
            final Like l = new Like();
            final User u = userRepository.findByUsername(username);
            l.setUser(u);
            l.setPost(p.get());
            reactionRepository.save(l);
        }else {
            status = "UnLiked";
            final Like l = reactionRepository.findByPost_IdAndUser_Username(post_id, username);
            reactionRepository.delete(l);
        }
        p.get().setLikeCount(reactionRepository.countByPost_id(post_id));   
        return ResponseEntity.ok().body(Map.of("message","post "+status+" with success"));
    }
}
