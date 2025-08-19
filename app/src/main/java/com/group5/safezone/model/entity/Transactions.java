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
                        childColumns = "userId",
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

    @ColumnInfo(name = "userId")
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

    @ColumnInfo(name = "referenceId")
    private String referenceId; // ZaloPay transaction ID

    @ColumnInfo(name = "createdAt")
    private Date createdAt;

    @ColumnInfo(name = "updatedAt")
    private Date updatedAt;

    // Constructor
    public Transactions() {
        this.transactionDate = new Date();
        this.createdAt = new Date();
        this.updatedAt = new Date();
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

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    // Transaction types constants
    public static final String TYPE_DEPOSIT = "DEPOSIT";
    public static final String TYPE_WITHDRAW = "WITHDRAW";
    public static final String TYPE_TRANSFER = "TRANSFER";
    public static final String TYPE_PAYMENT = "PAYMENT";

    // Status constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
}
