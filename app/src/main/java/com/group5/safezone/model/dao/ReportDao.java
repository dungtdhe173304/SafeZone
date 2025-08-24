package com.group5.safezone.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.group5.safezone.model.entity.Report;

import java.util.List;

@Dao
public interface ReportDao {
    @Query("SELECT * FROM report WHERE IsDeleted = 0")
    List<Report> getAllReports();

    @Query("SELECT * FROM report WHERE id = :id AND IsDeleted = 0")
    Report getReportById(int id);

    @Query("SELECT * FROM report WHERE CreatedBy = :userId AND IsDeleted = 0")
    List<Report> getReportsByUserId(int userId);

    @Query("SELECT * FROM report WHERE orderId = :orderId AND IsDeleted = 0")
    List<Report> getReportsByOrderId(int orderId);

    @Query("SELECT * FROM report WHERE status = :status AND IsDeleted = 0")
    List<Report> getReportsByStatus(String status);

    @Query("UPDATE report SET status = :status WHERE id = :id")
    void updateReportStatus(int id, String status);

    @Insert
    void insert(Report report);

    @Update
    void update(Report report);

    @Delete
    void delete(Report report);

    @Query("UPDATE report SET IsDeleted = 1, DeletedAt = datetime('now') WHERE id = :id")
    void softDelete(int id);
}
