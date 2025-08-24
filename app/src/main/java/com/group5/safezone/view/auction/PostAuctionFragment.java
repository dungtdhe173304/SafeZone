package com.group5.safezone.view.auction;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
	private TextView tvImagesInfo;
	private LinearLayout llSelectedImages;
	private TextView tvNoImages;

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
		tvImagesInfo = v.findViewById(R.id.tvImagesInfo);
		llSelectedImages = v.findViewById(R.id.llSelectedImages);
		tvNoImages = v.findViewById(R.id.tvNoImages);
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
		
		if (pickedUris.isEmpty()) {
			Toast.makeText(requireContext(), "Vui lòng chọn ít nhất 1 ảnh sản phẩm", Toast.LENGTH_SHORT).show();
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
		
		// Show loading
		btnSubmit.setEnabled(false);
		btnSubmit.setText("Đang xử lý...");
		
		new Thread(() -> {
			try {
				AppDatabase db = AppDatabase.getDatabase(requireContext().getApplicationContext());
				
				// Insert product via DAO
				db.productDao().insert(p);
				
				// Process and save images
				if (pickedUris != null && !pickedUris.isEmpty()) {
					for (int i = 0; i < pickedUris.size(); i++) {
						Uri imageUri = pickedUris.get(i);
						String imagePath = saveImageToInternalStorage(imageUri, "IMG_" + i + "_" + productId);
						
						if (imagePath != null) {
							ProductImages img = new ProductImages();
							img.setProductId(productId);
							img.setName("IMG_" + i);
							img.setPath(imagePath);
							db.productImagesDao().insert(img);
						}
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
				
			} catch (Exception e) {
				Log.e("PostAuction", "Error submitting auction: " + e.getMessage(), e);
				requireActivity().runOnUiThread(() -> {
					Toast.makeText(requireContext(), "Lỗi khi gửi yêu cầu: " + e.getMessage(), Toast.LENGTH_LONG).show();
					btnSubmit.setEnabled(true);
					btnSubmit.setText("Gửi duyệt");
				});
			}
		}).start();
	}

	private String saveImageToInternalStorage(Uri imageUri, String fileName) {
		try {
			// Get input stream from URI
			InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
			if (inputStream == null) return null;
			
			// Decode bitmap
			Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
			if (bitmap == null) return null;
			
			// Compress bitmap
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
			
			// Create file in internal storage
			File imagesDir = new File(requireContext().getFilesDir(), "product_images");
			if (!imagesDir.exists()) {
				imagesDir.mkdirs();
			}
			
			File imageFile = new File(imagesDir, fileName + ".jpg");
			FileOutputStream fos = new FileOutputStream(imageFile);
			fos.write(baos.toByteArray());
			fos.close();
			inputStream.close();
			
			return imageFile.getAbsolutePath();
			
		} catch (IOException e) {
			Log.e("PostAuction", "Error saving image: " + e.getMessage(), e);
			return null;
		}
	}

	private void openImagePicker() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
		startActivityForResult(Intent.createChooser(intent, "Chọn ảnh sản phẩm"), 9099);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 9099 && resultCode == requireActivity().RESULT_OK && data != null) {
			pickedUris.clear();
			
			if (data.getData() != null) {
				// Single image selected
				pickedUris.add(data.getData());
			} else if (data.getClipData() != null) {
				// Multiple images selected
				for (int i = 0; i < data.getClipData().getItemCount(); i++) {
					pickedUris.add(data.getClipData().getItemAt(i).getUri());
				}
			}
			
			updateImagesDisplay();
			Toast.makeText(requireContext(), "Đã chọn " + pickedUris.size() + " ảnh", Toast.LENGTH_SHORT).show();
		}
	}

	private void updateImagesDisplay() {
		if (pickedUris.isEmpty()) {
			tvImagesInfo.setText("Chưa chọn ảnh");
			tvNoImages.setVisibility(View.VISIBLE);
			llSelectedImages.setVisibility(View.GONE);
		} else {
			tvImagesInfo.setText("Đã chọn " + pickedUris.size() + " ảnh");
			tvNoImages.setVisibility(View.GONE);
			llSelectedImages.setVisibility(View.VISIBLE);
			
			// Clear existing image views
			llSelectedImages.removeAllViews();
			
			// Add image views for selected images
			for (int i = 0; i < Math.min(pickedUris.size(), 5); i++) { // Show max 5 images
				ImageView imageView = new ImageView(requireContext());
				imageView.setLayoutParams(new LinearLayout.LayoutParams(120, 120));
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setPadding(4, 4, 4, 4);
				
				// Load image into ImageView
				try {
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), pickedUris.get(i));
					imageView.setImageBitmap(bitmap);
				} catch (IOException e) {
					Log.e("PostAuction", "Error loading image: " + e.getMessage(), e);
				}
				
				llSelectedImages.addView(imageView);
			}
		}
	}
}
