package com.group5.safezone.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.group5.safezone.model.entity.Auctions;

import java.util.List;

@Dao
public interface AuctionsDao {
    @Query("SELECT * FROM auctions")
    List<Auctions> getAllAuctions();

    @Query("SELECT * FROM auctions WHERE id = :id")
    Auctions getAuctionById(int id);

    @Query("SELECT * FROM auctions WHERE productId = :productId")
    Auctions getAuctionByProductId(String productId);

    @Query("SELECT * FROM auctions WHERE sellerUserId = :userId")
    List<Auctions> getAuctionsBySellerUserId(int userId);

    @Query("SELECT * FROM auctions WHERE status = :status")
    List<Auctions> getAuctionsByStatus(String status);

    @Query("SELECT * FROM auctions WHERE status = 'active' AND endTime > datetime('now')")
    List<Auctions> getActiveAuctions();

    @Query("SELECT * FROM auctions WHERE status = 'active' AND endTime <= datetime('now')")
    List<Auctions> getExpiredAuctions();

    @Query("UPDATE auctions SET currentHighestBid = :bidAmount, highestBidderUserId = :bidderId WHERE id = :auctionId")
    void updateHighestBid(int auctionId, double bidAmount, int bidderId);

    @Query("UPDATE auctions SET status = :status, winnerUserId = :winnerId, winningBidAmount = :amount WHERE id = :auctionId")
    void completeAuction(int auctionId, String status, Integer winnerId, Double amount);

    @Insert
    void insert(Auctions auctions);

    @Update
    void update(Auctions auctions);

    @Delete
    void delete(Auctions auctions);
}
