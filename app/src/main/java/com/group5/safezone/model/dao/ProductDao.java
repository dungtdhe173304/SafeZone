package com.group5.safezone.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.group5.safezone.model.entity.Product;

import java.util.List;

@Dao
public interface ProductDao {
    @Query("SELECT * FROM product WHERE IsDeleted = 0")
    List<Product> getAllProducts();

    @Query("SELECT * FROM product WHERE id = :id AND IsDeleted = 0")
    Product getProductById(String id);

    @Query("SELECT * FROM product WHERE UserId = :userId AND IsDeleted = 0")
    List<Product> getProductsByUserId(int userId);

    @Query("SELECT * FROM product WHERE status = :status AND IsDeleted = 0")
    List<Product> getProductsByStatus(String status);

    @Query("SELECT * FROM product WHERE isAuctionItem = 1 AND IsDeleted = 0")
    List<Product> getAuctionProducts();

    @Query("SELECT * FROM product WHERE isAuctionItem = 0 AND IsDeleted = 0")
    List<Product> getNormalProducts();

    @Query("SELECT * FROM product WHERE productName LIKE '%' || :keyword || '%' AND IsDeleted = 0")
    List<Product> searchProducts(String keyword);

    @Query("UPDATE product SET view = view + 1 WHERE id = :id")
    void incrementView(String id);

    @Query("UPDATE product SET isAdminCheck = :isChecked, adminmessage = :message WHERE id = :id")
    void updateAdminCheck(String id, boolean isChecked, String message);

    @Insert
    void insert(Product product);

    @Update
    void update(Product product);

    @Delete
    void delete(Product product);

    @Query("UPDATE product SET IsDeleted = 1, DeletedAt = datetime('now') WHERE id = :id")
    void softDelete(String id);
}
