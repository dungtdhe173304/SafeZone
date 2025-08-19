package com.group5.safezone.repository;

import android.app.Application;

import com.group5.safezone.config.AppDatabase;
import com.group5.safezone.model.dao.AuctionRegistrationsDao;
import com.group5.safezone.model.dao.AuctionsDao;
import com.group5.safezone.model.dao.ProductDao;
import com.group5.safezone.model.dao.ProductImagesDao;
import com.group5.safezone.model.entity.AuctionRegistrations;
import com.group5.safezone.model.entity.Auctions;
import com.group5.safezone.model.entity.Product;
import com.group5.safezone.model.entity.ProductImages;
import com.group5.safezone.model.ui.AuctionItemUiModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuctionRepository {
    private final AuctionsDao auctionsDao;
    private final ProductDao productDao;
    private final ProductImagesDao productImagesDao;
    private final AuctionRegistrationsDao registrationsDao;
    private final com.group5.safezone.model.dao.UserDao userDao;
    private final ExecutorService executor;

    public AuctionRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        this.auctionsDao = database.auctionsDao();
        this.productDao = database.productDao();
        this.productImagesDao = database.productImagesDao();
        this.registrationsDao = database.auctionRegistrationsDao();
        this.userDao = database.userDao();
        this.executor = Executors.newFixedThreadPool(4);
    }

    public List<AuctionItemUiModel> loadActiveAuctionsForUser(int userId) {
        List<AuctionItemUiModel> result = new ArrayList<>();
        Date now = new Date();
        List<Auctions> all = auctionsDao.getAllAuctions();
        for (Auctions auction : all) {
            if (auction.getStatus() != null && auction.getStatus().equalsIgnoreCase("active")
                    && auction.getEndTime() != null && auction.getEndTime().after(now)) {
                Product product = productDao.getProductById(auction.getProductId());
                List<ProductImages> images = productImagesDao.getImagesByProductId(auction.getProductId());
                AuctionRegistrations reg = registrationsDao.getRegistrationByAuctionAndUser(auction.getId(), userId);
                int count = 0;
                List<com.group5.safezone.model.entity.AuctionRegistrations> regs = registrationsDao.getRegistrationsByAuctionId(auction.getId());
                if (regs != null) {
                    for (com.group5.safezone.model.entity.AuctionRegistrations r : regs) {
                        if (r.getStatus() == null || !r.getStatus().equalsIgnoreCase("cancelled")) {
                            count++;
                        }
                    }
                }
                boolean registered = reg != null && reg.getStatus() != null && !reg.getStatus().equalsIgnoreCase("cancelled");
                result.add(new AuctionItemUiModel(product, auction, images, registered, reg, count));
            }
            // Settle finished auctions: non-winners lose 20% deposit
            if (auction.getStatus() != null && auction.getStatus().equalsIgnoreCase("active")
                    && auction.getEndTime() != null && !auction.getEndTime().after(now)) {
                settleAuction(auction);
            }
        }
        return result;
    }

    public List<AuctionItemUiModel> loadActiveAuctionsForUserFiltered(int userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return loadActiveAuctionsForUser(userId);
        }
        String lower = keyword.trim().toLowerCase();
        List<AuctionItemUiModel> base = loadActiveAuctionsForUser(userId);
        List<AuctionItemUiModel> filtered = new ArrayList<>();
        for (AuctionItemUiModel item : base) {
            String name = item.getProduct() != null ? item.getProduct().getProductName() : null;
            if (name != null && name.toLowerCase().contains(lower)) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    public void registerForAuction(int auctionId, int userId, double paymentAmount) {
        AuctionRegistrations existing = registrationsDao.getRegistrationByAuctionAndUser(auctionId, userId);
        if (existing != null) {
            return; // Already registered
        }
        AuctionRegistrations registration = new AuctionRegistrations();
        registration.setAuctionId(auctionId);
        registration.setUserId(userId);
        registration.setPaymentAmount(paymentAmount);
        registration.setStatus("registered");
        registrationsDao.insert(registration);
    }

    public AuctionItemUiModel getAuctionItemById(int auctionId, int userId) {
        Auctions auction = auctionsDao.getAuctionById(auctionId);
        if (auction == null) return null;
        Product product = productDao.getProductById(auction.getProductId());
        List<ProductImages> images = productImagesDao.getImagesByProductId(auction.getProductId());
        AuctionRegistrations reg = registrationsDao.getRegistrationByAuctionAndUser(auctionId, userId);
        boolean registered = reg != null && reg.getStatus() != null && !reg.getStatus().equalsIgnoreCase("cancelled");
        int count = 0;
        List<com.group5.safezone.model.entity.AuctionRegistrations> regs = registrationsDao.getRegistrationsByAuctionId(auctionId);
        if (regs != null) {
            for (com.group5.safezone.model.entity.AuctionRegistrations r : regs) {
                if (r.getStatus() == null || !r.getStatus().equalsIgnoreCase("cancelled")) count++;
            }
        }
        return new AuctionItemUiModel(product, auction, images, registered, reg, count);
    }

    public boolean registerIfSufficientBalance(int auctionId, int userId) {
        Auctions auction = auctionsDao.getAuctionById(auctionId);
        if (auction == null) return false;
        double deposit = auction.getStartPrice() != null ? auction.getStartPrice() : 0;
        com.group5.safezone.model.entity.User user = userDao.getUserById(userId);
        if (user == null || user.getBalance() == null) return false;
        double balance = user.getBalance();
        if (balance < deposit) {
            return false;
        }
        // Deduct and register
        userDao.updateBalance(userId, -deposit);
        registerForAuction(auctionId, userId, deposit);
        return true;
    }

    private void settleAuction(Auctions auction) {
        // Mark as completed, distribute deposits: non-winners lose 20%
        List<com.group5.safezone.model.entity.AuctionRegistrations> regs = registrationsDao.getRegistrationsByAuctionId(auction.getId());
        if (regs == null || regs.isEmpty()) {
            auctionsDao.completeAuction(auction.getId(), "completed", null, null);
            return;
        }
        Integer winnerId = auction.getWinnerUserId();
        for (com.group5.safezone.model.entity.AuctionRegistrations r : regs) {
            if (winnerId != null && r.getUserId() == winnerId) continue; // winner not penalized here
            Double deposit = r.getPaymentAmount() != null ? r.getPaymentAmount() : 0;
            double penalty = deposit * 0.2;
            // Refund 80%
            userDao.updateBalance(r.getUserId(), deposit - penalty);
            registrationsDao.updateRegistrationStatus(r.getId(), "refunded_80");
        }
        auctionsDao.completeAuction(auction.getId(), "completed", auction.getWinnerUserId(), auction.getWinningBidAmount());
    }
}


