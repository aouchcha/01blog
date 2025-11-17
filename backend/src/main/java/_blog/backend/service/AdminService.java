package _blog.backend.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import _blog.backend.Entitys.Report.ReportEntity;
import _blog.backend.Entitys.User.UserStatsDTO;
import _blog.backend.Repos.ReportRepository;
import _blog.backend.Repos.UserRepository;

@Service
public class AdminService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Transactional(readOnly = true)
    public ResponseEntity<?> getBoard(String token) {
        List<UserStatsDTO> users = userRepository.findUsersStates();
        Long reports_count = reportRepository.count();
        return ResponseEntity.ok(Map.of("users", users, "reportsCount", reports_count));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> loadReports() {
        List<ReportEntity> reports = reportRepository.findAll();
        return ResponseEntity.ok(Map.of("reports", reports));
    }

    public ResponseEntity<?> DeleteReport(Long reportId) {
        ReportEntity r = reportRepository.findById(reportId).orElse(null);
        if (r == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Bad Request"));
        }
        reportRepository.delete(r);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Report Deleted With success"));
    }
}
