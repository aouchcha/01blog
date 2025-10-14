package _blog.backend.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import _blog.backend.Entitys.Interactions.Follow.Follow;
import _blog.backend.Entitys.Interactions.Follow.FollowRequest;
import _blog.backend.Entitys.User.User;
import _blog.backend.Repos.FollowRepositry;
import _blog.backend.Repos.UserRepository;
import _blog.backend.helpers.JwtUtil;

@Service
@Transactional
public class FollowService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowRepositry followRepositry;

    public ResponseEntity<?> follow(FollowRequest followRequest, String token) {
        String username = jwtUtil.getUsername(token);

        User follower = userRepository.findByUsername(username);
        User followed = userRepository.findById(followRequest.getFollowed_id()).orElse(null);

        if (follower == null || followed == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "one of the users isn't valid"));
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

        return ResponseEntity.ok(Map.of("message", status + " applied"));
    }
}
