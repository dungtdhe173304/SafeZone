package com.group5.safezone.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.group5.safezone.model.entity.Order;

import java.util.List;

@Dao
public interface OrderDao {
    @Query("SELECT * FROM `order` WHERE IsDeleted = 0")
    List<Order> getAllOrders();

    @Query("SELECT * FROM `order` WHERE id = :id AND IsDeleted = 0")
    Order getOrderById(int id);

    @Query("SELECT * FROM `order` WHERE UserId = :userId AND IsDeleted = 0 ORDER BY orderDate DESC")
    List<Order> getOrdersByUserId(int userId);

    @Query("SELECT * FROM `order` WHERE productId = :productId AND IsDeleted = 0")
    List<Order> getOrdersByProductId(String productId);

    @Query("SELECT * FROM `order` WHERE status = :status AND IsDeleted = 0")
    List<Order> getOrdersByStatus(String status);
    
    @Query("SELECT o.* FROM `order` o " +
           "INNER JOIN product p ON o.productId = p.id " +
           "WHERE p.userId = :sellerId AND o.IsDeleted = 0 " +
           "ORDER BY o.orderDate DESC")
    List<Order> getOrdersBySellerId(int sellerId);

    @Query("UPDATE `order` SET status = :status WHERE id = :id")
    void updateOrderStatus(int id, String status);

    @Insert
    long insert(Order order);

    @Update
    void update(Order order);

    @Delete
    void delete(Order order);

    @Query("UPDATE `order` SET IsDeleted = 1, DeletedAt = datetime('now') WHERE id = :id")
    void softDelete(int id);
}
