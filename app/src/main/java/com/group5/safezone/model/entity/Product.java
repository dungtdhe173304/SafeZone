package com.group5.safezone.model.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;

import java.util.Date;

@Entity(tableName = "product",
        foreignKeys = @ForeignKey(entity = User.class,
                parentColumns = "id",
                childColumns = "UserId",
                onDelete = ForeignKey.CASCADE))
public class Product {
    @PrimaryKey
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "productName")
    private String productName;

    @ColumnInfo(name = "fee")
    private Double fee;

    @ColumnInfo(name = "describe")
    private String describe;

    @ColumnInfo(name = "information")
    private String information;

    @ColumnInfo(name = "view")
    private Integer view;

    @ColumnInfo(name = "quantity")
    private Integer quantity;

    @ColumnInfo(name = "price")
    private Double price;

    @ColumnInfo(name = "privateInfo")
    private String privateInfo;

    @ColumnInfo(name = "publicPrivate")
    private String publicPrivate;

    @ColumnInfo(name = "userView")
    private String userView;

    @ColumnInfo(name = "status")
    private String status;

    @ColumnInfo(name = "IsDeleted")
    private Boolean isDeleted;

    @ColumnInfo(name = "DeletedAt")
    private Date deletedAt;

    @ColumnInfo(name = "CreatedAt")
    private Date createdAt;

    @ColumnInfo(name = "UpdatedAt")
    private Date updatedAt;

    @ColumnInfo(name = "UserId")
    private int userId;

    @ColumnInfo(name = "isAuctionItem")
    private Boolean isAuctionItem;

    @ColumnInfo(name = "isAdminCheck")
    private Boolean isAdminCheck;

    @ColumnInfo(name = "adminmessage")
    private String adminMessage;

    @ColumnInfo(name = "minBidIncrement")
    private Double minBidIncrement;

    // Constructor
    public Product() {
        this.view = 0;
        this.quantity = 1;
        this.isDeleted = false;
        this.isAuctionItem = false;
        this.isAdminCheck = false;
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Double getFee() { return fee; }
    public void setFee(Double fee) { this.fee = fee; }

    public String getDescribe() { return describe; }
    public void setDescribe(String describe) { this.describe = describe; }

    public String getInformation() { return information; }
    public void setInformation(String information) { this.information = information; }

    public Integer getView() { return view; }
    public void setView(Integer view) { this.view = view; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getPrivateInfo() { return privateInfo; }
    public void setPrivateInfo(String privateInfo) { this.privateInfo = privateInfo; }

    public String getPublicPrivate() { return publicPrivate; }
    public void setPublicPrivate(String publicPrivate) { this.publicPrivate = publicPrivate; }

    public String getUserView() { return userView; }
    public void setUserView(String userView) { this.userView = userView; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }

    public Date getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Date deletedAt) { this.deletedAt = deletedAt; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Boolean getIsAuctionItem() { return isAuctionItem; }
    public void setIsAuctionItem(Boolean isAuctionItem) { this.isAuctionItem = isAuctionItem; }

    public Boolean getIsAdminCheck() { return isAdminCheck; }
    public void setIsAdminCheck(Boolean isAdminCheck) { this.isAdminCheck = isAdminCheck; }

    public String getAdminMessage() { return adminMessage; }
    public void setAdminMessage(String adminMessage) { this.adminMessage = adminMessage; }

    public Double getMinBidIncrement() { return minBidIncrement; }
    public void setMinBidIncrement(Double minBidIncrement) { this.minBidIncrement = minBidIncrement; }
}
