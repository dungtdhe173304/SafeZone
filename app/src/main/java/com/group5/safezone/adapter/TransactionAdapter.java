package com.group5.safezone.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.safezone.R;
import com.group5.safezone.model.entity.Transactions;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private List<Transactions> transactions;
    private Context context;
    private OnTransactionClickListener listener;

    public TransactionAdapter(Context context, List<Transactions> transactions) {
        this.context = context;
        this.transactions = transactions;
    }

    public void setTransactions(List<Transactions> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transactions transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions != null ? transactions.size() : 0;
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivTransactionIcon;
        private TextView tvTransactionType, tvTransactionDescription, tvTransactionDate;
        private TextView tvTransactionAmount, tvTransactionStatus;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTransactionIcon = itemView.findViewById(R.id.ivTransactionIcon);
            tvTransactionType = itemView.findViewById(R.id.tvTransactionType);
            tvTransactionDescription = itemView.findViewById(R.id.tvTransactionDescription);
            tvTransactionDate = itemView.findViewById(R.id.tvTransactionDate);
            tvTransactionAmount = itemView.findViewById(R.id.tvTransactionAmount);
            tvTransactionStatus = itemView.findViewById(R.id.tvTransactionStatus);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onTransactionClick(transactions.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Transactions transaction) {
            // Set transaction type
            String typeText = getTransactionTypeText(transaction.getTransactionType());
            tvTransactionType.setText(typeText);

            // Set description
            tvTransactionDescription.setText(transaction.getDescription() != null ? 
                transaction.getDescription() : "Giao dịch");

            // Set date
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String dateText = transaction.getCreatedAt() != null ? 
                dateFormat.format(transaction.getCreatedAt()) : "N/A";
            tvTransactionDate.setText(dateText);

            // Set amount with color
            if (transaction.getAmount() != null) {
                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                String amountText = formatter.format(Math.abs(transaction.getAmount()));
                
                // Add + or - prefix based on transaction type
                if (Transactions.TYPE_DEPOSIT.equals(transaction.getTransactionType()) ||
                    Transactions.TYPE_REFUND.equals(transaction.getTransactionType()) ||
                    Transactions.TYPE_PAYMENT_COMPLETE.equals(transaction.getTransactionType())) {
                    // Giao dịch nhận tiền (dương)
                    amountText = "+" + amountText;
                    tvTransactionAmount.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                } else if (Transactions.TYPE_WITHDRAW.equals(transaction.getTransactionType()) || 
                           Transactions.TYPE_PAYMENT.equals(transaction.getTransactionType()) ||
                           Transactions.TYPE_POSTING_FEE.equals(transaction.getTransactionType()) ||
                           Transactions.TYPE_PRODUCT_PURCHASE.equals(transaction.getTransactionType())) {
                    // Giao dịch chi tiền (âm)
                    amountText = "-" + amountText;
                    tvTransactionAmount.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                } else {
                    // Giao dịch khác
                    tvTransactionAmount.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
                }
                
                tvTransactionAmount.setText(amountText);
            } else {
                tvTransactionAmount.setText("0 VNĐ");
            }

            // Set status
            String statusText = getStatusText(transaction.getStatus());
            tvTransactionStatus.setText(statusText);
            
            // Set status color
            if (Transactions.STATUS_SUCCESS.equals(transaction.getStatus()) || 
                Transactions.STATUS_COMPLETED.equals(transaction.getStatus()) || 
                Transactions.STATUS_SUCCESSFUL.equals(transaction.getStatus())) {
                tvTransactionStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            } else if (Transactions.STATUS_FAILED.equals(transaction.getStatus()) || 
                       Transactions.STATUS_ERROR.equals(transaction.getStatus())) {
                tvTransactionStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            } else if (Transactions.STATUS_PENDING.equals(transaction.getStatus()) || 
                       Transactions.STATUS_PROCESSING.equals(transaction.getStatus()) || 
                       Transactions.STATUS_WAITING.equals(transaction.getStatus())) {
                tvTransactionStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
            } else {
                tvTransactionStatus.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            }

            // Set icon based on transaction type
            setTransactionIcon(transaction.getTransactionType());
        }

        private String getTransactionTypeText(String type) {
            if (type == null) {
                return "Giao dịch";
            }
            
            if (Transactions.TYPE_DEPOSIT.equals(type)) {
                return "Nạp tiền";
            } else if (Transactions.TYPE_WITHDRAW.equals(type)) {
                return "Rút tiền";
            } else if (Transactions.TYPE_TRANSFER.equals(type)) {
                return "Chuyển tiền";
            } else if (Transactions.TYPE_PAYMENT.equals(type)) {
                return "Thanh toán";
            } else if (Transactions.TYPE_POSTING_FEE.equals(type)) {
                return "Phí đăng bài";
            } else if (Transactions.TYPE_PRODUCT_PURCHASE.equals(type)) {
                return "Mua sản phẩm";
            } else if (Transactions.TYPE_REFUND.equals(type)) {
                return "Hoàn tiền";
            } else if (Transactions.TYPE_PAYMENT_COMPLETE.equals(type)) {
                return "Thanh toán";
            } else if (Transactions.TYPE_DONATE.equals(type)) {
                return "Donate";
            } else if (Transactions.TYPE_DONATE_RECEIVED.equals(type)) {
                return "Nhận donate";
            }
            
            return "Giao dịch";
        }

        private String getStatusText(String status) {
            if (status == null) {
                return "Không xác định";
            }
            
            // Xử lý các trạng thái thành công
            if (Transactions.STATUS_SUCCESS.equals(status) || 
                Transactions.STATUS_COMPLETED.equals(status) || 
                Transactions.STATUS_SUCCESSFUL.equals(status)) {
                return "Thành công";
            } 
            // Xử lý các trạng thái thất bại
            else if (Transactions.STATUS_FAILED.equals(status) || 
                     Transactions.STATUS_ERROR.equals(status)) {
                return "Thất bại";
            } 
            // Xử lý các trạng thái đang xử lý
            else if (Transactions.STATUS_PENDING.equals(status) || 
                     Transactions.STATUS_PROCESSING.equals(status) || 
                     Transactions.STATUS_WAITING.equals(status)) {
                return "Đang xử lý";
            }
            
            return "Không xác định";
        }

        private void setTransactionIcon(String type) {
            if (Transactions.TYPE_DEPOSIT.equals(type)) {
                ivTransactionIcon.setImageResource(R.drawable.ic_wallet);
                ivTransactionIcon.setBackgroundTintList(context.getColorStateList(android.R.color.holo_green_dark));
            } else if (Transactions.TYPE_WITHDRAW.equals(type)) {
                ivTransactionIcon.setImageResource(R.drawable.ic_wallet);
                ivTransactionIcon.setBackgroundTintList(context.getColorStateList(android.R.color.holo_red_dark));
            } else if (Transactions.TYPE_TRANSFER.equals(type)) {
                ivTransactionIcon.setImageResource(R.drawable.ic_wallet);
                ivTransactionIcon.setBackgroundTintList(context.getColorStateList(android.R.color.holo_blue_dark));
            } else if (Transactions.TYPE_PAYMENT.equals(type)) {
                ivTransactionIcon.setImageResource(R.drawable.ic_shopping_bag);
                ivTransactionIcon.setBackgroundTintList(context.getColorStateList(android.R.color.holo_orange_dark));
            } else {
                ivTransactionIcon.setImageResource(R.drawable.ic_wallet);
                ivTransactionIcon.setBackgroundTintList(context.getColorStateList(android.R.color.holo_blue_dark));
            }
        }
    }

    public interface OnTransactionClickListener {
        void onTransactionClick(Transactions transaction);
    }
}
