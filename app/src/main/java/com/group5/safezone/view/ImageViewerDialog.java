package com.group5.safezone.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.group5.safezone.R;
import com.group5.safezone.model.entity.ProductImages;

import java.util.List;

public class ImageViewerDialog extends DialogFragment {

    private static final String ARG_IMAGES = "images";
    private static final String ARG_START_POSITION = "start_position";

    private List<ProductImages> images;
    private int currentPosition;

    // UI Elements
    private PhotoView photoView;
    private TextView tvImageCounter;
    private ImageButton btnClose, btnPrevious, btnNext;

    public static ImageViewerDialog newInstance(List<ProductImages> images, int startPosition) {
        ImageViewerDialog dialog = new ImageViewerDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_IMAGES, (java.io.Serializable) images);
        args.putInt(ARG_START_POSITION, startPosition);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FullScreenDialog);
        
        if (getArguments() != null) {
            images = (List<ProductImages>) getArguments().getSerializable(ARG_IMAGES);
            currentPosition = getArguments().getInt(ARG_START_POSITION, 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_image_viewer, container, false);
        
        initViews(view);
        setupListeners();
        loadImage(currentPosition);
        updateNavigationButtons();
        
        return view;
    }

    private void initViews(View view) {
        photoView = view.findViewById(R.id.photoView);
        tvImageCounter = view.findViewById(R.id.tvImageCounter);
        btnClose = view.findViewById(R.id.btnClose);
        btnPrevious = view.findViewById(R.id.btnPrevious);
        btnNext = view.findViewById(R.id.btnNext);
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> dismiss());
        
        btnPrevious.setOnClickListener(v -> {
            if (currentPosition > 0) {
                currentPosition--;
                loadImage(currentPosition);
                updateNavigationButtons();
            }
        });
        
        btnNext.setOnClickListener(v -> {
            if (currentPosition < images.size() - 1) {
                currentPosition++;
                loadImage(currentPosition);
                updateNavigationButtons();
            }
        });
    }

    private void loadImage(int position) {
        if (images != null && position >= 0 && position < images.size()) {
            ProductImages image = images.get(position);
            if (image.getPath() != null && !image.getPath().isEmpty()) {
                Glide.with(this)
                    .load(image.getPath())
                    .placeholder(R.drawable.ic_info)
                    .error(R.drawable.ic_info)
                    .into(photoView);
            } else {
                photoView.setImageResource(R.drawable.ic_info);
            }
            
            // Update counter
            tvImageCounter.setText((position + 1) + " / " + images.size());
        }
    }

    private void updateNavigationButtons() {
        if (images == null || images.size() <= 1) {
            btnPrevious.setVisibility(View.GONE);
            btnNext.setVisibility(View.GONE);
            return;
        }

        btnPrevious.setVisibility(currentPosition > 0 ? View.VISIBLE : View.GONE);
        btnNext.setVisibility(currentPosition < images.size() - 1 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(params);
        }
    }
}
