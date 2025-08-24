package com.group5.safezone.view.notification;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.safezone.R;
import com.group5.safezone.config.SessionManager;
import com.group5.safezone.model.entity.Notification;
import com.group5.safezone.repository.NotificationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class NotificationsActivity extends AppCompatActivity implements NotificationsAdapter.OnItemClickListener {

	private Toolbar toolbar;
	private RecyclerView recyclerView;
	private ProgressBar progressBar;
	private TextView emptyState;

	private SessionManager sessionManager;
	private NotificationRepository repository;
	private NotificationsAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notifications);

		toolbar = findViewById(R.id.toolbar);
		recyclerView = findViewById(R.id.recyclerView);
		progressBar = findViewById(R.id.progressBar);
		emptyState = findViewById(R.id.emptyState);

		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
			getSupportActionBar().setTitle(R.string.notifications);
		}

		sessionManager = new SessionManager(this);
		repository = new NotificationRepository(getApplication());

		adapter = new NotificationsAdapter(new ArrayList<>(), this);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(adapter);

		loadData();
	}

	private void loadData() {
		showLoading(true);
		new Thread(() -> {
			try {
				int userId = sessionManager.getUserId();
				List<Notification> list = repository.getNotificationsByUserIdAsync(userId).get();
				runOnUiThread(() -> {
					showLoading(false);
					if (list != null && !list.isEmpty()) {
						adapter.update(list);
						recyclerView.setVisibility(View.VISIBLE);
						emptyState.setVisibility(View.GONE);
						// Đánh dấu đã đọc tất cả khi mở danh sách
						repository.markAllAsReadByUserId(userId);
						// Broadcast để header cập nhật badge về 0
						androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this)
								.sendBroadcast(new android.content.Intent(com.group5.safezone.config.NotificationEvents.ACTION_UPDATED));
					} else {
						recyclerView.setVisibility(View.GONE);
						emptyState.setVisibility(View.VISIBLE);
					}
				});
			} catch (ExecutionException | InterruptedException e) {
				runOnUiThread(() -> showLoading(false));
			}
		}).start();
	}

	private void showLoading(boolean show) {
		progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	@Override
	public boolean onSupportNavigateUp() {
		onBackPressed();
		return true;
	}

	@Override
	public void onItemClick(Notification notification) {
		// Đánh dấu đã đọc khi click riêng một item
		repository.markAsRead(notification.getId());
		androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this)
				.sendBroadcast(new android.content.Intent(com.group5.safezone.config.NotificationEvents.ACTION_UPDATED));
		// Điều hướng theo loại
		if ("DEPOSIT_SUCCESS".equals(notification.getType())) {
			startActivity(new android.content.Intent(this, com.group5.safezone.view.Wallet.WalletActivity.class));
		}
	}
}


