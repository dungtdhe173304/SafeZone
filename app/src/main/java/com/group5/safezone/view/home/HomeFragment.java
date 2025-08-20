package com.group5.safezone.view.home;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.safezone.R;
import com.group5.safezone.adapter.AuctionItemAdapter;
import com.group5.safezone.config.AuthInterceptor;
import com.group5.safezone.config.SessionManager;
import com.group5.safezone.model.ui.AuctionItemUiModel;
import com.group5.safezone.view.AuctionRegistrationActivity;
import com.group5.safezone.viewmodel.AuctionViewModel;
import com.group5.safezone.viewmodel.UserViewModel;

import java.text.NumberFormat;
import java.util.Locale;

public class HomeFragment extends Fragment {

	private UserViewModel userViewModel;
	private AuctionViewModel auctionViewModel;
	private SessionManager sessionManager;
	private AuctionItemAdapter auctionAdapter;

	private ProgressBar progressBar;
	private TextView tvError, tvHelloUser, tvBalancePill;
	private ImageView ivAvatar;
	private RecyclerView recyclerViewAuctions;
	private TextView etSearch; // reuse TextView id
	private Button btnSearch;
	private Button btnPostAuction;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_home, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		sessionManager = new SessionManager(requireContext());
		initViews(view);
		setupViewModel();
		setupRecyclerView();
		setupClickListeners();
		observeViewModel();
		displayWelcomeMessage();
		refreshData();
	}

	public void refreshData() {
		try {
			if (!AuthInterceptor.checkAuthentication(requireActivity())) return;
			int uid = sessionManager.getUserId();
			if (uid > 0) {
				userViewModel.getUserById(uid);
			}
			auctionViewModel.loadAuctions(uid);
		} catch (Exception e) {
			android.util.Log.e("HomeFragment", "Error in refreshData: " + e.getMessage());
			Toast.makeText(requireContext(), "Lỗi khi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	private void initViews(View root) {
		progressBar = root.findViewById(R.id.progressBar);
		tvError = root.findViewById(R.id.tvError);
		tvHelloUser = root.findViewById(R.id.tvHelloUser);
		tvBalancePill = root.findViewById(R.id.tvBalancePill);
		ivAvatar = root.findViewById(R.id.ivAvatar);
		recyclerViewAuctions = root.findViewById(R.id.recyclerViewAuctions);
		etSearch = root.findViewById(R.id.etSearch);
		btnSearch = root.findViewById(R.id.btnSearch);
		btnPostAuction = root.findViewById(R.id.btnPostAuction);
	}

	private void setupViewModel() {
		userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
		auctionViewModel = new ViewModelProvider(requireActivity()).get(AuctionViewModel.class);
	}

	private void setupRecyclerView() {
		auctionAdapter = new AuctionItemAdapter(requireContext());
		recyclerViewAuctions.setLayoutManager(new LinearLayoutManager(requireContext()));
		recyclerViewAuctions.setAdapter(auctionAdapter);

		auctionAdapter.setActionListener(new AuctionItemAdapter.OnAuctionActionListener() {
			@Override
			public void onRegisterClick(AuctionItemUiModel item) {
				try {
					if (sessionManager.getUserId() <= 0) {
						Toast.makeText(requireContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
						return;
					}
					
					if (item == null || item.getAuction() == null) {
						Toast.makeText(requireContext(), "Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
						return;
					}
					
					android.content.Intent intent = new android.content.Intent(requireContext(), AuctionRegistrationActivity.class);
					intent.putExtra(AuctionRegistrationActivity.EXTRA_AUCTION_ID, item.getAuction().getId());
					startActivity(intent);
				} catch (Exception e) {
					Toast.makeText(requireContext(), "Lỗi khi mở trang đăng ký: " + e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onEnterRoomClick(AuctionItemUiModel item) {
				try {
					if (sessionManager.getUserId() <= 0) {
						Toast.makeText(requireContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
						return;
					}
					
					if (item == null || item.getAuction() == null) {
						Toast.makeText(requireContext(), "Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
						return;
					}
					
					android.content.Intent intent = new android.content.Intent(requireContext(), com.group5.safezone.view.AuctionRoomActivity.class);
					intent.putExtra(com.group5.safezone.view.AuctionRoomActivity.EXTRA_AUCTION_ID, item.getAuction().getId());
					startActivity(intent);
				} catch (Exception e) {
					Toast.makeText(requireContext(), "Lỗi khi mở phòng đấu giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private void setupClickListeners() {
		btnSearch.setOnClickListener(v -> {
			int uid = sessionManager.getUserId();
			String keyword = etSearch.getText() != null ? etSearch.getText().toString() : "";
			auctionViewModel.searchAuctions(uid, keyword);
		});

		btnPostAuction.setOnClickListener(v -> {
			requireActivity().getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.home_container, new com.group5.safezone.view.auction.PostAuctionFragment())
				.addToBackStack("post_auction")
				.commit();
		});
	}

	private void observeViewModel() {
		userViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
			progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
		});

		userViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
			if (errorMessage != null && !errorMessage.isEmpty()) {
				tvError.setText(errorMessage);
				tvError.setVisibility(View.VISIBLE);
				Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
			} else {
				tvError.setVisibility(View.GONE);
			}
		});

		userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
			if (user != null) {
				NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi","VN"));
				String balanceText = formatter.format(user.getBalance() != null ? user.getBalance() : 0);
				tvBalancePill.setText(balanceText);
				tvHelloUser.setText("Chào mừng, " + (user.getUserName() != null ? user.getUserName() : "Khách") + "!");
			}
		});

		auctionViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
			progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
		});

		auctionViewModel.getItems().observe(getViewLifecycleOwner(), items -> {
			try {
				if (items != null) {
					auctionAdapter.submitList(items);
				}
			} catch (Exception e) {
				android.util.Log.e("HomeFragment", "Error in getItems observer: " + e.getMessage());
				Toast.makeText(requireContext(), "Lỗi khi hiển thị danh sách: " + e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});

		auctionViewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
			if (!TextUtils.isEmpty(msg)) {
				tvError.setText(msg);
				tvError.setVisibility(View.VISIBLE);
			} else {
				tvError.setVisibility(View.GONE);
			}
		});
	}

	private void displayWelcomeMessage() {
		String userName = sessionManager.getUserName();
		NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi","VN"));
		String balanceText = formatter.format(sessionManager.getBalance());
		tvHelloUser.setText("Chào mừng, " + (userName != null ? userName : "Khách") + "!");
		tvBalancePill.setText(balanceText);
		ivAvatar.setImageResource(R.drawable.ic_person);
	}
}
