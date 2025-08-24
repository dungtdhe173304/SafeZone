package com.group5.safezone.view.notification;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.safezone.R;
import com.group5.safezone.model.entity.Notification;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Notification notification);
    }

    private List<Notification> notifications;
    private final OnItemClickListener listener;
    private final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public NotificationsAdapter(List<Notification> notifications, OnItemClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    public void update(List<Notification> list) {
        this.notifications = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification n = notifications.get(position);
        holder.tvTitle.setText(n.getMessage());
        holder.tvTime.setText(n.getCreatedDate() != null ? formatter.format(n.getCreatedDate()) : "");
        holder.icon.setImageResource("DEPOSIT_SUCCESS".equals(n.getType()) ? R.drawable.ic_wallet : R.drawable.ic_info);
        holder.itemView.setAlpha(Boolean.TRUE.equals(n.getIsRead()) ? 0.6f : 1f);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(n));
    }

    @Override
    public int getItemCount() {
        return notifications != null ? notifications.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime;
        ImageView icon;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTime = itemView.findViewById(R.id.tvTime);
            icon = itemView.findViewById(R.id.icon);
        }
    }
}


