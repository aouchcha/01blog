package _blog.backend.Entitys.Report;

import java.time.LocalDateTime;

import _blog.backend.Entitys.User.User;
import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "report")
public class ReportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "repported_id", nullable = false)
    private User repported;

    @ManyToOne
    @JoinColumn(name = "repporter_id", nullable = false)
    private User repporter;

    @Nonnull
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getRepported() {
        return repported;
    }

    public void setRepported(User repported) {
        this.repported = repported;
    }

    public User getRepporter() {
        return repporter;
    }

    public void setRepporter(User repporter) {
        this.repporter = repporter;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
