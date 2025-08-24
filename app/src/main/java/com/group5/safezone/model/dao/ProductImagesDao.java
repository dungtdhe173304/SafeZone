package com.group5.safezone.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.group5.safezone.model.entity.ProductImages;

import java.util.List;

@Dao
public interface ProductImagesDao {
    @Query("SELECT * FROM productImages")
    List<ProductImages> getAllProductImages();

    @Query("SELECT * FROM productImages WHERE id = :id")
    ProductImages getProductImageById(int id);

    @Query("SELECT * FROM productImages WHERE productId = :productId ORDER BY CreatedAt ASC")
    List<ProductImages> getImagesByProductId(String productId);

    @Insert
    void insert(ProductImages productImages);

    @Insert
    void insertMultiple(List<ProductImages> productImages);

    @Query("SELECT * FROM productImages WHERE productId = :productId")
    List<ProductImages> getByProductId(String productId);

    @Update
    void update(ProductImages productImages);

    @Delete
    void delete(ProductImages productImages);

    @Query("DELETE FROM productImages WHERE productId = :productId")
    void deleteImagesByProductId(String productId);




}
