package com.group5.safezone.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.group5.safezone.model.entity.Transactions;

import java.util.List;

@Dao
public interface TransactionsDao {
    @Query("SELECT * FROM transactions")
    List<Transactions> getAllTransactions();

    @Query("SELECT * FROM transactions WHERE id = :id")
    Transactions getTransactionById(int id);

    @Query("SELECT * FROM transactions WHERE UserId = :userId ORDER BY transactionDate DESC")
    List<Transactions> getTransactionsByUserId(int userId);

    @Query("SELECT * FROM transactions WHERE transactionType = :type")
    List<Transactions> getTransactionsByType(String type);

    @Query("SELECT * FROM transactions WHERE status = :status")
    List<Transactions> getTransactionsByStatus(String status);

    @Query("SELECT * FROM transactions WHERE orderId = :orderId")
    List<Transactions> getTransactionsByOrderId(int orderId);

    @Query("SELECT * FROM transactions WHERE auctionId = :auctionId")
    List<Transactions> getTransactionsByAuctionId(int auctionId);

    @Query("SELECT SUM(amount) FROM transactions WHERE UserId = :userId AND transactionType = 'deposit' AND status = 'completed'")
    Double getTotalDepositByUserId(int userId);

    @Query("SELECT SUM(amount) FROM transactions WHERE UserId = :userId AND transactionType = 'withdraw' AND status = 'completed'")
    Double getTotalWithdrawByUserId(int userId);

    @Query("UPDATE transactions SET status = :status WHERE id = :id")
    void updateTransactionStatus(int id, String status);

    @Insert
    void insert(Transactions transaction);

    @Update
    void update(Transactions transaction);

    @Delete
    void delete(Transactions transaction);
}
