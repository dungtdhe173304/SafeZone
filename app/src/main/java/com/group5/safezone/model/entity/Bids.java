package com.group5.safezone.model.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;

import java.util.Date;

@Entity(tableName = "bids",
        foreignKeys = {
                @ForeignKey(entity = Auctions.class,
                        parentColumns = "id",
                        childColumns = "auctionId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = User.class,
                        parentColumns = "id",
                        childColumns = "bidderUserId",
                        onDelete = ForeignKey.CASCADE)
        })
public class Bids {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "auctionId")
    private int auctionId;

    @ColumnInfo(name = "bidderUserId")
    private int bidderUserId;

    @ColumnInfo(name = "bidAmount")
    private Double bidAmount;

    @ColumnInfo(name = "bidTime")
    private Date bidTime;

    @ColumnInfo(name = "status")
    private String status;

    @ColumnInfo(name = "CreatedAt")
    private Date createdAt;

    // Constructor
    public Bids() {
        this.bidTime = new Date();
        this.status = "active";
        this.createdAt = new Date();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAuctionId() { return auctionId; }
    public void setAuctionId(int auctionId) { this.auctionId = auctionId; }

    public int getBidderUserId() { return bidderUserId; }
    public void setBidderUserId(int bidderUserId) { this.bidderUserId = bidderUserId; }

    public Double getBidAmount() { return bidAmount; }
    public void setBidAmount(Double bidAmount) { this.bidAmount = bidAmount; }

    public Date getBidTime() { return bidTime; }
    public void setBidTime(Date bidTime) { this.bidTime = bidTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
