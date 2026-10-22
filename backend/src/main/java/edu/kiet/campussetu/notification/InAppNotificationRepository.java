package edu.kiet.campussetu.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InAppNotificationRepository extends JpaRepository<InAppNotification, Long> {
    Page<InAppNotification> findByRecipientUserIdOrderByCreatedAtDesc(Long recipientUserId, Pageable pageable);
    List<InAppNotification> findTop20ByRecipientUserIdOrderByCreatedAtDesc(Long recipientUserId);
    long countByRecipientUserIdAndIsReadFalse(Long recipientUserId);
}
