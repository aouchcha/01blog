package _blog.backend.Repos;

import org.springframework.data.jpa.repository.JpaRepository;

import _blog.backend.Entitys.Report.ReportEntity;

public interface ReportRepository extends JpaRepository<ReportEntity, Long>{
    
}
