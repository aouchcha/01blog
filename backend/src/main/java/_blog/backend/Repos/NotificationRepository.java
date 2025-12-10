package _blog.backend.Repos;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import _blog.backend.Entitys.Notifications.NotificationEntity;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByCreator_IdAndIdGreaterThan(Long userId, Long lastEventId);

    List<NotificationEntity> findByCreator_Id(Long userId);

    int countByRecipient_IdAndSeenFalse(Long recipientId);

    List<NotificationEntity> findByRecipient_IdAndSeenFalse(Long recipientId);

    List<NotificationEntity> findByRecipientIdOrderByCreatedAtDescIdDesc(
            Long recipientId,
            Pageable pageable);

    @Query("SELECT n FROM NotificationEntity n WHERE n.recipient.id = :recipientId " +
            "AND (n.createdAt < :lastDate OR (n.createdAt = :lastDate AND n.id < :lastId)) " +
            "ORDER BY n.createdAt DESC, n.id DESC")
    List<NotificationEntity> findByRecipientIdAndCreatedAtLessThanOrCreatedAtEqualsAndIdLessThanOrderByCreatedAtDescIdDesc(
            @Param("recipientId") Long recipientId,
            @Param("lastDate") LocalDateTime lastDate,
            @Param("lastId") Long lastId,
            Pageable pageable);
}