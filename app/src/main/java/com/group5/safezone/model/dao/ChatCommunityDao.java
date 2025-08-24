package com.group5.safezone.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.group5.safezone.model.entity.ChatCommunity;

import java.util.List;

@Dao
public interface ChatCommunityDao {
    @Query("SELECT * FROM chatCommunity ORDER BY CreatedAt DESC")
    List<ChatCommunity> getAllMessages();

    @Query("SELECT * FROM chatCommunity WHERE id = :id")
    ChatCommunity getMessageById(int id);

    @Query("SELECT * FROM chatCommunity WHERE UserId = :userId ORDER BY CreatedAt DESC")
    List<ChatCommunity> getMessagesByUserId(int userId);

    @Query("SELECT * FROM chatCommunity ORDER BY CreatedAt DESC LIMIT :limit")
    List<ChatCommunity> getRecentMessages(int limit);

    @Insert
    void insert(ChatCommunity chatCommunity);

    @Update
    void update(ChatCommunity chatCommunity);

    @Delete
    void delete(ChatCommunity chatCommunity);
} 
