package com.group5.safezone.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.safezone.R;

import java.util.ArrayList;
import java.util.List;

public class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.Holder> {
    private final List<String> uris = new ArrayList<>();

    public void setItems(List<String> newUris) {
        uris.clear();
        if (newUris != null) uris.addAll(newUris);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_preview, parent, false);
        return new Holder(v);
    }

    @Override public void onBindViewHolder(@NonNull Holder h, int pos) {
        h.iv.setImageURI(Uri.parse(uris.get(pos)));
    }

    @Override public int getItemCount() { return uris.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        ImageView iv;
        Holder(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.ivPreview);
        }
    }
}
