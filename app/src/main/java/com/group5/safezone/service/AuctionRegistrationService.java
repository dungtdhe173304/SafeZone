package com.group5.safezone.service;

import android.app.Application;
import android.util.Log;

import com.group5.safezone.config.AppDatabase;
import com.group5.safezone.model.dao.AuctionRegistrationsDao;
import com.group5.safezone.model.dao.AuctionsDao;
import com.group5.safezone.model.dao.UserDao;
import com.group5.safezone.model.entity.AuctionRegistrations;
import com.group5.safezone.model.entity.Auctions;
import com.group5.safezone.model.entity.User;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuctionRegistrationService {
    private static final String TAG = "AuctionRegistrationService";
    
    private final AuctionRegistrationsDao registrationsDao;
    private final AuctionsDao auctionsDao;
    private final UserDao userDao;
    private final ExecutorService executor;

    public AuctionRegistrationService(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        this.registrationsDao = database.auctionRegistrationsDao();
        this.auctionsDao = database.auctionsDao();
        this.userDao = database.userDao();
        this.executor = Executors.newFixedThreadPool(4);
    }

    /**
     * Đăng ký tham gia đấu giá
     * @param auctionId ID của phiên đấu giá
     * @param userId ID của người dùng
     * @param callback Callback để trả về kết quả
     */
    public void registerForAuction(int auctionId, int userId, RegistrationCallback callback) {
        executor.execute(() -> {
            try {
                // Kiểm tra xem người dùng đã đăng ký chưa
                AuctionRegistrations existing = registrationsDao.getRegistrationByAuctionAndUser(auctionId, userId);
                if (existing != null) {
                    if ("approved".equals(existing.getStatus())) {
                        callback.onFailure("Bạn đã đăng ký tham gia phiên đấu giá này");
                        return;
                    } else if ("pending".equals(existing.getStatus())) {
                        callback.onFailure("Bạn đã đăng ký và đang chờ duyệt");
                        return;
                    }
                }

                // Lấy thông tin phiên đấu giá
                Auctions auction = auctionsDao.getAuctionById(auctionId);
                if (auction == null) {
                    callback.onFailure("Không tìm thấy phiên đấu giá");
                    return;
                }

                // Kiểm tra trạng thái phiên đấu giá
                if (!"active".equals(auction.getStatus())) {
                    callback.onFailure("Phiên đấu giá không còn hoạt động");
                    return;
                }

                // Kiểm tra thời gian
                Date now = new Date();
                if (auction.getStartTime() != null && now.before(auction.getStartTime())) {
                    callback.onFailure("Phiên đấu giá chưa bắt đầu");
                    return;
                }
                if (auction.getEndTime() != null && now.after(auction.getEndTime())) {
                    callback.onFailure("Phiên đấu giá đã kết thúc");
                    return;
                }

                // Kiểm tra số dư và trừ cọc ngay khi người dùng đồng ý
                double requiredAmount = auction.getStartPrice() != null ? auction.getStartPrice() : 0;
                User user = userDao.getUserById(userId);
                if (user == null) {
                    callback.onFailure("Không tìm thấy thông tin người dùng");
                    return;
                }
                double currentBalance = user.getBalance() != null ? user.getBalance() : 0;
                if (currentBalance < requiredAmount) {
                    callback.onFailure("Số dư không đủ để đặt cọc tham gia phiên đấu giá");
                    return;
                }

                // Trừ tiền cọc
                userDao.updateBalance(userId, -requiredAmount);

                // Tạo đăng ký mới (đã duyệt luôn)
                AuctionRegistrations registration = new AuctionRegistrations();
                registration.setAuctionId(auctionId);
                registration.setUserId(userId);
                registration.setPaymentAmount(requiredAmount);
                registration.setStatus("approved");
                registration.setCreatedAt(new Date());
                registration.setUpdatedAt(new Date());

                // Lưu vào database
                registrationsDao.insert(registration);
                
                Log.d(TAG, "Đăng ký thành công và đã trừ cọc: Auction ID=" + auctionId + ", User ID=" + userId);
                callback.onSuccess(registration, "Đăng ký thành công! Đã trừ tiền cọc và bạn có thể vào phòng đấu giá.");

            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi đăng ký: " + e.getMessage(), e);
                callback.onFailure("Lỗi khi đăng ký: " + e.getMessage());
            }
        });
    }

    /**
     * Admin duyệt đăng ký tham gia đấu giá
     * @param registrationId ID của đăng ký
     * @param approved true nếu duyệt, false nếu từ chối
     * @param callback Callback để trả về kết quả
     */
    public void approveRegistration(int registrationId, boolean approved, ApprovalCallback callback) {
        executor.execute(() -> {
            try {
                // Lấy thông tin đăng ký
                AuctionRegistrations registration = registrationsDao.getRegistrationById(registrationId);
                if (registration == null) {
                    callback.onFailure("Không tìm thấy đăng ký");
                    return;
                }

                if (approved) {
                    // Kiểm tra số dư của người dùng
                    User user = userDao.getUserById(registration.getUserId());
                    if (user == null) {
                        callback.onFailure("Không tìm thấy thông tin người dùng");
                        return;
                    }

                    double requiredAmount = registration.getPaymentAmount() != null ? registration.getPaymentAmount() : 0;
                    double currentBalance = user.getBalance() != null ? user.getBalance() : 0;

                    if (currentBalance < requiredAmount) {
                        callback.onFailure("Người dùng không đủ số dư để tham gia đấu giá");
                        return;
                    }

                    // Trừ tiền cọc
                    userDao.updateBalance(registration.getUserId(), -requiredAmount);
                    
                    // Cập nhật trạng thái đăng ký
                    registration.setStatus("approved");
                    registration.setUpdatedAt(new Date());
                    registrationsDao.update(registration);

                    Log.d(TAG, "Duyệt đăng ký thành công: Registration ID=" + registrationId);
                    callback.onSuccess(registration, "Duyệt đăng ký thành công! Người dùng đã được trừ tiền cọc.");

                } else {
                    // Từ chối đăng ký
                    registration.setStatus("rejected");
                    registration.setUpdatedAt(new Date());
                    registrationsDao.update(registration);

                    Log.d(TAG, "Từ chối đăng ký: Registration ID=" + registrationId);
                    callback.onSuccess(registration, "Đã từ chối đăng ký tham gia đấu giá.");
                }

            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi duyệt đăng ký: " + e.getMessage(), e);
                callback.onFailure("Lỗi khi duyệt đăng ký: " + e.getMessage());
            }
        });
    }

    /**
     * Kiểm tra xem người dùng có thể tham gia đấu giá không
     * @param auctionId ID của phiên đấu giá
     * @param userId ID của người dùng
     * @param callback Callback để trả về kết quả
     */
    public void checkEligibility(int auctionId, int userId, EligibilityCallback callback) {
        executor.execute(() -> {
            try {
                // Kiểm tra đăng ký
                AuctionRegistrations registration = registrationsDao.getRegistrationByAuctionAndUser(auctionId, userId);
                if (registration == null) {
                    callback.onNotRegistered("Bạn chưa đăng ký tham gia phiên đấu giá này");
                    return;
                }

                if ("approved".equals(registration.getStatus())) {
                    callback.onEligible(registration, "Bạn có thể tham gia đấu giá");
                } else if ("pending".equals(registration.getStatus())) {
                    callback.onPending(registration, "Đăng ký của bạn đang chờ duyệt");
                } else if ("rejected".equals(registration.getStatus())) {
                    callback.onRejected(registration, "Đăng ký của bạn đã bị từ chối");
                } else {
                    callback.onNotEligible(registration, "Trạng thái đăng ký không hợp lệ");
                }

            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi kiểm tra điều kiện: " + e.getMessage(), e);
                callback.onError("Lỗi khi kiểm tra điều kiện: " + e.getMessage());
            }
        });
    }

    /**
     * Hủy đăng ký tham gia đấu giá
     * @param registrationId ID của đăng ký
     * @param callback Callback để trả về kết quả
     */
    public void cancelRegistration(int registrationId, CancellationCallback callback) {
        executor.execute(() -> {
            try {
                AuctionRegistrations registration = registrationsDao.getRegistrationById(registrationId);
                if (registration == null) {
                    callback.onFailure("Không tìm thấy đăng ký");
                    return;
                }

                if ("approved".equals(registration.getStatus())) {
                    // Nếu đã được duyệt, hoàn tiền cọc
                    double refundAmount = registration.getPaymentAmount() != null ? registration.getPaymentAmount() : 0;
                    userDao.updateBalance(registration.getUserId(), refundAmount);
                    
                    Log.d(TAG, "Hoàn tiền cọc: User ID=" + registration.getUserId() + ", Amount=" + refundAmount);
                }

                // Cập nhật trạng thái
                registration.setStatus("cancelled");
                registration.setUpdatedAt(new Date());
                registrationsDao.update(registration);

                Log.d(TAG, "Hủy đăng ký thành công: Registration ID=" + registrationId);
                callback.onSuccess(registration, "Hủy đăng ký thành công!");

            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi hủy đăng ký: " + e.getMessage(), e);
                callback.onFailure("Lỗi khi hủy đăng ký: " + e.getMessage());
            }
        });
    }

    // Callback interfaces
    public interface RegistrationCallback {
        void onSuccess(AuctionRegistrations registration, String message);
        void onFailure(String error);
    }

    public interface ApprovalCallback {
        void onSuccess(AuctionRegistrations registration, String message);
        void onFailure(String error);
    }

    public interface EligibilityCallback {
        void onEligible(AuctionRegistrations registration, String message);
        void onPending(AuctionRegistrations registration, String message);
        void onRejected(AuctionRegistrations registration, String message);
        void onNotRegistered(String message);
        void onNotEligible(AuctionRegistrations registration, String message);
        void onError(String error);
    }

    public interface CancellationCallback {
        void onSuccess(AuctionRegistrations registration, String message);
        void onFailure(String error);
    }

    public void shutdown() {
        executor.shutdown();
    }
}
