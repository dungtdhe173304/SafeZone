package com.group5.safezone.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.group5.safezone.model.entity.Notification;

import java.util.List;

@Dao
public interface NotificationDao {
    @Query("SELECT * FROM notification WHERE IsDeleted = 0")
    List<Notification> getAllNotifications();

    @Query("SELECT * FROM notification WHERE UserId = :userId AND IsDeleted = 0 ORDER BY CreatedDate DESC")
    List<Notification> getNotificationsByUserId(int userId);

    @Query("SELECT * FROM notification WHERE id = :id AND IsDeleted = 0")
    Notification getNotificationById(int id);

    @Query("SELECT * FROM notification WHERE UserId = :userId AND IsRead = 0 AND IsDeleted = 0")
    List<Notification> getUnreadNotificationsByUserId(int userId);

    @Query("UPDATE notification SET IsRead = 1 WHERE id = :id")
    void markAsRead(int id);

    @Query("UPDATE notification SET IsRead = 1 WHERE UserId = :userId")
    void markAllAsReadByUserId(int userId);

    @Insert
    void insert(Notification notification);

    @Update
    void update(Notification notification);

    @Delete
    void delete(Notification notification);

    @Query("UPDATE notification SET IsDeleted = 1, DeletedAt = datetime('now') WHERE id = :id")
    void softDelete(int id);
}
