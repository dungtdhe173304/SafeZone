package com.group5.safezone.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.group5.safezone.model.entity.AuctionRegistrations;
import com.group5.safezone.model.ui.AuctionItemUiModel;
import com.group5.safezone.repository.AuctionRepository;
import com.group5.safezone.service.AuctionRegistrationService;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuctionViewModel extends AndroidViewModel {
    private final AuctionRepository repository;
    private final ExecutorService executor;
    private final MutableLiveData<List<AuctionItemUiModel>> items = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<AuctionItemUiModel> currentItem = new MutableLiveData<>();
    private final MutableLiveData<String> registrationMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isRegistrationLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> registrationSuccess = new MutableLiveData<>(false);

    public AuctionViewModel(@NonNull Application application) {
        super(application);
        repository = new AuctionRepository(application);
        executor = Executors.newFixedThreadPool(4);
    }

    public LiveData<List<AuctionItemUiModel>> getItems() { return items; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<AuctionItemUiModel> getCurrentItem() { return currentItem; }
    public LiveData<String> getRegistrationMessage() { return registrationMessage; }
    public LiveData<Boolean> getIsRegistrationLoading() { return isRegistrationLoading; }
    public LiveData<Boolean> getRegistrationSuccess() { return registrationSuccess; }
    
    public AuctionRepository getRepository() {
        return repository;
    }

    public void resetRegistrationFlag() {
        registrationSuccess.postValue(false);
    }

    public void loadAuctions(int userId) {
        android.util.Log.d("AuctionViewModel", "Loading auctions for user: " + userId);
        isLoading.postValue(true);
        executor.execute(() -> {
            try {
                List<AuctionItemUiModel> data = repository.loadActiveAuctionsForUser(userId);
                android.util.Log.d("AuctionViewModel", "Repository returned " + (data != null ? data.size() : "null") + " items");
                items.postValue(data);
            } catch (Exception e) {
                android.util.Log.e("AuctionViewModel", "Error loading auctions: " + e.getMessage(), e);
                errorMessage.postValue("Không thể tải danh sách đấu giá: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    public void register(int auctionId, int userId, double amount) {
        isRegistrationLoading.postValue(true);
        repository.registerForAuction(auctionId, userId, amount, new AuctionRegistrationService.RegistrationCallback() {
            @Override
            public void onSuccess(AuctionRegistrations registration, String message) {
                registrationMessage.postValue(message);
                registrationSuccess.postValue(true);
                loadAuctions(userId);
                isRegistrationLoading.postValue(false);
            }

            @Override
            public void onFailure(String error) {
                errorMessage.postValue(error);
                registrationSuccess.postValue(false);
                isRegistrationLoading.postValue(false);
            }
        });
    }

    public void searchAuctions(int userId, String keyword) {
        isLoading.postValue(true);
        executor.execute(() -> {
            try {
                List<AuctionItemUiModel> data = repository.loadActiveAuctionsForUserFiltered(userId, keyword);
                items.postValue(data);
            } catch (Exception e) {
                errorMessage.postValue("Không thể tìm kiếm: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    public void loadItem(int auctionId, int userId) {
        isLoading.postValue(true);
        executor.execute(() -> {
            try {
                AuctionItemUiModel data = repository.getAuctionItemById(auctionId, userId);
                currentItem.postValue(data);
            } catch (Exception e) {
                errorMessage.postValue("Không thể tải phiên đấu giá: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    public void loadItem(int auctionId) {
        isLoading.postValue(true);
        executor.execute(() -> {
            try {
                // Load auction without user context for auction room
                AuctionItemUiModel data = repository.getAuctionItemById(auctionId, -1);
                currentItem.postValue(data);
            } catch (Exception e) {
                errorMessage.postValue("Không thể tải phiên đấu giá: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    public boolean registerIfEnough(int auctionId, int userId) {
        try {
            boolean ok = repository.registerIfSufficientBalance(auctionId, userId);
            if (ok) {
                loadAuctions(userId);
            }
            return ok;
        } catch (Exception e) {
            errorMessage.postValue("Không thể đăng ký: " + e.getMessage());
            return false;
        }
    }

    /**
     * Kiểm tra xem người dùng có thể tham gia đấu giá không
     */
    public void checkEligibility(int auctionId, int userId) {
        isRegistrationLoading.postValue(true);
        repository.getRegistrationService().checkEligibility(auctionId, userId, new AuctionRegistrationService.EligibilityCallback() {
            @Override
            public void onEligible(AuctionRegistrations registration, String message) {
                registrationMessage.postValue(message);
                isRegistrationLoading.postValue(false);
            }

            @Override
            public void onPending(AuctionRegistrations registration, String message) {
                registrationMessage.postValue(message);
                isRegistrationLoading.postValue(false);
            }

            @Override
            public void onRejected(AuctionRegistrations registration, String message) {
                errorMessage.postValue(message);
                isRegistrationLoading.postValue(false);
            }

            @Override
            public void onNotRegistered(String message) {
                errorMessage.postValue(message);
                isRegistrationLoading.postValue(false);
            }

            @Override
            public void onNotEligible(AuctionRegistrations registration, String message) {
                errorMessage.postValue(message);
                isRegistrationLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue(error);
                isRegistrationLoading.postValue(false);
            }
        });
    }
}


