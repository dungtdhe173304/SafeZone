package com.group5.safezone.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.group5.safezone.model.entity.Verify;

import java.util.List;

@Dao
public interface VerifyDao {
    @Query("SELECT * FROM verify")
    List<Verify> getAllVerifications();

    @Query("SELECT * FROM verify WHERE id = :id")
    Verify getVerificationById(int id);

    @Query("SELECT * FROM verify WHERE UserId = :userId")
    Verify getVerificationByUserId(int userId);

    @Insert
    void insert(Verify verify);

    @Update
    void update(Verify verify);

    @Delete
    void delete(Verify verify);
}
