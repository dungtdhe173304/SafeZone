package com.group5.safezone.model.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;

import java.util.Date;

@Entity(tableName = "auctions",
        foreignKeys = {
                @ForeignKey(entity = Product.class,
                        parentColumns = "id",
                        childColumns = "productId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = User.class,
                        parentColumns = "id",
                        childColumns = "sellerUserId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = User.class,
                        parentColumns = "id",
                        childColumns = "highestBidderUserId",
                        onDelete = ForeignKey.SET_NULL),
                @ForeignKey(entity = User.class,
                        parentColumns = "id",
                        childColumns = "winnerUserId",
                        onDelete = ForeignKey.SET_NULL)
        })
public class Auctions {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "productId")
    private String productId;

    @ColumnInfo(name = "sellerUserId")
    private int sellerUserId;

    @ColumnInfo(name = "startPrice")
    private Double startPrice;

    @ColumnInfo(name = "buyNowPrice")
    private Double buyNowPrice;

    @ColumnInfo(name = "startTime")
    private Date startTime;

    @ColumnInfo(name = "endTime")
    private Date endTime;

    @ColumnInfo(name = "currentHighestBid")
    private Double currentHighestBid;

    @ColumnInfo(name = "highestBidderUserId")
    private Integer highestBidderUserId;

    @ColumnInfo(name = "status")
    private String status;

    @ColumnInfo(name = "winnerUserId")
    private Integer winnerUserId;

    @ColumnInfo(name = "winningBidAmount")
    private Double winningBidAmount;

    @ColumnInfo(name = "CreatedAt")
    private Date createdAt;

    @ColumnInfo(name = "UpdatedAt")
    private Date updatedAt;

    // Constructor
    public Auctions() {
        this.currentHighestBid = 0.0;
        this.status = "pending";
        this.createdAt = new Date();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public int getSellerUserId() { return sellerUserId; }
    public void setSellerUserId(int sellerUserId) { this.sellerUserId = sellerUserId; }

    public Double getStartPrice() { return startPrice; }
    public void setStartPrice(Double startPrice) { this.startPrice = startPrice; }

    public Double getBuyNowPrice() { return buyNowPrice; }
    public void setBuyNowPrice(Double buyNowPrice) { this.buyNowPrice = buyNowPrice; }

    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }

    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }

    public Double getCurrentHighestBid() { return currentHighestBid; }
    public void setCurrentHighestBid(Double currentHighestBid) { this.currentHighestBid = currentHighestBid; }

    public Integer getHighestBidderUserId() { return highestBidderUserId; }
    public void setHighestBidderUserId(Integer highestBidderUserId) { this.highestBidderUserId = highestBidderUserId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getWinnerUserId() { return winnerUserId; }
    public void setWinnerUserId(Integer winnerUserId) { this.winnerUserId = winnerUserId; }

    public Double getWinningBidAmount() { return winningBidAmount; }
    public void setWinningBidAmount(Double winningBidAmount) { this.winningBidAmount = winningBidAmount; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
