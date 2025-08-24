package com.group5.safezone.view.livestreaming;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.group5.safezone.config.SessionManager;
import com.group5.safezone.model.entity.Transactions;
import com.group5.safezone.model.entity.User;
import com.group5.safezone.repository.TransactionRepository;
import com.group5.safezone.repository.UserRepository;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.util.Log;

public class DonateManager {

    private static final double HOST_PERCENTAGE = 0.5; // Host nhận 50%
    private static final double PLATFORM_PERCENTAGE = 0.5; // Platform giữ 50%

    private Context context;
    private TransactionRepository transactionRepository;
    private UserRepository userRepository;
    private SessionManager sessionManager;
    private ExecutorService executor;

    public DonateManager(Context context) {
        this.context = context;
        this.transactionRepository = new TransactionRepository((Application) context.getApplicationContext());
        this.userRepository = new UserRepository((Application) context.getApplicationContext());
        this.sessionManager = new SessionManager(context);
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void processDonate(int amount, String hostId, String liveId, OnDonateResultListener listener) {
        try {
            if (amount <= 0) {
                if (listener != null) {
                    listener.onDonateFailed("Số tiền không hợp lệ");
                }
                return;
            }

            if (hostId == null || liveId == null) {
                if (listener != null) {
                    listener.onDonateFailed("Thông tin livestream không hợp lệ");
                }
                return;
            }

            // Kiểm tra balance của người donate
            int currentUserId = sessionManager.getUserId();
            executor.execute(() -> {
                try {
                    User user = userRepository.getUserById(currentUserId);
                    if (user == null) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (listener != null) {
                                listener.onDonateFailed("Không tìm thấy thông tin người dùng");
                            }
                        });
                        return;
                    }

                    double currentBalance = user.getBalance() != null ? user.getBalance() : 0;
                    if (currentBalance < amount) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (listener != null) {
                                listener.onDonateFailed("Số dư không đủ để donate");
                            }
                        });
                        return;
                    }

