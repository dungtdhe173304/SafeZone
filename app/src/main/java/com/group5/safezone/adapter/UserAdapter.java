package com.group5.safezone.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.safezone.R;
import com.group5.safezone.model.entity.User;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> users = new ArrayList<>();
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<User> users) {
        this.users = users != null ? users : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUserName, tvEmail, tvRole, tvBalance, tvStatus;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvItemUserName);
            tvEmail = itemView.findViewById(R.id.tvItemEmail);
            tvRole = itemView.findViewById(R.id.tvItemRole);
            tvBalance = itemView.findViewById(R.id.tvItemBalance);
            tvStatus = itemView.findViewById(R.id.tvItemStatus);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onUserClick(users.get(getAdapterPosition()));
                }
            });
        }

        public void bind(User user) {
            tvUserName.setText(user.getUserName() != null ? user.getUserName() : "N/A");
            tvEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");
            tvRole.setText(user.getRole() != null ? user.getRole() : "N/A");

            // Format balance
            if (user.getBalance() != null) {
                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                tvBalance.setText(formatter.format(user.getBalance()));
            } else {
                tvBalance.setText("0 VNĐ");
            }

            tvStatus.setText(user.getStatus() != null ? user.getStatus() : "N/A");
        }
    }
}
