package com.group5.safezone.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.group5.safezone.R;
import com.group5.safezone.model.entity.User;
import java.util.ArrayList;
import java.util.List;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserViewHolder> {
    private List<User> userList = new ArrayList<>();
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserSearchAdapter(OnUserClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_search, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateUsers(List<User> newUsers) {
        this.userList.clear();
        this.userList.addAll(newUsers);
        notifyDataSetChanged();
    }

    public void clearUsers() {
        this.userList.clear();
        notifyDataSetChanged();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUserName;
        private TextView tvUserEmail;
        private TextView tvUserRole;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onUserClick(userList.get(position));
                }
            });
        }

        public void bind(User user) {
            tvUserName.setText(user.getUserName());
            tvUserEmail.setText(user.getEmail());
            tvUserRole.setText(user.getRole());
            
            // Hiển thị role với màu khác nhau
            if ("ADMIN".equals(user.getRole())) {
                tvUserRole.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.error_color));
            } else {
                tvUserRole.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.primary));
            }
        }
    }
}