                    // Thực hiện donate
                    performDonate(amount, hostId, liveId, currentUserId, listener);
                } catch (Exception e) {
                    Log.e("DonateManager", "Error processing donate: " + e.getMessage());
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (listener != null) {
                            listener.onDonateFailed("Lỗi xử lý donate: " + e.getMessage());
                        }
                    });
                }
            });
        } catch (Exception e) {
            Log.e("DonateManager", "Error in processDonate: " + e.getMessage());
            if (listener != null) {
                listener.onDonateFailed("Lỗi: " + e.getMessage());
            }
        }
    }

    private void performDonate(int amount, String hostId, String liveId, int donorId, OnDonateResultListener listener) {
        try {
            // Tính toán số tiền host nhận được
            double hostAmount = amount * HOST_PERCENTAGE;
            double platformAmount = amount * PLATFORM_PERCENTAGE;

            // Tạo transaction cho người donate (trừ tiền)
            Transactions donateTransaction = new Transactions();
            donateTransaction.setUserId(donorId);
            donateTransaction.setTransactionType("DONATE");
            donateTransaction.setAmount(-(double)amount); // Số âm vì trừ tiền
            donateTransaction.setDescription("Donate cho livestream " + liveId);
            donateTransaction.setStatus(Transactions.STATUS_SUCCESS);
            donateTransaction.setReferenceId("donate_" + System.currentTimeMillis());
            donateTransaction.setCreatedAt(new Date());
            donateTransaction.setUpdatedAt(new Date());

            // Tạo transaction cho host (nhận tiền)
            // Lưu ý: hostId có thể là string như "user_3", không phải số
            // Chúng ta sẽ lưu hostId dưới dạng string trong description hoặc referenceId
            Transactions hostTransaction = new Transactions();
            
            // Tìm userId thực của host từ database
            // Trong trường hợp này, chúng ta cần tìm user có username hoặc identifier tương ứng
            executor.execute(() -> {
                try {
                    // Tìm user bằng username hoặc identifier
                    User hostUser = findHostUser(hostId);
                    if (hostUser == null) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (listener != null) {
                                listener.onDonateFailed("Không tìm thấy thông tin host");
                            }
                        });
                        return;
                    }

                    // Sử dụng userId thực của host
                    int actualHostId = hostUser.getId();
                    
                    // Cập nhật hostTransaction với userId thực
                    hostTransaction.setUserId(actualHostId);
                    hostTransaction.setTransactionType("DONATE_RECEIVED");
                    hostTransaction.setAmount(hostAmount); // Số dương vì nhận tiền
                    hostTransaction.setDescription("Nhận donate từ livestream " + liveId);
                    hostTransaction.setStatus(Transactions.STATUS_SUCCESS);
                    hostTransaction.setReferenceId("donate_" + System.currentTimeMillis());
                    hostTransaction.setCreatedAt(new Date());
                    hostTransaction.setUpdatedAt(new Date());

                    // Lưu transactions
                    transactionRepository.insertTransaction(donateTransaction, donateId -> {
                        try {
                            transactionRepository.insertTransaction(hostTransaction, hostReceiveId -> {
                                try {
                                    // Cập nhật balance người donate
                                    executor.execute(() -> {
                                        try {
                                            User donor = userRepository.getUserById(donorId);
                                            if (donor != null) {
                                                double newDonorBalance = donor.getBalance() - amount;
                                                donor.setBalance(newDonorBalance);
                                                userRepository.updateUser(donor);
                                            }
                                        } catch (Exception e) {
                                            Log.e("DonateManager", "Error updating donor balance: " + e.getMessage());
                                        }
                                    });

                                    // Cập nhật balance host
                                    executor.execute(() -> {
                                        try {
                                            User host = userRepository.getUserById(actualHostId);
                                            if (host != null) {
                                                double newHostBalance = host.getBalance() + hostAmount;
                                                host.setBalance(newHostBalance);
                                                userRepository.updateUser(host);
                                            }
                                        } catch (Exception e) {
                                            Log.e("DonateManager", "Error updating host balance: " + e.getMessage());
                                        }
                                    });

                                    // Thông báo thành công
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        try {
                                            if (listener != null) {
                                                listener.onDonateSuccess(amount, hostAmount);
                                            }
                                            
                                            Toast.makeText(context, 
                                                "Donate thành công! Host nhận được " + formatCurrency(hostAmount), 
                                                Toast.LENGTH_LONG).show();
                                        } catch (Exception e) {
                                            Log.e("DonateManager", "Error showing success message: " + e.getMessage());
                                        }
                                    });
                                } catch (Exception e) {
                                    Log.e("DonateManager", "Error in host transaction callback: " + e.getMessage());
                                    new Handler(Looper.getMainLooper()).post(() -> {
                                        if (listener != null) {
                                            listener.onDonateFailed("Lỗi xử lý giao dịch: " + e.getMessage());
                                        }
                                    });
                                }
                            });
                        } catch (Exception e) {
                            Log.e("DonateManager", "Error in donate transaction callback: " + e.getMessage());
                            new Handler(Looper.getMainLooper()).post(() -> {
                                if (listener != null) {
                                    listener.onDonateFailed("Lỗi xử lý giao dịch: " + e.getMessage());
                                }
                            });
                        }
                    });
                } catch (Exception e) {
                    Log.e("DonateManager", "Error finding host user: " + e.getMessage());
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (listener != null) {
                            listener.onDonateFailed("Lỗi tìm thông tin host: " + e.getMessage());
                        }
                    });
                }
            });
        } catch (Exception e) {
            Log.e("DonateManager", "Error in performDonate: " + e.getMessage());
            new Handler(Looper.getMainLooper()).post(() -> {
                if (listener != null) {
                    listener.onDonateFailed("Lỗi thực hiện donate: " + e.getMessage());
                }
            });
        }
    }

    // Tìm user host dựa trên hostId string
    private User findHostUser(String hostId) {
        try {
            Log.d("DonateManager", "Finding host user for hostId: " + hostId);
            
            // Nếu hostId là số, thử parse
            if (hostId.matches("\\d+")) {
                Log.d("DonateManager", "hostId is numeric: " + hostId);
                return userRepository.getUserById(Integer.parseInt(hostId));
            }
            
            // Nếu hostId có format "user_X", tìm user có id = X
            if (hostId.startsWith("user_")) {
                String userIdStr = hostId.substring(5); // Lấy phần sau "user_"
                Log.d("DonateManager", "Extracted userId from user_X: " + userIdStr);
                
                if (userIdStr.matches("\\d+")) {
                    int userId = Integer.parseInt(userIdStr);
                    Log.d("DonateManager", "Parsed userId: " + userId);
                    return userRepository.getUserById(userId);
                }
            }
            
            // Fallback: sử dụng userId = 1 (admin/default user)
            Log.d("DonateManager", "Using fallback userId = 1");
            return userRepository.getUserById(1);
            
        } catch (Exception e) {
            Log.e("DonateManager", "Error finding host user: " + e.getMessage());
            // Fallback: sử dụng userId = 1
            try {
                return userRepository.getUserById(1);
            } catch (Exception fallbackError) {
                Log.e("DonateManager", "Fallback error: " + fallbackError.getMessage());
                return null;
            }
        }
    }

    private String formatCurrency(double amount) {
        return String.format("%,.0f VNĐ", amount);
    }

    public interface OnDonateResultListener {
        void onDonateSuccess(int donatedAmount, double hostReceivedAmount);
        void onDonateFailed(String errorMessage);
    }
}
