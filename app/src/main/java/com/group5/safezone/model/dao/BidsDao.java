package com.group5.safezone.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.group5.safezone.model.entity.Bids;

import java.util.List;

@Dao
public interface BidsDao {
    @Query("SELECT * FROM bids")
    List<Bids> getAllBids();

    @Query("SELECT * FROM bids WHERE id = :id")
    Bids getBidById(int id);

    @Query("SELECT * FROM bids WHERE auctionId = :auctionId ORDER BY bidAmount DESC")
    List<Bids> getBidsByAuctionId(int auctionId);

    @Query("SELECT * FROM bids WHERE bidderUserId = :userId ORDER BY bidTime DESC")
    List<Bids> getBidsByUserId(int userId);

    @Query("SELECT * FROM bids WHERE auctionId = :auctionId ORDER BY bidAmount DESC LIMIT 1")
    Bids getHighestBidByAuctionId(int auctionId);

    @Query("SELECT * FROM bids WHERE auctionId = :auctionId AND bidderUserId = :userId ORDER BY bidAmount DESC LIMIT 1")
    Bids getHighestBidByUserInAuction(int auctionId, int userId);

    @Query("SELECT * FROM bids WHERE status = :status")
    List<Bids> getBidsByStatus(String status);

    @Query("UPDATE bids SET status = :status WHERE id = :id")
    void updateBidStatus(int id, String status);

    @Insert
    void insert(Bids bid);

    @Update
    void update(Bids bid);

    @Delete
    void delete(Bids bid);
}
