package com.group5.safezone.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;


import com.group5.safezone.model.entity.AuctionRegistrations;

import java.util.List;

@Dao
public interface AuctionRegistrationsDao {
    @Query("SELECT * FROM auctionRegistrations")
    List<AuctionRegistrations> getAllRegistrations();

    @Query("SELECT * FROM auctionRegistrations WHERE id = :id")
    AuctionRegistrations getRegistrationById(int id);

    @Query("SELECT * FROM auctionRegistrations WHERE auctionId = :auctionId")
    List<AuctionRegistrations> getRegistrationsByAuctionId(int auctionId);

    @Query("SELECT * FROM auctionRegistrations WHERE userId = :userId")
    List<AuctionRegistrations> getRegistrationsByUserId(int userId);

    @Query("SELECT * FROM auctionRegistrations WHERE auctionId = :auctionId AND userId = :userId")
    AuctionRegistrations getRegistrationByAuctionAndUser(int auctionId, int userId);

    @Query("SELECT * FROM auctionRegistrations WHERE status = :status")
    List<AuctionRegistrations> getRegistrationsByStatus(String status);

    @Query("UPDATE auctionRegistrations SET status = :status WHERE id = :id")
    void updateRegistrationStatus(int id, String status);

    @Insert
    void insert(AuctionRegistrations registration);

    @Update
    void update(AuctionRegistrations registration);

    @Delete
    void delete(AuctionRegistrations registration);
}
