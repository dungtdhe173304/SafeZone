package com.group5.safezone.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.group5.safezone.model.ui.AuctionItemUiModel;
import com.group5.safezone.repository.AuctionRepository;

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

    public AuctionViewModel(@NonNull Application application) {
        super(application);
        repository = new AuctionRepository(application);
        executor = Executors.newFixedThreadPool(4);
    }

    public LiveData<List<AuctionItemUiModel>> getItems() { return items; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<AuctionItemUiModel> getCurrentItem() { return currentItem; }

    public void loadAuctions(int userId) {
        isLoading.setValue(true);
        executor.execute(() -> {
            try {
                List<AuctionItemUiModel> data = repository.loadActiveAuctionsForUser(userId);
                items.postValue(data);
            } catch (Exception e) {
                errorMessage.postValue("Không thể tải danh sách đấu giá: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    public void register(int auctionId, int userId, double amount) {
        executor.execute(() -> {
            try {
                repository.registerForAuction(auctionId, userId, amount);
                loadAuctions(userId);
            } catch (Exception e) {
                errorMessage.postValue("Không thể đăng ký: " + e.getMessage());
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

    public boolean registerIfEnough(int auctionId, int userId) {
        try {
            boolean ok = repository.registerIfSufficientBalance(auctionId, userId);
            loadAuctions(userId);
            return ok;
        } catch (Exception e) {
            errorMessage.postValue("Không thể đăng ký: " + e.getMessage());
            return false;
        }
    }
}


