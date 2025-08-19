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

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY createdAt DESC")
    List<Transactions> getTransactionsByUserId(int userId);

    @Query("SELECT * FROM transactions WHERE userId = :userId AND transactionType = :type ORDER BY createdAt DESC")
    List<Transactions> getTransactionsByUserIdAndType(int userId, String type);

    @Query("SELECT * FROM transactions WHERE userId = :userId AND status = :status ORDER BY createdAt DESC")
    List<Transactions> getTransactionsByUserIdAndStatus(int userId, String status);

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit")
    List<Transactions> getRecentTransactions(int userId, int limit);

    @Query("SELECT * FROM transactions WHERE transactionType = :type")
    List<Transactions> getTransactionsByType(String type);

    @Query("SELECT * FROM transactions WHERE status = :status")
    List<Transactions> getTransactionsByStatus(String status);

    @Query("SELECT * FROM transactions WHERE orderId = :orderId")
    List<Transactions> getTransactionsByOrderId(int orderId);

    @Query("SELECT * FROM transactions WHERE auctionId = :auctionId")
    List<Transactions> getTransactionsByAuctionId(int auctionId);

    @Query("SELECT * FROM transactions WHERE referenceId = :referenceId")
    Transactions getTransactionByReferenceId(String referenceId);

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND transactionType = 'DEPOSIT' AND status = 'SUCCESS'")
    Double getTotalDepositByUserId(int userId);

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND transactionType = 'WITHDRAW' AND status = 'SUCCESS'")
    Double getTotalWithdrawByUserId(int userId);

    @Query("SELECT COUNT(*) FROM transactions WHERE userId = :userId AND transactionType = :type AND status = :status")
    int getTransactionCount(int userId, String type, String status);

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND transactionType = :type AND status = :status")
    Double getTotalAmount(int userId, String type, String status);

    @Query("UPDATE transactions SET status = :status, referenceId = :referenceId WHERE id = :id")
    void updateTransactionStatus(long id, String status, String referenceId);

    @Insert
    long insert(Transactions transaction);

    @Insert
    void insertMultipleTransactions(List<Transactions> transactions);

    @Update
    void update(Transactions transaction);

    @Delete
    void delete(Transactions transaction);
}
