package _blog.backend.Repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import _blog.backend.Entitys.Report.ReportEntity;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, Long>{
    
}
