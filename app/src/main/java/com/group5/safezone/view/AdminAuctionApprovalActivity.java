package com.group5.safezone.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.safezone.R;
import com.group5.safezone.config.SessionManager;
import com.group5.safezone.model.entity.AuctionRegistrations;
import com.group5.safezone.service.AuctionRegistrationService;
import com.group5.safezone.viewmodel.AuctionViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminAuctionApprovalActivity extends AppCompatActivity {
    
    private AuctionViewModel viewModel;
    private SessionManager sessionManager;
    private RecyclerView recyclerView;
    private RegistrationAdapter adapter;
    private List<AuctionRegistrations> registrations = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_auction_approval);
        
        // Khởi tạo ViewModel và SessionManager
        viewModel = new ViewModelProvider(this).get(AuctionViewModel.class);
        sessionManager = new SessionManager(this);
        
        // Kiểm tra quyền admin
        if (!"admin".equals(sessionManager.getUserRole())) {
            Toast.makeText(this, "Bạn không có quyền truy cập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        loadPendingRegistrations();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new RegistrationAdapter();
        recyclerView.setAdapter(adapter);
    }
    
    private void loadPendingRegistrations() {
        // TODO: Load pending registrations from database
        // For now, we'll use dummy data
        // In real implementation, you should load from AuctionRepository
    }
    
    private void approveRegistration(AuctionRegistrations registration, boolean approved) {
        viewModel.getRepository().getRegistrationService().approveRegistration(
            registration.getId(), 
            approved, 
            new AuctionRegistrationService.ApprovalCallback() {
                @Override
                public void onSuccess(AuctionRegistrations registration, String message) {
                    Toast.makeText(AdminAuctionApprovalActivity.this, message, Toast.LENGTH_LONG).show();
                    // Refresh the list
                    loadPendingRegistrations();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(AdminAuctionApprovalActivity.this, error, Toast.LENGTH_LONG).show();
                }
            }
        );
    }
    
    // Adapter for RecyclerView
    private class RegistrationAdapter extends RecyclerView.Adapter<RegistrationAdapter.ViewHolder> {
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_registration_approval, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AuctionRegistrations registration = registrations.get(position);
            holder.bind(registration);
        }
        
        @Override
        public int getItemCount() {
            return registrations.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView tvUserId;
            private TextView tvAuctionId;
            private TextView tvPaymentAmount;
            private TextView tvPaymentDate;
            private TextView tvStatus;
            private Button btnApprove;
            private Button btnReject;
            
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvUserId = itemView.findViewById(R.id.tv_user_id);
                tvAuctionId = itemView.findViewById(R.id.tv_auction_id);
                tvPaymentAmount = itemView.findViewById(R.id.tv_payment_amount);
                tvPaymentDate = itemView.findViewById(R.id.tv_payment_date);
                tvStatus = itemView.findViewById(R.id.tv_status);
                btnApprove = itemView.findViewById(R.id.btn_approve);
                btnReject = itemView.findViewById(R.id.btn_reject);
            }
            
            public void bind(AuctionRegistrations registration) {
                tvUserId.setText("User ID: " + registration.getUserId());
                tvAuctionId.setText("Auction ID: " + registration.getAuctionId());
                tvPaymentAmount.setText(String.format("%,.0f VNĐ", 
                    registration.getPaymentAmount() != null ? registration.getPaymentAmount() : 0));
                
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                if (registration.getPaymentDate() != null) {
                    tvPaymentDate.setText("Ngày: " + sdf.format(registration.getPaymentDate()));
                }
                
                tvStatus.setText("Trạng thái: " + registration.getStatus());
                
                // Show/hide buttons based on status
                if ("pending".equals(registration.getStatus())) {
                    btnApprove.setVisibility(View.VISIBLE);
                    btnReject.setVisibility(View.VISIBLE);
                } else {
                    btnApprove.setVisibility(View.GONE);
                    btnReject.setVisibility(View.GONE);
                }
                
                btnApprove.setOnClickListener(v -> showApprovalDialog(registration, true));
                btnReject.setOnClickListener(v -> showApprovalDialog(registration, false));
            }
            
            private void showApprovalDialog(AuctionRegistrations registration, boolean approved) {
                String action = approved ? "duyệt" : "từ chối";
                String message = approved ? 
                    "Bạn có chắc chắn muốn duyệt đăng ký này?" :
                    "Bạn có chắc chắn muốn từ chối đăng ký này?";
                
                new AlertDialog.Builder(AdminAuctionApprovalActivity.this)
                    .setTitle("Xác nhận " + action)
                    .setMessage(message)
                    .setPositiveButton(action, (dialog, which) -> {
                        approveRegistration(registration, approved);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            }
        }
    }
}
