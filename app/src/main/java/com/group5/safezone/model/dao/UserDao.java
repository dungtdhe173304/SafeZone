package com.group5.safezone.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.group5.safezone.model.entity.User;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM user WHERE IsDeleted = 0")
    List<User> getAllUsers();

    @Query("SELECT * FROM user WHERE id = :userId AND IsDeleted = 0")
    User getUserById(int userId);

    @Query("SELECT * FROM user WHERE email = :email AND IsDeleted = 0")
    User getUserByEmail(String email);

    @Query("SELECT * FROM user WHERE userName = :userName AND IsDeleted = 0")
    User getUserByUserName(String userName);

    @Insert
    void insertUser(User user);

    @Insert
    void insertMultipleUsers(List<User> users);

    @Update
    void updateUser(User user);

    @Delete
    void deleteUser(User user);

    @Query("UPDATE user SET IsDeleted = 1, DeletedAt = datetime('now') WHERE id = :userId")
    void softDeleteUser(int userId);

    @Query("SELECT * FROM user WHERE role = :role AND IsDeleted = 0")
    List<User> getUsersByRole(String role);

    @Query("UPDATE user SET balance = balance + :amount WHERE id = :userId")
    void updateBalance(int userId, double amount);

    @Query("DELETE FROM user")
    void deleteAllUsers();
    
    @Query("SELECT * FROM user WHERE (userName LIKE :query OR email LIKE :query) AND IsDeleted = 0 ORDER BY userName ASC")
    List<User> searchUsersByUsernameOrEmail(String query);
}
