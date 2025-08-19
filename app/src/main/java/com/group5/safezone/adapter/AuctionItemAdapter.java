package com.group5.safezone.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.safezone.R;
import com.group5.safezone.model.entity.Auctions;
import com.group5.safezone.model.entity.Product;
import com.group5.safezone.model.ui.AuctionItemUiModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AuctionItemAdapter extends RecyclerView.Adapter<AuctionItemAdapter.AuctionViewHolder> {
    public interface OnAuctionActionListener {
        void onRegisterClick(AuctionItemUiModel item);
        void onEnterRoomClick(AuctionItemUiModel item);
    }

    private final List<AuctionItemUiModel> items = new ArrayList<>();
    private final LayoutInflater inflater;
    private OnAuctionActionListener actionListener;

    public AuctionItemAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    public void setActionListener(OnAuctionActionListener listener) {
        this.actionListener = listener;
    }

    public void submitList(List<AuctionItemUiModel> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AuctionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_auction, parent, false);
        return new AuctionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AuctionViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class AuctionViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivThumbnail;
        private final TextView tvTitle;
        private final TextView tvPrice;
        private final TextView tvStartPrice;
        private final TextView tvStartTime;
        private final TextView tvEndTime;
        private final TextView tvStatus;
        private final TextView tvParticipants;
        private final Button btnAction;

        AuctionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStartPrice = itemView.findViewById(R.id.tvStartPrice);
            tvStartTime = itemView.findViewById(R.id.tvStartTime);
            tvEndTime = itemView.findViewById(R.id.tvEndTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvParticipants = itemView.findViewById(R.id.tvParticipants);
            btnAction = itemView.findViewById(R.id.btnAction);
        }

        void bind(AuctionItemUiModel item) {
            Product product = item.getProduct();
            Auctions auction = item.getAuction();

            tvTitle.setText(product != null && product.getProductName() != null ? product.getProductName() : "Vật phẩm");

            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

            // Logic: nếu chưa đến thời gian bắt đầu → giá khởi điểm = buyNow
            Double startPrice = auction != null ? auction.getStartPrice() : 0;
            if (auction != null && auction.getStartTime() != null && auction.getBuyNowPrice() != null) {
                java.util.Date now = new java.util.Date();
                if (now.before(auction.getStartTime())) {
                    startPrice = auction.getBuyNowPrice();
                }
            }

            Double current = auction != null && auction.getCurrentHighestBid() != null && auction.getCurrentHighestBid() > 0
                    ? auction.getCurrentHighestBid()
                    : startPrice;

            tvPrice.setText("Giá hiện tại: " + formatter.format(current != null ? current : 0));
            tvStartPrice.setText("Giá khởi điểm: " + formatter.format(startPrice != null ? startPrice : 0));

            // Thời gian bắt đầu/kết thúc
            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("HH:mm dd/MM/yyyy", new java.util.Locale("vi","VN"));
            String startStr = auction != null && auction.getStartTime() != null ? df.format(auction.getStartTime()) : "--";
            String endStr = auction != null && auction.getEndTime() != null ? df.format(auction.getEndTime()) : "--";
            tvStartTime.setText("Bắt đầu: " + startStr);
            tvEndTime.setText("Kết thúc: " + endStr);

            // Thumbnail: we keep a placeholder from resources since paths could be remote
            ivThumbnail.setImageResource(R.drawable.ic_inventory);

            tvStatus.setText(item.isRegistered() ? "Đã đăng ký" : "Chưa đăng ký");
            tvParticipants.setText("Người tham gia: " + item.getParticipantCount());

            if (item.isRegistered()) {
                btnAction.setText("Vào phòng đấu giá");
                btnAction.setOnClickListener(v -> {
                    if (actionListener != null) actionListener.onEnterRoomClick(item);
                });
            } else {
                btnAction.setText("Đăng ký tham gia");
                btnAction.setOnClickListener(v -> {
                    if (actionListener != null) actionListener.onRegisterClick(item);
                });
            }
        }
    }
}

