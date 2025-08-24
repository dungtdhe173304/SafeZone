package com.group5.safezone.repository;

import android.app.Application;

import com.group5.safezone.config.AppDatabase;
import com.group5.safezone.model.dao.TransactionsDao;
import com.group5.safezone.model.entity.Transactions;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionRepository {
    private TransactionsDao transactionsDao;
    private ExecutorService executor;

    public TransactionRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        transactionsDao = db.transactionsDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public void insertTransaction(Transactions transaction, OnInsertCallback callback) {
        executor.execute(() -> {
            long id = transactionsDao.insert(transaction);
            callback.onResult(id);
        });
    }

    public void insertMultipleTransactions(List<Transactions> transactions) {
        executor.execute(() -> transactionsDao.insertMultipleTransactions(transactions));
    }

    public void updateTransaction(Transactions transaction) {
        executor.execute(() -> transactionsDao.update(transaction));
    }

    public void updateTransactionStatus(long transactionId, String status, String referenceId) {
        executor.execute(() -> transactionsDao.updateTransactionStatus(transactionId, status, referenceId));
    }

    public void deleteTransaction(Transactions transaction) {
        executor.execute(() -> transactionsDao.delete(transaction));
    }

    public void deleteTransactionsByUserId(int userId) {
        executor.execute(() -> {
            List<Transactions> transactions = transactionsDao.getTransactionsByUserId(userId);
            for (Transactions transaction : transactions) {
                transactionsDao.delete(transaction);
            }
        });
    }

    // Asynchronous methods
    public void getTransactionsByUserId(int userId, OnTransactionCallback callback) {
        executor.execute(() -> {
            List<Transactions> transactions = transactionsDao.getTransactionsByUserId(userId);
            callback.onResult(transactions);
        });
    }

    public void getRecentTransactions(int userId, int limit, OnTransactionCallback callback) {
        executor.execute(() -> {
            List<Transactions> transactions = transactionsDao.getRecentTransactions(userId, limit);
            callback.onResult(transactions);
        });
    }

    public void getTransactionsByUserIdAndType(int userId, String type, OnTransactionCallback callback) {
        executor.execute(() -> {
            List<Transactions> transactions = transactionsDao.getTransactionsByUserIdAndType(userId, type);
            callback.onResult(transactions);
        });
    }

    public void getTransactionsByUserIdAndStatus(int userId, String status, OnTransactionCallback callback) {
        executor.execute(() -> {
            List<Transactions> transactions = transactionsDao.getTransactionsByUserIdAndStatus(userId, status);
            callback.onResult(transactions);
        });
    }

    public void getTransactionByReferenceId(String referenceId, OnSingleTransactionCallback callback) {
        executor.execute(() -> {
            Transactions transaction = transactionsDao.getTransactionByReferenceId(referenceId);
            callback.onResult(transaction);
        });
    }

    public void getTransactionCount(int userId, String type, String status, OnCountCallback callback) {
        executor.execute(() -> {
            int count = transactionsDao.getTransactionCount(userId, type, status);
            callback.onResult(count);
        });
    }

    public void getTotalAmount(int userId, String type, String status, OnAmountCallback callback) {
        executor.execute(() -> {
            Double amount = transactionsDao.getTotalAmount(userId, type, status);
            callback.onResult(amount != null ? amount : 0.0);
        });
    }

    // Synchronous methods (chỉ dùng khi cần thiết)
    public List<Transactions> getTransactionsByUserIdSync(int userId) {
        return transactionsDao.getTransactionsByUserId(userId);
    }

    public List<Transactions> getRecentTransactionsSync(int userId, int limit) {
        return transactionsDao.getRecentTransactions(userId, limit);
    }

    public Transactions getTransactionByReferenceIdSync(String referenceId) {
        return transactionsDao.getTransactionByReferenceId(referenceId);
    }

    // Callback interfaces
    public interface OnTransactionCallback {
        void onResult(List<Transactions> transactions);
    }

    public interface OnSingleTransactionCallback {
        void onResult(Transactions transaction);
    }

    public interface OnCountCallback {
        void onResult(int count);
    }

    public interface OnAmountCallback {
        void onResult(double amount);
    }

    public interface OnInsertCallback {
        void onResult(long id);
    }

    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }
}
