package com.group5.safezone.model.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;

import java.util.Date;

@Entity(tableName = "report",
        foreignKeys = {
                @ForeignKey(entity = User.class,
                        parentColumns = "id",
                        childColumns = "CreatedBy",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Order.class,
                        parentColumns = "id",
                        childColumns = "orderId",
                        onDelete = ForeignKey.CASCADE)
        })
public class Report {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "describe")
    private String describe;

    @ColumnInfo(name = "CreatedBy")
    private int createdBy;

    @ColumnInfo(name = "CreatedAt")
    private Date createdAt;

    @ColumnInfo(name = "UpdatedAt")
    private Date updatedAt;

    @ColumnInfo(name = "IsDeleted")
    private Boolean isDeleted;

    @ColumnInfo(name = "DeletedAt")
    private Date deletedAt;

    @ColumnInfo(name = "status")
    private String status;

    @ColumnInfo(name = "orderId")
    private int orderId;

    // Constructor
    public Report() {
        this.createdAt = new Date();
        this.isDeleted = false;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescribe() { return describe; }
    public void setDescribe(String describe) { this.describe = describe; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }

    public Date getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Date deletedAt) { this.deletedAt = deletedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
}
