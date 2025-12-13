package _blog.backend.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
public class UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final FollowRepositry followRepositry;
    private final PostRepository postRepository;
    private final NotificationRepository notificationRepository;
    private final ReportRepository reportRepository;
    private final RateLimiterService rateLimiterService;

    public UserService(UserRepository userRepository, JwtUtil jwtUtil, FollowRepositry followRepositry,
            PostRepository postRepository, NotificationRepository notificationRepository,
            ReportRepository reportRepository, RateLimiterService rateLimiterService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.followRepositry = followRepositry;
        this.postRepository = postRepository;
        this.notificationRepository = notificationRepository;
        this.reportRepository = reportRepository;
        this.rateLimiterService = rateLimiterService;
    }

    public ResponseEntity<?> getData(String token) {
        final String username = jwtUtil.getUsername(token);

        final User u = userRepository.findByUsername(username);

        if (u == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalide user"));
        }
        if (u.getisbaned()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You are banned"));
        }
        int count = notificationRepository.countByRecipient_IdAndSeenFalse(u.getId());

        return ResponseEntity.ok().body(Map.of("me", u, "notifCount", count));
    }

    public ResponseEntity<?> getUsers(String token, Long lastUserId) {
        final String username = jwtUtil.getUsername(token);
        User me = userRepository.findByUsername(username);
        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "invalid user"));
        }

        if (me.getisbaned()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You are banned"));
        }

        List<User> users;
        PageRequest limit = PageRequest.of(0, 10);
        if (lastUserId == null) {
            users = userRepository.findByUsernameNotAndRoleNotOrderByIdAsc(
                    username,
                    Role.Admin,
                    limit);
        } else {
            users = userRepository.findByUsernameNotAndRoleNotAndIdGreaterThanOrderByIdAsc(
                    username,
                    Role.Admin,
                    lastUserId,
                    limit);
        }
        Set<Long> followedUserIds = followRepositry.findFollowedUserIds(me.getId());

        for (User u : users) {
            u.setFollow(followedUserIds.contains(u.getId()));
        }
        users = users.stream()
                .sorted(Comparator.comparing(User::getId))
                .toList();

        return ResponseEntity.ok().body(Map.of("users", users));
    }

    public ResponseEntity<?> getUserProfile(String username, String token, LocalDateTime lastDate, Long lastId) {
        final String myName = jwtUtil.getUsername(token);

        User me = userRepository.findByUsername(myName);
        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "invalid user"));
        }

        if (me.getisbaned()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You are banned"));
        }

        User ProfileInfos = userRepository.findByUsername(username);

        if (ProfileInfos == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "invalid user to search"));
        }

        if (ProfileInfos.getRole().equals(Role.Admin)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "you are trying to get sensitive data"));
        }

        if (followRepositry.existsByFollower_IdAndFollowed_Id(me.getId(), ProfileInfos.getId())) {
            ProfileInfos.setFollow(true);
        } else {
            ProfileInfos.setFollow(false);
        }
        List<Post> posts;
        PageRequest limit = PageRequest.of(0, 10);

        if (lastDate == null || lastId == null) {
            posts = postRepository.findTop10ByUserIdOrderByCreatedAtDescIdDesc(ProfileInfos.getId(),
                    me.getRole().equals(Role.Admin), limit);
        } else {
            posts = postRepository.findNextPosts(ProfileInfos.getId(), me.getRole().equals(Role.Admin), lastDate,
                    lastId, limit);
        }

        return ResponseEntity.ok(Map.of("user", ProfileInfos, "posts", posts, "followers",
                followRepositry.countByFollowed_Id(ProfileInfos.getId()), "followings",
                followRepositry.countByFollower_Id(ProfileInfos.getId()), "count",
                postRepository.countByUserId(ProfileInfos.getId())));
    }

    @PreAuthorize("hasRole('User')")
    public ResponseEntity<?> report(ReportRequest reportRequest, String token) {



        if (reportRequest.getType() == null || reportRequest.getType().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "No Type Found"));
        }

        final String type = reportRequest.getType().trim().toLowerCase();
        User repported = null;
        Post post = null;

        if (type.equals("post")) {
            if (reportRequest.getPost_id() == null) {
                throw new IllegalArgumentException("post_id is required for POST reports");
            } else {
                post = postRepository.findById(reportRequest.getPost_id()).orElse(null);
                if (post == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "invalid post to report"));
                }
                repported = post.getUser();
            }
        } else if (type.equals("user")) {
            if (reportRequest.getReportted_username() == null) {
                throw new IllegalArgumentException("reportted_username is required for USER reports");
            } else {
                repported = userRepository.findByUsername(reportRequest.getReportted_username());
                if (repported == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "invalid user to report"));
                }
                post = null;
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "invalid report type"));
        }

        if (reportRequest.getDiscription().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "No Description Found"));
        }

        if (reportRequest.getDiscription().length() > 500) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Description Should be under 200 letter"));
        }

        if (repported.getRole().equals(Role.Admin)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "you cant report admin users"));
        }

        final String myName = jwtUtil.getUsername(token);

        if (!rateLimiterService.isAllowed(myName)) {
            return ResponseEntity.status(429).body(Map.of("error", "Rate limit exceeded. Try again later."));
        }

        final User me = userRepository.findByUsername(myName);
        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid user"));
        }

        if (me.getId() == repported.getId()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "you cant report yourself"));
        }

        if (me.getisbaned()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You are banned"));
        }

        ReportEntity r = new ReportEntity();
        r.setDescription(reportRequest.getDiscription());
        r.setRepporter(me);
        r.setRepported(repported);
        r.setCreatedAt(LocalDateTime.now());
        r.setType(reportRequest.getType());
        r.setPost(post);
        reportRepository.save(r);
        return ResponseEntity.ok().body(Map.of("message", "reported succesfuuly"));
    }

   
}
