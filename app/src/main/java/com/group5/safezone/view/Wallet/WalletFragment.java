//package com.group5.safezone.view.Wallet;
//
//import android.app.AlertDialog;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.material.chip.Chip;
//import com.group5.safezone.R;
//import com.group5.safezone.adapter.TransactionAdapter;
//import com.group5.safezone.config.SessionManager;
//import com.group5.safezone.model.entity.Transactions;
//import com.group5.safezone.repository.TransactionRepository;
//import com.group5.safezone.repository.UserRepository;
//
//import java.text.NumberFormat;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;
//
//public class WalletFragment extends Fragment {
//
//    private TextView tvBalance, tvUserName, tvViewAll;
//    private RecyclerView rvTransactions;
//    private View emptyState;
//    private ProgressBar progressBar;
//    private Button btnDeposit, btnWithdraw;
//
//    private SessionManager sessionManager;
//    private UserRepository userRepository;
//    private TransactionRepository transactionRepository;
//    private TransactionAdapter transactionAdapter;
//    private List<Transactions> transactionList = new ArrayList<>();
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View root = inflater.inflate(R.layout.content_wallet, container, false);
//        tvBalance = root.findViewById(R.id.tvBalance);
//        tvUserName = root.findViewById(R.id.tvUserName);
//        tvViewAll = root.findViewById(R.id.tvViewAll);
//        rvTransactions = root.findViewById(R.id.rvTransactions);
//        emptyState = root.findViewById(R.id.emptyState);
//        progressBar = requireActivity().findViewById(R.id.progressBar); // from container activity
//        btnDeposit = root.findViewById(R.id.btnDeposit);
//        btnWithdraw = root.findViewById(R.id.btnWithdraw);
//
//        sessionManager = new SessionManager(requireContext());
//        userRepository = new UserRepository(requireActivity().getApplication());
//        transactionRepository = new TransactionRepository(requireActivity().getApplication());
//
//        setupRecyclerView();
//        loadUserData();
//        loadTransactions();
//        setupClickListeners();
//        return root;
//    }
//
//    private void setupRecyclerView() {
//        transactionAdapter = new TransactionAdapter(requireContext(), transactionList);
//        rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
//        rvTransactions.setAdapter(transactionAdapter);
//    }
//
//    private void loadUserData() {
//        String userName = sessionManager.getUserName();
//        double balance = sessionManager.getBalance();
//        tvUserName.setText(userName != null ? userName : "Người dùng");
//        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
//        tvBalance.setText(formatter.format(balance));
//    }
//
//    private void loadTransactions() {
//        showLoading(true);
//        int userId = sessionManager.getUserId();
//        transactionRepository.getRecentTransactions(userId, 10, transactions -> {
//            if (!isAdded()) return;
//            requireActivity().runOnUiThread(() -> {
//                showLoading(false);
//                if (transactions != null && !transactions.isEmpty()) {
//                    transactionList.clear();
//                    transactionList.addAll(transactions);
//                    transactionAdapter.notifyDataSetChanged();
//                    rvTransactions.setVisibility(View.VISIBLE);
//                    emptyState.setVisibility(View.GONE);
//                } else {
//                    rvTransactions.setVisibility(View.GONE);
//                    emptyState.setVisibility(View.VISIBLE);
//                }
//            });
//        });
//    }
//
//    private void setupClickListeners() {
//        btnDeposit.setOnClickListener(v -> showDepositDialog());
//        btnWithdraw.setOnClickListener(v -> Toast.makeText(requireContext(), getString(R.string.withdraw_development), Toast.LENGTH_SHORT).show());
//        tvViewAll.setOnClickListener(v -> Toast.makeText(requireContext(), getString(R.string.view_all), Toast.LENGTH_SHORT).show());
//    }
//
//    private void showDepositDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
//        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_deposit, null);
//        builder.setView(dialogView);
//        AlertDialog dialog = builder.create();
//        dialog.setCancelable(false);
//
//        EditText etAmount = dialogView.findViewById(R.id.etAmount);
//        Chip chip50k = dialogView.findViewById(R.id.chip50k);
//        Chip chip100k = dialogView.findViewById(R.id.chip100k);
//        Chip chip200k = dialogView.findViewById(R.id.chip200k);
//        Chip chip500k = dialogView.findViewById(R.id.chip500k);
//        Chip chip1M = dialogView.findViewById(R.id.chip1M);
//        Chip chip5M = dialogView.findViewById(R.id.chip5M);
//        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
//        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);
//
//        chip50k.setOnCheckedChangeListener((b, c) -> { if (c) { etAmount.setText("50000"); uncheck(chip100k, chip200k, chip500k, chip1M, chip5M);} });
//        chip100k.setOnCheckedChangeListener((b, c) -> { if (c) { etAmount.setText("100000"); uncheck(chip50k, chip200k, chip500k, chip1M, chip5M);} });
//        chip200k.setOnCheckedChangeListener((b, c) -> { if (c) { etAmount.setText("200000"); uncheck(chip50k, chip100k, chip500k, chip1M, chip5M);} });
//        chip500k.setOnCheckedChangeListener((b, c) -> { if (c) { etAmount.setText("500000"); uncheck(chip50k, chip100k, chip200k, chip1M, chip5M);} });
//        chip1M.setOnCheckedChangeListener((b, c) -> { if (c) { etAmount.setText("1000000"); uncheck(chip50k, chip100k, chip200k, chip500k, chip5M);} });
//        chip5M.setOnCheckedChangeListener((b, c) -> { if (c) { etAmount.setText("5000000"); uncheck(chip50k, chip100k, chip200k, chip500k, chip1M);} });
//
//        btnCancel.setOnClickListener(v -> dialog.dismiss());
//        btnConfirm.setOnClickListener(v -> {
//            String amountText = etAmount.getText().toString().trim();
//            if (amountText.isEmpty()) { Toast.makeText(requireContext(), getString(R.string.please_enter_amount), Toast.LENGTH_SHORT).show(); return; }
//            try {
//                double amount = Double.parseDouble(amountText);
//                if (amount < 10000) { Toast.makeText(requireContext(), "Số tiền nạp tối thiểu là 10.000 VNĐ", Toast.LENGTH_SHORT).show(); return; }
//                dialog.dismiss();
//                // Gọi activity ví xử lý nạp như cũ, hoặc refactor tiếp nếu cần
//                // Tạm thời hiển thị thông báo dev-only
//                Toast.makeText(requireContext(), "Nạp tiền demo: " + amountText, Toast.LENGTH_SHORT).show();
//            } catch (NumberFormatException ignored) {
//                Toast.makeText(requireContext(), getString(R.string.invalid_amount), Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        dialog.show();
//    }
//
//    private void uncheck(Chip... chips) { for (Chip c : chips) c.setChecked(false); }
//
//    private void showLoading(boolean show) {
//        if (progressBar != null) progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
//    }
//}
