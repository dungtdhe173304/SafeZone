package com.group5.safezone.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.group5.safezone.model.entity.User;
import com.group5.safezone.repository.UserRepository;
import com.group5.safezone.config.PasswordUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthViewModel extends AndroidViewModel {
    private UserRepository userRepository;
    private ExecutorService executor;

    private MutableLiveData<User> loginResult;
    private MutableLiveData<Boolean> registerResult;
    private MutableLiveData<Boolean> isLoading;
    private MutableLiveData<String> errorMessage;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        executor = Executors.newFixedThreadPool(4);

        loginResult = new MutableLiveData<>();
        registerResult = new MutableLiveData<>();
        isLoading = new MutableLiveData<>(false);
        errorMessage = new MutableLiveData<>();
    }

    public LiveData<User> getLoginResult() {
        return loginResult;
    }

    public LiveData<Boolean> getRegisterResult() {
        return registerResult;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void login(String emailOrUsername, String password) {
        isLoading.setValue(true);
        executor.execute(() -> {
            try {
                User user = null;
                
                // Kiểm tra xem input là email hay username
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(emailOrUsername).matches()) {
                    // Nếu là email format, tìm user bằng email
                    user = userRepository.getUserByEmail(emailOrUsername);
                    if (user == null) {
                        errorMessage.postValue("Email không tồn tại trong hệ thống");
                        isLoading.postValue(false);
                        return;
                    }
                } else {
                    // Nếu không phải email format, tìm user bằng username
                    user = userRepository.getUserByUserName(emailOrUsername);
                    if (user == null) {
                        errorMessage.postValue("Tên đăng nhập không tồn tại trong hệ thống");
                        isLoading.postValue(false);
                        return;
                    }
                }

                // Kiểm tra mật khẩu
                if (!PasswordUtils.verifyPassword(password, user.getPassword())) {
                    errorMessage.postValue("Mật khẩu không chính xác");
                    isLoading.postValue(false);
                    return;
                }

                // Kiểm tra trạng thái tài khoản
                if ("BANNED".equals(user.getStatus())) {
                    errorMessage.postValue("Tài khoản của bạn đã bị khóa");
                    isLoading.postValue(false);
                    return;
                }

                if ("INACTIVE".equals(user.getStatus())) {
                    errorMessage.postValue("Tài khoản chưa được kích hoạt");
                    isLoading.postValue(false);
                    return;
                }

                // Đăng nhập thành công
                loginResult.postValue(user);
                isLoading.postValue(false);

            } catch (Exception e) {
                errorMessage.postValue("Lỗi đăng nhập: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public void register(String userName, String email, String password, String phone) {
        isLoading.setValue(true);
        executor.execute(() -> {
            try {
                // Kiểm tra email đã tồn tại
                User existingUserByEmail = userRepository.getUserByEmail(email);
                if (existingUserByEmail != null) {
                    errorMessage.postValue("Email đã được sử dụng");
                    isLoading.postValue(false);
                    return;
                }

                // Kiểm tra username đã tồn tại
                User existingUserByUsername = userRepository.getUserByUserName(userName);
                if (existingUserByUsername != null) {
                    errorMessage.postValue("Tên đăng nhập đã được sử dụng");
                    isLoading.postValue(false);
                    return;
                }

                // Tạo user mới
                User newUser = new User();
                newUser.setUserName(userName);
                newUser.setEmail(email);
                newUser.setPassword(PasswordUtils.hashPassword(password));
                newUser.setPhone(phone);
                newUser.setRole("USER");
                newUser.setStatus("PENDING");
                newUser.setBalance(0.0);
                newUser.setIsVerify(false);

                userRepository.insertUser(newUser);
                registerResult.postValue(true);
                isLoading.postValue(false);

            } catch (Exception e) {
                errorMessage.postValue("Lỗi đăng ký: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (executor != null) {
            executor.shutdown();
        }
    }
}
