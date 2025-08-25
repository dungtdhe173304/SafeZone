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
import com.group5.safezone.service.AuctionRegistrationService;

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
    private final AuctionRegistrationService registrationService;
    private final ExecutorService executor;
    private final Application application;

    public AuctionRepository(Application application) {
        this.application = application;
        AppDatabase database = AppDatabase.getDatabase(application);
        this.auctionsDao = database.auctionsDao();
        this.productDao = database.productDao();
        this.productImagesDao = database.productImagesDao();
        this.registrationsDao = database.auctionRegistrationsDao();
        this.userDao = database.userDao();
        this.registrationService = new AuctionRegistrationService(application);
        this.executor = Executors.newFixedThreadPool(4);
    }

    public List<AuctionItemUiModel> loadActiveAuctionsForUser(int userId) {
                try {
            List<AuctionItemUiModel> result = new ArrayList<>();
            Date now = new Date();
            
            // Debug: Log số lượng auction trong database
            List<Auctions> allAuctions = auctionsDao.getAllAuctions();
            android.util.Log.d("AuctionRepository", "Total auctions in DB: " + (allAuctions != null ? allAuctions.size() : 0));
            
            // Sử dụng getActiveAuctions() để lấy chỉ auction active và chưa hết hạn
            List<Auctions> activeAuctions = auctionsDao.getActiveAuctions(System.currentTimeMillis());
            android.util.Log.d("AuctionRepository", "Active auctions: " + (activeAuctions != null ? activeAuctions.size() : 0));
            
            // Fallback 1: Nếu getActiveAuctions() trả về ít hoặc null, thử lấy tất cả auction active
            if (activeAuctions == null || activeAuctions.size() < 1) {
                android.util.Log.d("AuctionRepository", "Fallback: Getting all active auctions manually");
                activeAuctions = auctionsDao.getAuctionsByStatus("active");
                android.util.Log.d("AuctionRepository", "Fallback active auctions: " + (activeAuctions != null ? activeAuctions.size() : 0));
            }

            // Fallback 2 (Demo): nếu vẫn không có, hiển thị tất cả auctions để không bị trống UI
            if (activeAuctions == null || activeAuctions.isEmpty()) {
                android.util.Log.d("AuctionRepository", "Fallback: No active auctions. Returning all auctions for display");
                activeAuctions = allAuctions;
            }
            
            if (activeAuctions == null) {
                return result;
            }
            
            for (Auctions auction : activeAuctions) {
                try {
                    // Kiểm tra thêm: chỉ lấy auction active và chưa hết hạn
                    if (auction != null && auction.getStatus() != null && 
                        auction.getStatus().equalsIgnoreCase("active") && 
                        (auction.getEndTime() == null || auction.getEndTime().after(now))) {
                        
                        Product product = null;
                        List<ProductImages> images = null;
                        AuctionRegistrations reg = null;
                        int count = 0;
                        
                        try {
                            product = productDao.getProductById(auction.getProductId());
                        } catch (Exception e) {
                            android.util.Log.e("AuctionRepository", "Error getting product: " + e.getMessage());
                        }
                        
                        try {
                            images = productImagesDao.getImagesByProductId(auction.getProductId());
                        } catch (Exception e) {
                            android.util.Log.e("AuctionRepository", "Error getting images: " + e.getMessage());
                        }
                        
                        try {
                            reg = registrationsDao.getRegistrationByAuctionAndUser(auction.getId(), userId);
                        } catch (Exception e) {
                            android.util.Log.e("AuctionRepository", "Error getting registration: " + e.getMessage());
                        }
                        
                        try {
                            List<AuctionRegistrations> regs = registrationsDao.getRegistrationsByAuctionId(auction.getId());
                            if (regs != null) {
                                for (AuctionRegistrations r : regs) {
                                    if (r.getStatus() == null || !r.getStatus().equalsIgnoreCase("cancelled")) {
                                        count++;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            android.util.Log.e("AuctionRepository", "Error counting registrations: " + e.getMessage());
                        }
                        
                        boolean registered = reg != null && reg.getStatus() != null && reg.getStatus().equalsIgnoreCase("approved");
                        
                        com.group5.safezone.model.entity.User seller = null;
                        String sellerName = "--";
                        String sellerEmail = "--";
                        String sellerPhone = "--";
                        
                        try {
                            seller = userDao.getUserById(auction.getSellerUserId());
                            if (seller != null) {
                                sellerName = seller.getUserName() != null ? seller.getUserName() : "--";
                                sellerEmail = seller.getEmail() != null ? seller.getEmail() : "--";
                                sellerPhone = seller.getPhone() != null ? seller.getPhone() : "--";
                            }
                        } catch (Exception e) {
                            android.util.Log.e("AuctionRepository", "Error getting seller: " + e.getMessage());
                        }
                        
                        result.add(new AuctionItemUiModel(product, auction, images, registered, reg, count, sellerName, sellerEmail, sellerPhone));
                    }
                    
                    // Settle finished auctions: non-winners lose 20% deposit
                    if (auction.getStatus() != null && auction.getStatus().equalsIgnoreCase("active")
                            && auction.getEndTime() != null && !auction.getEndTime().after(now)) {
                        try {
                            settleAuction(auction);
                        } catch (Exception e) {
                            android.util.Log.e("AuctionRepository", "Error settling auction: " + e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.e("AuctionRepository", "Error processing auction: " + e.getMessage());
                }
            }
            return result;
        } catch (Exception e) {
            android.util.Log.e("AuctionRepository", "Error in loadActiveAuctionsForUser: " + e.getMessage());
            return new ArrayList<>();
        }
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

    public void registerForAuction(int auctionId, int userId, double paymentAmount, AuctionRegistrationService.RegistrationCallback callback) {
        registrationService.registerForAuction(auctionId, userId, callback);
    }

    public void registerForAuction(int auctionId, int userId, double paymentAmount) {
        registrationService.registerForAuction(auctionId, userId, new AuctionRegistrationService.RegistrationCallback() {
            @Override
            public void onSuccess(AuctionRegistrations registration, String message) {
                // Legacy callback - do nothing
            }

            @Override
            public void onFailure(String error) {
                // Legacy callback - do nothing
            }
        });
    }

    public AuctionItemUiModel getAuctionItemById(int auctionId, int userId) {
        Auctions auction = auctionsDao.getAuctionById(auctionId);
        if (auction == null) return null;
        Product product = productDao.getProductById(auction.getProductId());
        List<ProductImages> images = productImagesDao.getImagesByProductId(auction.getProductId());
        AuctionRegistrations reg = registrationsDao.getRegistrationByAuctionAndUser(auctionId, userId);
        boolean registered = reg != null && reg.getStatus() != null && reg.getStatus().equalsIgnoreCase("approved");
        int count = 0;
        List<AuctionRegistrations> regs = registrationsDao.getRegistrationsByAuctionId(auctionId);
        if (regs != null) {
            for (AuctionRegistrations r : regs) {
                if (r.getStatus() == null || !r.getStatus().equalsIgnoreCase("cancelled")) count++;
            }
        }
        com.group5.safezone.model.entity.User seller = userDao.getUserById(auction.getSellerUserId());
        String sellerName = seller != null && seller.getUserName() != null ? seller.getUserName() : "--";
        String sellerEmail = seller != null && seller.getEmail() != null ? seller.getEmail() : "--";
        String sellerPhone = seller != null && seller.getPhone() != null ? seller.getPhone() : "--";
        return new AuctionItemUiModel(product, auction, images, registered, reg, count, sellerName, sellerEmail, sellerPhone);
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
        // Use the new service for registration
        registrationService.registerForAuction(auctionId, userId, new AuctionRegistrationService.RegistrationCallback() {
            @Override
            public void onSuccess(AuctionRegistrations registration, String message) {
                // Registration successful
            }

            @Override
            public void onFailure(String error) {
                // Registration failed
            }
        });
        return true;
    }

    private void settleAuction(Auctions auction) {
        // Mark as completed, distribute deposits: non-winners lose 20%
        List<AuctionRegistrations> regs = registrationsDao.getRegistrationsByAuctionId(auction.getId());
        if (regs == null || regs.isEmpty()) {
            auctionsDao.completeAuction(auction.getId(), "completed", null, null);
            return;
        }
        Integer winnerId = auction.getWinnerUserId();
        for (AuctionRegistrations r : regs) {
            if (winnerId != null && r.getUserId() == winnerId) continue; // winner not penalized here
            Double deposit = r.getPaymentAmount() != null ? r.getPaymentAmount() : 0;
            double penalty = deposit * 0.2;
            // Refund 80%
            userDao.updateBalance(r.getUserId(), deposit - penalty);
            registrationsDao.updateRegistrationStatus(r.getId(), "refunded_80");
        }
        auctionsDao.completeAuction(auction.getId(), "completed", auction.getWinnerUserId(), auction.getWinningBidAmount());
    }

    /**
     * Lấy service đăng ký đấu giá
     */
    public AuctionRegistrationService getRegistrationService() {
        return registrationService;
    }

    public AppDatabase getDatabase() {
        return AppDatabase.getDatabase(application);
    }
}


