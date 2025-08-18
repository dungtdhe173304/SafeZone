package com.group5.safezone.model.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;

import java.util.Date;

@Entity(tableName = "notification",
        foreignKeys = {
                @ForeignKey(entity = User.class,
                        parentColumns = "id",
                        childColumns = "UserId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = User.class,
                        parentColumns = "id",
                        childColumns = "CreatedBy",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = User.class,
                        parentColumns = "id",
                        childColumns = "ModifiedBy",
                        onDelete = ForeignKey.SET_NULL)
        })
public class Notification {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "UserId")
    private int userId;

    @ColumnInfo(name = "Type")
    private String type;

    @ColumnInfo(name = "Message")
    private String message;

    @ColumnInfo(name = "IsRead")
    private Boolean isRead;

    @ColumnInfo(name = "RelatedEntityId")
    private Integer relatedEntityId;

    @ColumnInfo(name = "RelatedEntityType")
    private String relatedEntityType;

    @ColumnInfo(name = "Link")
    private String link;

    @ColumnInfo(name = "CreatedDate")
    private Date createdDate;

    @ColumnInfo(name = "ModifiedDate")
    private Date modifiedDate;

    @ColumnInfo(name = "CreatedBy")
    private int createdBy;

    @ColumnInfo(name = "ModifiedBy")
    private Integer modifiedBy;

    @ColumnInfo(name = "IsDeleted")
    private Boolean isDeleted;

    @ColumnInfo(name = "DeletedAt")
    private Date deletedAt;

    // Constructor
    public Notification() {
        this.isRead = false;
        this.isDeleted = false;
        this.createdDate = new Date();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public Integer getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(Integer relatedEntityId) { this.relatedEntityId = relatedEntityId; }

    public String getRelatedEntityType() { return relatedEntityType; }
    public void setRelatedEntityType(String relatedEntityType) { this.relatedEntityType = relatedEntityType; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

    public Date getModifiedDate() { return modifiedDate; }
    public void setModifiedDate(Date modifiedDate) { this.modifiedDate = modifiedDate; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public Integer getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(Integer modifiedBy) { this.modifiedBy = modifiedBy; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }

    public Date getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Date deletedAt) { this.deletedAt = deletedAt; }
}
