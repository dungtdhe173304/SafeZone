package com.group5.safezone.view.admin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.safezone.R;
import com.group5.safezone.config.AuthInterceptor;
import com.group5.safezone.config.AppDatabase;
import com.group5.safezone.config.PasswordUtils;
import com.group5.safezone.model.entity.User;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserManagementActivity extends AppCompatActivity {

    private EditText etSearchUser;
    private Spinner spinnerRoleFilter;
    private Button btnSearch, btnAddUser;
    private RecyclerView recyclerViewUsers;
    private LinearLayout llEmptyState;
    
    private UserAdapter userAdapter;
    private List<User> allUsers = new ArrayList<>();
    private List<User> filteredUsers = new ArrayList<>();
    private ExecutorService executorService;
    private NumberFormat currencyFormatter;
    
    private static final String[] ROLES = {"Tất cả", "USER", "ADMIN"};
    private static final String[] USER_ROLES = {"USER", "ADMIN"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kiểm tra quyền admin
        if (!AuthInterceptor.checkAdminPermission(this)) {
            return;
        }

        setContentView(R.layout.activity_user_management);

        executorService = Executors.newFixedThreadPool(4);
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        setupToolbar();
        initViews();
        setupListeners();
        loadUsers();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Quản lý người dùng");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initViews() {
        etSearchUser = findViewById(R.id.etSearchUser);
        spinnerRoleFilter = findViewById(R.id.spinnerRoleFilter);
        btnSearch = findViewById(R.id.btnSearch);
        btnAddUser = findViewById(R.id.btnAddUser);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        llEmptyState = findViewById(R.id.llEmptyState);

        // Setup RecyclerView
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter();
        recyclerViewUsers.setAdapter(userAdapter);

        // Setup Role Filter Spinner
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, ROLES);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoleFilter.setAdapter(roleAdapter);
    }

    private void setupListeners() {
        btnSearch.setOnClickListener(v -> performSearch());
        btnAddUser.setOnClickListener(v -> showAddUserDialog());
        
        // Search as you type
        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Role filter change
        spinnerRoleFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                performSearch();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadUsers() {
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(this);
                allUsers = db.userDao().getAllUsers();
                
                runOnUiThread(() -> {
                    performSearch();
                });
                
            } catch (Exception e) {
                Log.e("UserManagement", "Error loading users: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi tải danh sách người dùng: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void performSearch() {
        String searchQuery = etSearchUser.getText().toString().toLowerCase().trim();
        String selectedRole = spinnerRoleFilter.getSelectedItem().toString();
        
        filteredUsers.clear();
        
        for (User user : allUsers) {
            boolean matchesSearch = searchQuery.isEmpty() || 
                user.getUserName().toLowerCase().contains(searchQuery) ||
                user.getEmail().toLowerCase().contains(searchQuery) ||
                (user.getPhone() != null && user.getPhone().toLowerCase().contains(searchQuery));
            
            boolean matchesRole = "Tất cả".equals(selectedRole) || 
                selectedRole.equals(user.getRole());
            
            if (matchesSearch && matchesRole) {
                filteredUsers.add(user);
            }
        }
        
        updateUI();
    }

    private void updateUI() {
        if (filteredUsers.isEmpty()) {
            recyclerViewUsers.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerViewUsers.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
            userAdapter.submitList(new ArrayList<>(filteredUsers));
        }
    }

    private void showAddUserDialog() {
        showUserDialog(null);
    }

    private void showEditUserDialog(User user) {
        showUserDialog(user);
    }

    private void showUserDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_edit_user, null);
        
        // Initialize views
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        EditText etUserName = dialogView.findViewById(R.id.etUserName);
        EditText etUserEmail = dialogView.findViewById(R.id.etUserEmail);
        EditText etUserPhone = dialogView.findViewById(R.id.etUserPhone);
        EditText etUserPassword = dialogView.findViewById(R.id.etUserPassword);
        EditText etUserBalance = dialogView.findViewById(R.id.etUserBalance);
        Spinner spinnerUserRole = dialogView.findViewById(R.id.spinnerUserRole);
        LinearLayout llPasswordSection = dialogView.findViewById(R.id.llPasswordSection);
        
        // Setup role spinner
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, USER_ROLES);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUserRole.setAdapter(roleAdapter);
        
        boolean isEditMode = user != null;
        
        if (isEditMode) {
            tvDialogTitle.setText("Sửa thông tin người dùng");
            etUserName.setText(user.getUserName());
            etUserEmail.setText(user.getEmail());
            etUserPhone.setText(user.getPhone());
            etUserBalance.setText(String.valueOf(user.getBalance()));
            
            // Set role
            for (int i = 0; i < USER_ROLES.length; i++) {
                if (USER_ROLES[i].equals(user.getRole())) {
                    spinnerUserRole.setSelection(i);
                    break;
                }
            }
            
            // Hide password section for edit mode
            llPasswordSection.setVisibility(View.GONE);
        } else {
            tvDialogTitle.setText("Thêm người dùng mới");
            etUserBalance.setText("0");
        }
        
        builder.setView(dialogView)
            .setPositiveButton("Lưu", null) // Set to null to prevent auto-dismiss
            .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // Override positive button to prevent auto-dismiss
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (validateUserInput(etUserName, etUserEmail, etUserPhone, 
                isEditMode ? null : etUserPassword, etUserBalance)) {
                
                if (isEditMode) {
                    updateUser(user, etUserName, etUserEmail, etUserPhone, 
                        spinnerUserRole, etUserBalance, dialog);
                } else {
                    createUser(etUserName, etUserEmail, etUserPhone, 
                        etUserPassword, spinnerUserRole, etUserBalance, dialog);
                }
            }
        });
    }

    private boolean validateUserInput(EditText etUserName, EditText etUserEmail, 
                                   EditText etUserPhone, EditText etUserPassword, 
                                   EditText etUserBalance) {
        
        if (etUserName.getText().toString().trim().isEmpty()) {
            etUserName.setError("Vui lòng nhập tên người dùng");
            return false;
        }
        
        if (etUserEmail.getText().toString().trim().isEmpty()) {
            etUserEmail.setError("Vui lòng nhập email");
            return false;
        }
        
        if (etUserPassword != null && etUserPassword.getText().toString().trim().isEmpty()) {
            etUserPassword.setError("Vui lòng nhập mật khẩu");
            return false;
        }
        
        try {
            if (!etUserBalance.getText().toString().trim().isEmpty()) {
                Double.parseDouble(etUserBalance.getText().toString().trim());
            }
        } catch (NumberFormatException e) {
            etUserBalance.setError("Số dư không hợp lệ");
            return false;
        }
        
        return true;
    }

    private void createUser(EditText etUserName, EditText etUserEmail, EditText etUserPhone,
                          EditText etUserPassword, Spinner spinnerUserRole, 
                          EditText etUserBalance, AlertDialog dialog) {
        
        String userName = etUserName.getText().toString().trim();
        String email = etUserEmail.getText().toString().trim();
        String phone = etUserPhone.getText().toString().trim();
        String password = etUserPassword.getText().toString().trim();
        String role = spinnerUserRole.getSelectedItem().toString();
        double balance = etUserBalance.getText().toString().trim().isEmpty() ? 0 : 
            Double.parseDouble(etUserBalance.getText().toString().trim());
        
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(this);
                
                // Check if email already exists
                User existingUser = db.userDao().getUserByEmail(email);
                if (existingUser != null) {
                    runOnUiThread(() -> {
                        etUserEmail.setError("Email đã tồn tại");
                    });
                    return;
                }
                
                // Create new user
                User newUser = new User();
                newUser.setUserName(userName);
                newUser.setEmail(email);
                newUser.setPhone(phone);
                newUser.setPassword(PasswordUtils.hashPassword(password));
                newUser.setRole(role);
                newUser.setBalance(balance);
                
                db.userDao().insertUser(newUser);
                
                runOnUiThread(() -> {
                    Toast.makeText(this, "Đã thêm người dùng mới thành công", 
                        Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadUsers();
                });
                
            } catch (Exception e) {
                Log.e("UserManagement", "Error creating user: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi tạo người dùng: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateUser(User user, EditText etUserName, EditText etUserEmail, 
                          EditText etUserPhone, Spinner spinnerUserRole, 
                          EditText etUserBalance, AlertDialog dialog) {
        
        String userName = etUserName.getText().toString().trim();
        String email = etUserEmail.getText().toString().trim();
        String phone = etUserPhone.getText().toString().trim();
        String role = spinnerUserRole.getSelectedItem().toString();
        double balance = etUserBalance.getText().toString().trim().isEmpty() ? 0 : 
            Double.parseDouble(etUserBalance.getText().toString().trim());
        
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(this);
                
                // Check if email already exists (excluding current user)
                User existingUser = db.userDao().getUserByEmail(email);
                if (existingUser != null && existingUser.getId() != user.getId()) {
                    runOnUiThread(() -> {
                        etUserEmail.setError("Email đã tồn tại");
                    });
                    return;
                }
                
                // Update user
                user.setUserName(userName);
                user.setEmail(email);
                user.setPhone(phone);
                user.setRole(role);
                user.setBalance(balance);
                
                db.userDao().updateUser(user);
                
                runOnUiThread(() -> {
                    Toast.makeText(this, "Đã cập nhật thông tin người dùng thành công", 
                        Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadUsers();
                });
                
            } catch (Exception e) {
                Log.e("UserManagement", "Error updating user: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi khi cập nhật người dùng: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void toggleUserStatus(User user) {
        String action = user.getIsDeleted() ? "mở khóa" : "khóa";
        String message = "Bạn có chắc chắn muốn " + action + " người dùng này?";
        
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận")
            .setMessage(message)
            .setPositiveButton(action, (dialog, which) -> {
                executorService.execute(() -> {
                    try {
                        AppDatabase db = AppDatabase.getDatabase(this);
                        user.setIsDeleted(!user.getIsDeleted());
                        db.userDao().updateUser(user);
                        
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Đã " + action + " người dùng thành công", 
                                Toast.LENGTH_SHORT).show();
                            loadUsers();
                        });
                        
                    } catch (Exception e) {
                        Log.e("UserManagement", "Error toggling user status: " + e.getMessage(), e);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Lỗi khi " + action + " người dùng: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void deleteUser(User user) {
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa người dùng này? Hành động này không thể hoàn tác.")
            .setPositiveButton("Xóa", (dialog, which) -> {
                executorService.execute(() -> {
                    try {
                        AppDatabase db = AppDatabase.getDatabase(this);
                        db.userDao().softDeleteUser(user.getId());
                        
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Đã xóa người dùng thành công", 
                                Toast.LENGTH_SHORT).show();
                            loadUsers();
                        });
                        
                    } catch (Exception e) {
                        Log.e("UserManagement", "Error deleting user: " + e.getMessage(), e);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Lỗi khi xóa người dùng: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    // User Adapter
    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
        
        private List<User> users = new ArrayList<>();
        
        public void submitList(List<User> newUsers) {
            users.clear();
            if (newUsers != null) {
                users.addAll(newUsers);
            }
            notifyDataSetChanged();
        }
        
        @Override
        public UserAdapter.ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(UserAdapter.ViewHolder holder, int position) {
            holder.bind(users.get(position));
        }
        
        @Override
        public int getItemCount() {
            return users.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView tvUserName, tvUserEmail, tvUserPhone, tvUserRole, tvUserBalance, tvUserStatus;
            private Button btnEditUser, btnToggleStatus, btnDeleteUser;
            
            public ViewHolder(View itemView) {
                super(itemView);
                tvUserName = itemView.findViewById(R.id.tvUserName);
                tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
                tvUserPhone = itemView.findViewById(R.id.tvUserPhone);
                tvUserRole = itemView.findViewById(R.id.tvUserRole);
                tvUserBalance = itemView.findViewById(R.id.tvUserBalance);
                tvUserStatus = itemView.findViewById(R.id.tvUserStatus);
                btnEditUser = itemView.findViewById(R.id.btnEditUser);
                btnToggleStatus = itemView.findViewById(R.id.btnToggleStatus);
                btnDeleteUser = itemView.findViewById(R.id.btnDeleteUser);
            }
            
            public void bind(User user) {
                tvUserName.setText(user.getUserName());
                tvUserEmail.setText(user.getEmail());
                tvUserPhone.setText(user.getPhone() != null ? user.getPhone() : "Chưa cập nhật");
                tvUserRole.setText(user.getRole());
                tvUserBalance.setText(currencyFormatter.format(user.getBalance()));
                
                // Set status
                if (user.getIsDeleted()) {
                    tvUserStatus.setText("Đã khóa");
                    tvUserStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    btnToggleStatus.setText("Mở khóa");
                    btnToggleStatus.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_dark));
                } else {
                    tvUserStatus.setText("Hoạt động");
                    tvUserStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    btnToggleStatus.setText("Khóa");
                    btnToggleStatus.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_orange_dark));
                }
                
                // Set role badge color
                if ("ADMIN".equals(user.getRole())) {
                    tvUserRole.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_dark));
                } else {
                    tvUserRole.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_blue_dark));
                }
                
                // Set button listeners
                btnEditUser.setOnClickListener(v -> showEditUserDialog(user));
                btnToggleStatus.setOnClickListener(v -> toggleUserStatus(user));
                btnDeleteUser.setOnClickListener(v -> deleteUser(user));
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
