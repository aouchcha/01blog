package _blog.backend.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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
        return ResponseEntity.ok(Map.of("users", users));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> loadReports() {
        List<ReportEntity> reports = reportRepository.findAll();
        return ResponseEntity.ok(Map.of("reports", reports));
    }
}
