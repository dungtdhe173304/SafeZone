package com.group5.safezone.model.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;

import java.util.Date;

@Entity(tableName = "chatCommunity",
        foreignKeys = @ForeignKey(entity = User.class,
                parentColumns = "id",
                childColumns = "UserId",
                onDelete = ForeignKey.CASCADE))
public class ChatCommunity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "message")
    private String message;

    @ColumnInfo(name = "CreatedAt")
    private Date createdAt;

    @ColumnInfo(name = "UpdatedAt")
    private Date updatedAt;

    @ColumnInfo(name = "UserId")
    private int userId;

    // Constructor
    public ChatCommunity() {
        this.createdAt = new Date();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
