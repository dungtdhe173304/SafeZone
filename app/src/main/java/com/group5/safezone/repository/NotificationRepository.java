package com.group5.safezone.repository;

import android.app.Application;

import com.group5.safezone.config.AppDatabase;
import com.group5.safezone.model.dao.NotificationDao;
import com.group5.safezone.model.entity.Notification;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NotificationRepository {
    private final NotificationDao notificationDao;
    private final ExecutorService executor;

    public NotificationRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        notificationDao = database.notificationDao();
        executor = Executors.newFixedThreadPool(3);
    }

    public Future<List<Notification>> getNotificationsByUserIdAsync(int userId) {
        return executor.submit(() -> notificationDao.getNotificationsByUserId(userId));
    }

    public Future<List<Notification>> getUnreadByUserIdAsync(int userId) {
        return executor.submit(() -> notificationDao.getUnreadNotificationsByUserId(userId));
    }

    public void insert(Notification notification) {
        executor.execute(() -> notificationDao.insert(notification));
    }

    public void markAsRead(int id) {
        executor.execute(() -> notificationDao.markAsRead(id));
    }

    public void markAllAsReadByUserId(int userId) {
        executor.execute(() -> notificationDao.markAllAsReadByUserId(userId));
    }

    public void shutdown() {
        executor.shutdown();
    }
}


