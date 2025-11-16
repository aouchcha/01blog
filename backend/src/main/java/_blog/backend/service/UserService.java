package _blog.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import _blog.backend.Entitys.Post.Post;
import _blog.backend.Entitys.User.User;
import _blog.backend.Entitys.Report.ReportEntity;
import _blog.backend.Entitys.User.ReportRequest;
import _blog.backend.Entitys.User.Role;
import _blog.backend.Repos.FollowRepositry;
import _blog.backend.Repos.NotificationRepository;
import _blog.backend.Repos.PostRepository;
import _blog.backend.Repos.ReportRepository;
import _blog.backend.Repos.UserRepository;
import _blog.backend.helpers.JwtUtil;

@Service
// @Transactional
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private FollowRepositry followRepositry;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    public ResponseEntity<?> getData(String token) {
        final String username = jwtUtil.getUsername(token);
        if (!userRepository.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "invalide user"));
        }
        User u = userRepository.findByUsername(username);
        int count = notificationRepository.countByRecipient_IdAndSeenFalse(u.getId());

        return ResponseEntity.ok().body(Map.of("me", u, "notifCount", count));
    }

    public ResponseEntity<?> getUsers(String token) {
        final String username = jwtUtil.getUsername(token);
        User me = userRepository.findByUsername(username);
        if (me == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "invalid user"));
        }
        List<User> users = userRepository.findByUsernameNotAndRoleNot(username, Role.Admin);
        Set<Long> followedUserIds = followRepositry.findFollowedUserIds(me.getId());

        for (User u : users) {
            u.setFollow(followedUserIds.contains(u.getId()));
        }
        users = users.stream()
                .sorted(Comparator.comparing(User::getId))
                .toList();

        return ResponseEntity.ok().body(Map.of("users", users));
    }

    public ResponseEntity<?> getUserProfile(String username, String token) {
        final String myName = jwtUtil.getUsername(token);
        User me = userRepository.findByUsername(myName);
        if (me == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "invalid user"));
        }

        User ProfileInfos = userRepository.findByUsername(username);

        if (ProfileInfos.getRole().equals(Role.Admin)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "you are trying to get sensitive data"));
        }
        if (followRepositry.existsByFollower_IdAndFollowed_Id(me.getId(), ProfileInfos.getId())) {
            ProfileInfos.setFollow(true);
        } else {
            ProfileInfos.setFollow(false);
        }
        List<Post> posts = postRepository.findByUserId(ProfileInfos.getId());

        return ResponseEntity.ok(Map.of("user", ProfileInfos, "posts", posts, "followers",
                followRepositry.countByFollowed_Id(ProfileInfos.getId()), "followings",
                followRepositry.countByFollower_Id(ProfileInfos.getId())));
    }

    @Autowired
    private ReportRepository reportRepository;

    public ResponseEntity<?> report(ReportRequest reportRequest, String token) {

        if (reportRequest.getDiscription().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "No Description Found"));
        }

        final String myName = jwtUtil.getUsername(token);

        if (!userRepository.existsByUsername(myName)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "invalid user"));
        }

        // final String reportted_name =

        if (!userRepository.existsByUsername(reportRequest.getReportted_username())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "invalid user to report"));
        }

        final User me = userRepository.findByUsername(myName);
        final User repported = userRepository.findByUsername(reportRequest.getReportted_username());

        ReportEntity r = new ReportEntity();
        r.setDescription(reportRequest.getDiscription());
        r.setRepporter(me);
        r.setRepported(repported);
        r.setCreatedAt(LocalDateTime.now());
        reportRepository.save(r);
        return ResponseEntity.ok().body(Map.of("message", "reported succesfuuly"));
    }

    public ResponseEntity<?> Remove(String username, String token) {
        if (!userRepository.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "the user u wanna to remove doesnt exist"));
        }

        if (!userRepository.existsByUsername(jwtUtil.getUsername(token))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "you doesnt exist"));
        }

        final User u = userRepository.findByUsername(username);
        userRepository.delete(u);
        return ResponseEntity.ok().body(Map.of("message", "User " + u + " Removed with success"));
    }

    @Transactional
    public ResponseEntity<?> Ban(String username, String token) {
        System.out.println("HAAAAAAAAAAAAAAAAAAAAAANNNNNNNNNNNIIIIIIIIIIIIIII");
        User u = userRepository.findByUsername(username);
        if (u == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "the user u wanna to ban doesnt exist"));
        }
        u.setIsBaned(!u.getIsBaned());
        return ResponseEntity.ok().body(Map.of("message", "User " + u + " Banned with success"));
    }
}
