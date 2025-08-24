package com.group5.safezone.view.livestreaming;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.group5.safezone.R;

import java.util.ArrayList;
import java.util.List;

public class DonateDialog extends DialogFragment {

    private static final String TAG = "DonateDialog";
    private static final String ARG_HOST_ID = "host_id";
    private static final String ARG_HOST_NAME = "host_name";
    private static final String ARG_LIVE_ID = "live_id";

    private String hostId;
    private String hostName;
    private String liveId;
    private OnDonateListener donateListener;

    public interface OnDonateListener {
        void onDonate(int amount, String hostId, String liveId);
    }

    public static DonateDialog newInstance(String hostId, String hostName, String liveId) {
        try {
            DonateDialog dialog = new DonateDialog();
            Bundle args = new Bundle();
            args.putString(ARG_HOST_ID, hostId);
            args.putString(ARG_HOST_NAME, hostName);
            args.putString(ARG_LIVE_ID, liveId);
            dialog.setArguments(args);
            return dialog;
        } catch (Exception e) {
            Log.e(TAG, "Error creating DonateDialog: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (getArguments() != null) {
                hostId = getArguments().getString(ARG_HOST_ID);
                hostName = getArguments().getString(ARG_HOST_NAME);
                liveId = getArguments().getString(ARG_LIVE_ID);
                Log.d(TAG, "Dialog created with hostId: " + hostId + ", hostName: " + hostName + ", liveId: " + liveId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            if (context instanceof OnDonateListener) {
                donateListener = (OnDonateListener) context;
                Log.d(TAG, "OnDonateListener attached successfully");
            } else {
                Log.w(TAG, "Context does not implement OnDonateListener");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onAttach: " + e.getMessage());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.dialog_donate_simple, container, false);
            setupUI(view);
            return view;
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView: " + e.getMessage());
            // Return a simple view to prevent crash
            TextView errorView = new TextView(getContext());
            errorView.setText("Error loading donate dialog");
            return errorView;
        }
    }

    private void setupUI(View view) {
        try {
            // Host info
            TextView tvHostName = view.findViewById(R.id.tv_host_name);
            if (tvHostName != null) {
                tvHostName.setText("Donate cho " + (hostName != null ? hostName : "Host"));
            }

            // Setup donate buttons
            setupDonateButton(view, R.id.btn_5k, 5000, "5K 💖");
            setupDonateButton(view, R.id.btn_10k, 10000, "10K 🎉");
            setupDonateButton(view, R.id.btn_20k, 20000, "20K 🌟");
            setupDonateButton(view, R.id.btn_50k, 50000, "50K 💎");
            setupDonateButton(view, R.id.btn_100k, 100000, "100K 👑");

            // Close button
            Button btnClose = view.findViewById(R.id.btn_close);
            if (btnClose != null) {
                btnClose.setOnClickListener(v -> {
                    try {
                        dismiss();
                    } catch (Exception e) {
                        Log.e(TAG, "Error dismissing dialog: " + e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in setupUI: " + e.getMessage());
        }
    }

    private void setupDonateButton(View view, int buttonId, int amount, String text) {
        try {
            Button button = view.findViewById(buttonId);
            if (button != null) {
                button.setText(text);
                button.setOnClickListener(v -> {
                    try {
                        if (donateListener != null) {
                            donateListener.onDonate(amount, hostId, liveId);
                            dismiss();
                        } else {
                            Log.w(TAG, "DonateListener is null");
                            Toast.makeText(getContext(), "Lỗi: Không thể xử lý donate", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in donate callback: " + e.getMessage());
                        Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up button " + buttonId + ": " + e.getMessage());
        }
    }

    public void setOnDonateListener(OnDonateListener listener) {
        try {
            this.donateListener = listener;
            Log.d(TAG, "DonateListener set successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting donate listener: " + e.getMessage());
        }
    }
}
