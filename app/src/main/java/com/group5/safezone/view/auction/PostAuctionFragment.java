package com.group5.safezone.view.auction;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.group5.safezone.R;
import com.group5.safezone.config.AppDatabase;
import com.group5.safezone.config.SessionManager;
import com.group5.safezone.model.entity.Auctions;
import com.group5.safezone.model.entity.Product;
import com.group5.safezone.model.entity.ProductImages;
import com.group5.safezone.repository.AuctionRepository;
import com.group5.safezone.repository.UserRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class PostAuctionFragment extends Fragment {

	private EditText etProductName, etDescription, etPrice, etStartPrice, etMinIncrement;
	private EditText etStartTime, etEndTime;
	private Button btnPickStart, btnPickEnd, btnSubmit, btnPickImages;

	private Date startDate, endDate;
	private ArrayList<Uri> pickedUris = new ArrayList<>();
	private AuctionRepository auctionRepository;
	private UserRepository userRepository;
	private SessionManager sessionManager;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_post_auction, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		sessionManager = new SessionManager(requireContext());
		auctionRepository = new AuctionRepository(requireActivity().getApplication());
		userRepository = new UserRepository(requireActivity().getApplication());

		initViews(view);
		setupPickers();
		btnSubmit.setOnClickListener(v -> submit());
		btnPickImages.setOnClickListener(v -> openImagePicker());
	}

	private void initViews(View v) {
		etProductName = v.findViewById(R.id.etProductName);
		etDescription = v.findViewById(R.id.etDescription);
		etPrice = v.findViewById(R.id.etPrice);
		etStartPrice = v.findViewById(R.id.etStartPrice);
		etMinIncrement = v.findViewById(R.id.etMinIncrement);
		etStartTime = v.findViewById(R.id.etStartTime);
		etEndTime = v.findViewById(R.id.etEndTime);
		btnPickStart = v.findViewById(R.id.btnPickStart);
		btnPickEnd = v.findViewById(R.id.btnPickEnd);
		btnSubmit = v.findViewById(R.id.btnSubmit);
		btnPickImages = v.findViewById(R.id.btnPickImages);
	}

	private void setupPickers() {
		btnPickStart.setOnClickListener(v -> pickDateTime(true));
		btnPickEnd.setOnClickListener(v -> pickDateTime(false));
	}

	private void pickDateTime(boolean isStart) {
		Calendar cal = Calendar.getInstance();
		DatePickerDialog dp = new DatePickerDialog(requireContext(), (view, y, m, d) -> {
			TimePickerDialog tp = new TimePickerDialog(requireContext(), (timePicker, h, min) -> {
				Calendar c = Calendar.getInstance();
				c.set(y, m, d, h, min, 0);
				Date picked = c.getTime();
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", new Locale("vi","VN"));
				if (isStart) {
					startDate = picked;
					etStartTime.setText(sdf.format(picked));
				} else {
					endDate = picked;
					etEndTime.setText(sdf.format(picked));
				}
			}, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
			tp.show();
		}, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
		dp.show();
	}

	private void submit() {
		int userId = sessionManager.getUserId();
		if (userId <= 0) {
			Toast.makeText(requireContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
			return;
		}
		String name = etProductName.getText().toString().trim();
		String desc = etDescription.getText().toString().trim();
		String priceStr = etPrice.getText().toString().trim();
		String startPriceStr = etStartPrice.getText().toString().trim();
		String minIncStr = etMinIncrement.getText().toString().trim();
		if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(startPriceStr) || startDate == null || endDate == null) {
			Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
			return;
		}
		double price = Double.parseDouble(priceStr);
		double startPrice = Double.parseDouble(startPriceStr);
		double minInc = TextUtils.isEmpty(minIncStr) ? 10000 : Double.parseDouble(minIncStr);
		if (!endDate.after(startDate)) {
			Toast.makeText(requireContext(), "Thời gian kết thúc phải sau thời gian bắt đầu", Toast.LENGTH_SHORT).show();
			return;
		}

		// Create product (auction item)
		String productId = UUID.randomUUID().toString();
		Product p = new Product(productId);
		p.setProductName(name);
		p.setDescribe(desc);
		p.setPrice(price);
		p.setUserId(userId);
		p.setIsAuctionItem(true);
		p.setIsAdminCheck(false);
		p.setStatus("pending");
		p.setMinBidIncrement(minInc);
		new Thread(() -> {
			AppDatabase db = AppDatabase.getDatabase(requireContext().getApplicationContext());
			// Insert product via DAO
			db.productDao().insert(p);
			// Insert selected images
			if (pickedUris != null) {
				for (int i = 0; i < pickedUris.size(); i++) {
					ProductImages img = new ProductImages();
					img.setProductId(productId);
					img.setName("IMG_" + i);
					img.setPath(pickedUris.get(i).toString());
					db.productImagesDao().insert(img);
				}
			}
			// Create auction row with status pending (await admin approve)
			Auctions a = new Auctions();
			a.setProductId(productId);
			a.setSellerUserId(userId);
			a.setStartPrice(startPrice);
			a.setBuyNowPrice(null);
			a.setStartTime(startDate);
			a.setEndTime(endDate);
			a.setStatus("pending");
			db.auctionsDao().insert(a);
			requireActivity().runOnUiThread(() -> {
				Toast.makeText(requireContext(), "Đã gửi yêu cầu đăng bán, chờ admin duyệt", Toast.LENGTH_LONG).show();
				requireActivity().getSupportFragmentManager().popBackStack();
			});
		}).start();
	}

	private void openImagePicker() {
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("image/*");
		intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
		startActivityForResult(intent, 9099);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 9099 && data != null) {
			pickedUris.clear();
			if (data.getData() != null) {
				pickedUris.add(data.getData());
			} else if (data.getClipData() != null) {
				for (int i = 0; i < data.getClipData().getItemCount(); i++) {
					pickedUris.add(data.getClipData().getItemAt(i).getUri());
				}
			}
			Toast.makeText(requireContext(), "Đã chọn " + pickedUris.size() + " ảnh", Toast.LENGTH_SHORT).show();
		}
	}
}
