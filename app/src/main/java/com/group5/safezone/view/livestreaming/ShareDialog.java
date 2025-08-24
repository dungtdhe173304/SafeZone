package com.group5.safezone.view.livestreaming;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.group5.safezone.R;

/**
 * Dialog for sharing live stream invitations
 * Provides various sharing options and custom message input
 */
public class ShareDialog extends DialogFragment {

    private String liveID;
    private String streamTitle;
    private String hostName;
    private OnShareListener shareListener;

    public interface OnShareListener {
        void onShare(String platform, String customMessage);
    }

    public static ShareDialog newInstance(String liveID, String streamTitle, String hostName) {
        ShareDialog dialog = new ShareDialog();
        Bundle args = new Bundle();
        args.putString("liveID", liveID);
        args.putString("streamTitle", streamTitle);
        args.putString("hostName", hostName);
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnShareListener(OnShareListener listener) {
        this.shareListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            liveID = getArguments().getString("liveID");
            streamTitle = getArguments().getString("streamTitle");
            hostName = getArguments().getString("hostName");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        // Inflate custom layout
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_share_live_stream, null);
        
        // Set dialog title
        TextView tvTitle = view.findViewById(R.id.tv_dialog_title);
        tvTitle.setText("Share Live Stream");
        
        // Set stream info
        TextView tvStreamInfo = view.findViewById(R.id.tv_stream_info);
        tvStreamInfo.setText(String.format("🎥 %s\n👤 Host: %s\n🆔 Stream ID: %s", 
            streamTitle, hostName, liveID));
        
        // Setup sharing platforms list
        ListView lvPlatforms = view.findViewById(R.id.lv_sharing_platforms);
        String[] platforms = ShareManager.getAvailablePlatforms(getActivity());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), 
            android.R.layout.simple_list_item_1, platforms);
        lvPlatforms.setAdapter(adapter);
        
        // Setup custom message input
        EditText etCustomMessage = view.findViewById(R.id.et_custom_message);
        etCustomMessage.setHint("Add a personal message (optional)");
        
        // Setup buttons
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnShare = view.findViewById(R.id.btn_share);
        
        // Cancel button
        btnCancel.setOnClickListener(v -> dismiss());
        
        // Share button
        btnShare.setOnClickListener(v -> {
            String customMessage = etCustomMessage.getText().toString().trim();
            if (shareListener != null) {
                shareListener.onShare("general", customMessage);
            }
            dismiss();
        });
        
        // Platform selection
        lvPlatforms.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedPlatform = platforms[position];
            String customMessage = etCustomMessage.getText().toString().trim();
            
            if (shareListener != null) {
                shareListener.onShare(selectedPlatform, customMessage);
            }
            dismiss();
        });
        
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 
                           ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
