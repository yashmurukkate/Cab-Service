package com.cabservice.notification.repository;

import com.cabservice.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUserId(Long userId, Pageable pageable);
    List<Notification> findByStatus(Notification.NotificationStatus status);
    List<Notification> findByStatusAndRetryCountLessThan(Notification.NotificationStatus status, Integer maxRetries);
}
