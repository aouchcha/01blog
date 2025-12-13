package _blog.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import _blog.backend.Entitys.Report.ReportEntity;
import _blog.backend.Entitys.User.User;
import _blog.backend.Entitys.User.UserStatsDTO;
import _blog.backend.Repos.ReportRepository;
import _blog.backend.Repos.UserRepository;
import _blog.backend.helpers.ContextHelpers;
import _blog.backend.Entitys.User.Role;

@Service
public class AdminService {
    private final UserRepository userRepository;

    private final ReportRepository reportRepository;

    private final ContextHelpers contextHelpers;

    private final RateLimiterService rateLimiterService;

    public AdminService(UserRepository userRepository, ReportRepository reportRepository, ContextHelpers contextHelpers,
            RateLimiterService rateLimiterService) {
        this.userRepository = userRepository;
        this.reportRepository = reportRepository;
        this.contextHelpers = contextHelpers;
        this.rateLimiterService = rateLimiterService;
    }

    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> getBoard(String token, Long lastUserId) {

        final User admin = userRepository.findByUsername(contextHelpers.getUsername());

        if (admin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid user"));
        }

        if (!admin.getRole().equals(Role.Admin)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "you don't have access here"));
        }
        PageRequest limit = PageRequest.of(0, 15);

        List<UserStatsDTO> users = new ArrayList<>();
        Long reports_count = 0L;
        try {
            if (lastUserId == null || lastUserId == 0) {
                users = userRepository.findUsersStates(limit);
            } else {
                users = userRepository.findUsersStatesAfter(lastUserId, limit);
            }
            reports_count = reportRepository.count();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "we can get data"));
        }
        return ResponseEntity.ok(Map.of("users", users, "reportsCount", reports_count));
    }

    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> loadReports(LocalDateTime lastDate, Long lastId) {
        final String username = contextHelpers.getUsername();
        User u = null;
        List<ReportEntity> reports = new ArrayList<>();
        Long reports_count = 0L;
        try {
            u = userRepository.findByUsername(username);
            if (u == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "the user is not valid"));
            }

            if (!u.getRole().equals(Role.Admin)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "you don't have access here"));
            }

            PageRequest pageRequest = PageRequest.of(0, 10);

            if (lastDate == null || lastId == null) {
                reports = reportRepository.findInitialReports(pageRequest);
            } else {
                reports = reportRepository.findNextReports(lastDate, lastId, pageRequest);
            }
            reports_count = reportRepository.count();

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "internal server error"));

        }

        return ResponseEntity.ok(Map.of("reports", reports, "reportsCount", reports_count));
    }

    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> DeleteReport(Long reportId) {

        if (!rateLimiterService.isAllowed(contextHelpers.getUsername())) {
            return ResponseEntity.status(429).body(Map.of("error", "Rate limit exceeded. Try again later."));
        }

        final User admin = userRepository.findByUsername(contextHelpers.getUsername());

        if (admin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid user"));
        }

        if (!admin.getRole().equals(Role.Admin)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "you don't have access here"));
        }

        ReportEntity r = reportRepository.findById(reportId).orElse(null);
        if (r == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Bad Request"));
        }
        reportRepository.delete(r);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Report Deleted With success"));
    }

    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> Remove(String username, String token) {

        if (!rateLimiterService.isAllowed(contextHelpers.getUsername())) {
            return ResponseEntity.status(429).body(Map.of("error", "Rate limit exceeded. Try again later."));
        }

        User user_to_remove = userRepository.findByUsername(username);

        if (user_to_remove == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "the user u wanna to remove doesnt exist"));
        }

        final User me = userRepository.findByUsername(contextHelpers.getUsername());

        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "you doesnt exist"));
        }

        if (!me.getRole().equals(Role.Admin)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "you don't have acess here"));
        }

        if (user_to_remove.getRole().equals(Role.Admin)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "you cant remove admin users"));
        }
        userRepository.delete(user_to_remove);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "User " + user_to_remove.getUsername() + " Removed with success"));
    }

    @Transactional
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> Ban(String username, String token) {

        if (!rateLimiterService.isAllowed(contextHelpers.getUsername())) {
            return ResponseEntity.status(429).body(Map.of("error", "Rate limit exceeded. Try again later."));
        }
        final User me = userRepository.findByUsername(contextHelpers.getUsername());

        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "you doesnt exist"));
        }

        if (!me.getRole().equals(Role.Admin)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "you don't have acess here"));
        }

        User u = userRepository.findByUsername(username);

        if (u == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "the user u wanna to ban doesnt exist"));
        }

        if (u.getRole().equals(Role.Admin)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "you cant ban admin users"));
        }

        u.setisbaned(true);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "User " + u.getUsername() + " Banned with success"));
    }

    @Transactional
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> UnBanned(String username, String token) {

        if (!rateLimiterService.isAllowed(contextHelpers.getUsername())) {
            return ResponseEntity.status(429).body(Map.of("error", "Rate limit exceeded. Try again later."));
        }
        final User me = userRepository.findByUsername(contextHelpers.getUsername());

        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "you doesnt exist"));
        }

        if (!me.getRole().equals(Role.Admin)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "you don't have acess here"));
        }

        User u = userRepository.findByUsername(username);

        if (u == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "the user u wanna to unban doesnt exist"));
        }

        if (u.getRole().equals(Role.Admin)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "you cant unban admin users"));
        }
        u.setisbaned(false);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "User " + u.getUsername() + " UnBanned with success"));
    }
}
