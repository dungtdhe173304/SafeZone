package com.group5.safezone.model.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;

import java.util.Date;

@Entity(tableName = "order",
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
public class Order {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "price")
    private Double price;

    @ColumnInfo(name = "quantity")
    private Integer quantity;

    @ColumnInfo(name = "note")
    private String note;

    @ColumnInfo(name = "orderDate")
    private Date orderDate;

    @ColumnInfo(name = "status")
    private String status;

    @ColumnInfo(name = "IsDeleted")
    private Boolean isDeleted;

    @ColumnInfo(name = "DeletedAt")
    private Date deletedAt;

    @ColumnInfo(name = "productId")
    private String productId;

    @ColumnInfo(name = "UserId")
    private int userId;

    // Constructor
    public Order() {
        this.orderDate = new Date();
        this.isDeleted = false;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }

    public Date getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Date deletedAt) { this.deletedAt = deletedAt; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
