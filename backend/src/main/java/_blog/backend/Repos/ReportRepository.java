package _blog.backend.Repos;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import _blog.backend.Entitys.Report.ReportEntity;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, Long> {
    
    // First page: Get initial 10 reports ordered by createdAt DESC, then id DESC
    @Query("SELECT r FROM ReportEntity r ORDER BY r.createdAt DESC, r.id DESC")
    List<ReportEntity> findInitialReports(Pageable pageable);
    
    // Subsequent pages: Get reports older than the cursor (lastDate, lastId)
    @Query("SELECT r FROM ReportEntity r " +
           "WHERE (r.createdAt < :lastDate OR (r.createdAt = :lastDate AND r.id < :lastId)) " +
           "ORDER BY r.createdAt DESC, r.id DESC")
    List<ReportEntity> findNextReports(
        LocalDateTime lastDate,
        Long lastId,
        Pageable pageable
    );
}
