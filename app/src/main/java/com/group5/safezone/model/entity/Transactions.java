package com.group5.safezone.model.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;

import java.util.Date;

@Entity(tableName = "transactions",
        foreignKeys = {
                @ForeignKey(entity = User.class,
                        parentColumns = "id",
                        childColumns = "UserId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = User.class,
                        parentColumns = "id",
                        childColumns = "relatedUserId",
                        onDelete = ForeignKey.SET_NULL),
                @ForeignKey(entity = Order.class,
                        parentColumns = "id",
                        childColumns = "orderId",
                        onDelete = ForeignKey.SET_NULL),
                @ForeignKey(entity = Auctions.class,
                        parentColumns = "id",
                        childColumns = "auctionId",
                        onDelete = ForeignKey.SET_NULL)
        })
public class Transactions {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "UserId")
    private int userId;

    @ColumnInfo(name = "transactionType")
    private String transactionType;

    @ColumnInfo(name = "amount")
    private Double amount;

    @ColumnInfo(name = "transactionDate")
    private Date transactionDate;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "relatedUserId")
    private Integer relatedUserId;

    @ColumnInfo(name = "status")
    private String status;

    @ColumnInfo(name = "orderId")
    private Integer orderId;

    @ColumnInfo(name = "auctionId")
    private Integer auctionId;

    // Constructor
    public Transactions() {
        this.transactionDate = new Date();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public Date getTransactionDate() { return transactionDate; }
    public void setTransactionDate(Date transactionDate) { this.transactionDate = transactionDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getRelatedUserId() { return relatedUserId; }
    public void setRelatedUserId(Integer relatedUserId) { this.relatedUserId = relatedUserId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }

    public Integer getAuctionId() { return auctionId; }
    public void setAuctionId(Integer auctionId) { this.auctionId = auctionId; }
}
