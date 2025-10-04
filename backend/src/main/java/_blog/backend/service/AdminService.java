package _blog.backend.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import _blog.backend.Entitys.Report.ReportEntity;
import _blog.backend.Entitys.User.UserStatsDTO;
import _blog.backend.Repos.ReportRepository;
import _blog.backend.Repos.UserRepository;
import jakarta.transaction.Transactional;

@Service
@Transactional

public class AdminService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportRepository reportRepository;

    public ResponseEntity<?> getBoard(String token) {
        List<UserStatsDTO> users = userRepository.findUsersStates();
        return ResponseEntity.ok().body(Map.of("users", users));
    }

    public ResponseEntity<?> loadReports() {
        List<ReportEntity> r = reportRepository.findAll();
        return ResponseEntity.ok().body(Map.of("reports", r));

    }
}
