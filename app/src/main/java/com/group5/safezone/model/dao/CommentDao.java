package com.group5.safezone.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.group5.safezone.model.entity.Comment;

import java.util.List;

@Dao
public interface CommentDao {
    @Query("SELECT * FROM comment WHERE IsDeleted = 0")
    List<Comment> getAllComments();

    @Query("SELECT * FROM comment WHERE id = :id AND IsDeleted = 0")
    Comment getCommentById(int id);

    @Query("SELECT * FROM comment WHERE productId = :productId AND IsDeleted = 0 ORDER BY CreatedAt DESC")
    List<Comment> getCommentsByProductId(String productId);

    @Query("SELECT * FROM comment WHERE UserId = :userId AND IsDeleted = 0")
    List<Comment> getCommentsByUserId(int userId);

    @Insert
    void insert(Comment comment);

    @Update
    void update(Comment comment);

    @Delete
    void delete(Comment comment);

    @Query("UPDATE comment SET IsDeleted = 1, DeletedAt = datetime('now') WHERE id = :id")
    void softDelete(int id);
}
