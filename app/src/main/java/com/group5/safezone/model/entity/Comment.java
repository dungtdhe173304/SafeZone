package com.group5.safezone.model.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;

import java.util.Date;

@Entity(tableName = "comment",
        foreignKeys = {
                @ForeignKey(entity = Product.class,
                        parentColumns = "id",
                        childColumns = "productId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = User.class,
                        parentColumns = "id",
                        childColumns = "UserId",
                        onDelete = ForeignKey.CASCADE)
        })
public class Comment {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "CreatedAt")
    private Date createdAt;

    @ColumnInfo(name = "UpdatedAt")
    private Date updatedAt;

    @ColumnInfo(name = "IsDeleted")
    private Boolean isDeleted;

    @ColumnInfo(name = "DeletedAt")
    private Date deletedAt;

    @ColumnInfo(name = "productId")
    private String productId;

    @ColumnInfo(name = "UserId")
    private int userId;

    // Constructor
    public Comment() {
        this.createdAt = new Date();
        this.isDeleted = false;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }

    public Date getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Date deletedAt) { this.deletedAt = deletedAt; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
