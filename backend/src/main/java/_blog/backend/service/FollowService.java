package _blog.backend.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import _blog.backend.Entitys.Interactions.Follow.Follow;
import _blog.backend.Entitys.Interactions.Follow.FollowRequest;
import _blog.backend.Entitys.User.User;
import _blog.backend.Repos.FollowRepositry;
import _blog.backend.Repos.UserRepository;
import _blog.backend.helpers.JwtUtil;

@Service
public class FollowService {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowRepositry followRepositry;

    public ResponseEntity<?> follow(FollowRequest followRequest, String token) {
        final String username = jwtUtil.getUsername(token);
        String status = "";
        if (!userRepository.existsByUsername(username) || !userRepository.existsById(followRequest.getFollowed_id())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "one of the users isn't valid"));
        }
        final User follower = userRepository.findByUsername(username);
        final User followed = userRepository.findById(followRequest.getFollowed_id()).get();
        if (!followRepositry.existsByFollower_IdAndFollowed_Id(follower.getId(), followed.getId())) {
            status = "follow";
            Follow f = new Follow();
            f.setFollowed(followed);
            f.setFollower(follower);

            followRepositry.save(f);
        } else {
            status = "unffolow";
            Follow f = followRepositry.findByFollower_IdAndFollowed_Id(follower.getId(), followed.getId());
            followRepositry.delete(f);
        }
        return ResponseEntity.ok().body(Map.of("message", status + " applied"));
    }
}
