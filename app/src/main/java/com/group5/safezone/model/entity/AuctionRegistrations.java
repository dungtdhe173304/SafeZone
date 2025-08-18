package com.group5.safezone.model.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;

import java.util.Date;

@Entity(tableName = "auctionRegistrations",
        foreignKeys = {
                @ForeignKey(entity = Auctions.class,
                        parentColumns = "id",
                        childColumns = "auctionId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = User.class,
                        parentColumns = "id",
                        childColumns = "userId",
                        onDelete = ForeignKey.CASCADE)
        })
public class AuctionRegistrations {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "auctionId")
    private int auctionId;

    @ColumnInfo(name = "userId")
    private int userId;

    @ColumnInfo(name = "paymentAmount")
    private Double paymentAmount;

    @ColumnInfo(name = "paymentDate")
    private Date paymentDate;

    @ColumnInfo(name = "status")
    private String status;

    @ColumnInfo(name = "CreatedAt")
    private Date createdAt;

    @ColumnInfo(name = "UpdatedAt")
    private Date updatedAt;

    // Constructor
    public AuctionRegistrations() {
        this.paymentDate = new Date();
        this.status = "pending";
        this.createdAt = new Date();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAuctionId() { return auctionId; }
    public void setAuctionId(int auctionId) { this.auctionId = auctionId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Double getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(Double paymentAmount) { this.paymentAmount = paymentAmount; }

    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
