package com.athena.v2.notifications.repositories;

import com.athena.v2.notifications.models.Notifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notifications, Long> {
        List<Notifications> findByUserIdOrderByTimestampDesc(String userId);
        List<Notifications> findByUserIdAndEventTypeOrderByTimestampDesc(String userId, String eventType);
}
