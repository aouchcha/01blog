package _blog.backend.Repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import _blog.backend.Entitys.Notifications.NotificationEntity;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByCreator_IdAndIdGreaterThan(Long userId, Long lastEventId);

    List<NotificationEntity> findByCreator_Id(Long userId);

    int countByRecipient_IdAndSeenFalse(Long recipientId);

    List<NotificationEntity> findByRecipient_IdAndSeenFalse(Long recipientId);
}