package _blog.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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

@Service
public class AdminService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    // private JwtUtil jwtUtil;
    private ContextHelpers contextHelpers;

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> getBoard(String token, Long lastUserId) {
        // System.out.println("IIIIIIIIIIIIIIIIIIIIIIIID : "+ lastUserId);
        PageRequest limit = PageRequest.of(0, 15);

        List<UserStatsDTO> users;
        if (lastUserId == null || lastUserId == 0) {
            // First page
            // System.out.println("3AAAAAAAAAAAAAAAAAADI");
            users = userRepository.findUsersStates(limit);
        } else {
            // Subsequent pages
            // System.out.println("JOOOOOOOOOOOOOOOOOOOUUUJ");
            users = userRepository.findUsersStatesAfter(lastUserId, limit);
        }
        Long reports_count = reportRepository.count();
        return ResponseEntity.ok(Map.of("users", users, "reportsCount", reports_count));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> loadReports(LocalDateTime lastDate, Long lastId) {
        final String username = contextHelpers.getUsername();
        User u = userRepository.findByUsername(username);
        if (u == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "the user is not valid"));
        }

        List<ReportEntity> reports;
        PageRequest pageRequest = PageRequest.of(0, 10);

        if (lastDate == null || lastId == null) {
            // First page - get initial 10 reports
            reports = reportRepository.findInitialReports(pageRequest);
        } else {
            // Subsequent pages - get reports after cursor
            reports = reportRepository.findNextReports(lastDate, lastId, pageRequest);
        }
        Long reports_count = reportRepository.count();

        return ResponseEntity.ok(Map.of("reports", reports, "reportsCount", reports_count));
    }

    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> DeleteReport(Long reportId) {
        ReportEntity r = reportRepository.findById(reportId).orElse(null);
        if (r == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Bad Request"));
        }
        reportRepository.delete(r);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Report Deleted With success"));
    }
}
