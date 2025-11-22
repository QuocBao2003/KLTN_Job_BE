package com.example.demo.repository;

import com.example.demo.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Đếm thông báo CHƯA XEM
    Long countByUserIdAndIsViewedFalse(Long userId);

    // Lấy thông báo chưa đọc quá 7 ngày
    List<Notification> findByUserIdAndIsReadTrueAndReadAtBefore(Long userId, Instant sevenDaysAgo);

    // Lấy tất cả thông báo (bao gồm cả đã đọc trong 7 ngày)
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId " +
            "AND (n.isRead = false OR (n.isRead = true AND n.readAt > :sevenDaysAgo)) " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotificationsByUserId(@Param("userId") Long userId,
                                                       @Param("sevenDaysAgo") Instant sevenDaysAgo);

    // Đánh dấu tất cả là đã xem (viewed)
    @Modifying
    @Query("UPDATE Notification n SET n.isViewed = true WHERE n.user.id = :userId AND n.isViewed = false")
    void markAllAsViewedByUserId(@Param("userId") Long userId);
}
