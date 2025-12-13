package _blog.backend.hanndlers;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import _blog.backend.helpers.JwtUtil;
import _blog.backend.service.AdminService;

@RestController
@CrossOrigin(origins = { "http://localhost:4200" })
@RequestMapping("/api/admin")

public class AdminController {

    private final AdminService adminService;
    private final JwtUtil jwtUtil;

    public AdminController(AdminService adminService, JwtUtil jwtUtil) {
        this.adminService = adminService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<?> AdminBoardContent(@RequestHeader("Authorization") String header,
            @RequestParam(required = false) Long lastUserId) {
        String token = header.replace("Bearer", "");
        if (!jwtUtil.validateToken(token) || !jwtUtil.getRole(token).equals("Admin")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid token"));
        }
        return adminService.getBoard(token, lastUserId);
    }

    @GetMapping("/reports")
    public ResponseEntity<?> LoadReports(@RequestHeader("Authorization") String header,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastDate,
            @RequestParam(required = false) Long lastId) {
        String token = header.replace("Bearer", "");
        if (!jwtUtil.validateToken(token) || !jwtUtil.getRole(token).equals("Admin")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid token"));
        }
        return adminService.loadReports(lastDate, lastId);
    }

    @DeleteMapping("/reports/{report_id}")
    public ResponseEntity<?> DeleteReports(@PathVariable Long report_id,
            @RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid token"));
        }
        return adminService.DeleteReport(report_id);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<?> RemoveUser(@PathVariable String username, @RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid token"));
        }
        return adminService.Remove(username, token);
    }

    @PutMapping("/ban/{username}")
    public ResponseEntity<?> BanUser(@PathVariable String username, @RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid token"));
        }
        return adminService.Ban(username, token);
    }

    @PutMapping("/unban/{username}")
    public ResponseEntity<?> UnBanUser(@PathVariable String username, @RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer", "");
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid token"));
        }
        return adminService.UnBanned(username, token);
    }
}
