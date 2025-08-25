package com.group5.safezone.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.group5.safezone.R;
import com.group5.safezone.model.entity.Bids;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BidHistoryAdapter extends RecyclerView.Adapter<BidHistoryAdapter.BidViewHolder> {
    
    private final List<Bids> bids = new ArrayList<>();
    private final LayoutInflater inflater;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy • HH:mm:ss.SSS", new Locale("vi", "VN"));
    private final Map<Integer, String> userIdToName = new HashMap<>();

    public BidHistoryAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    public void submitList(List<Bids> newBids) {
        bids.clear();
        if (newBids != null) {
            bids.addAll(newBids);
        }
        notifyDataSetChanged();
    }

    public void setUserIdToNameMap(Map<Integer, String> map) {
        userIdToName.clear();
        if (map != null) {
            userIdToName.putAll(map);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BidViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_bid_history, parent, false);
        return new BidViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BidViewHolder holder, int position) {
        holder.bind(bids.get(position), position == 0); // position == 0 means latest bid
    }

    @Override
    public int getItemCount() {
        return bids.size();
    }

    class BidViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvBidAmount;
        private final TextView tvBidderId;
        private final TextView tvBidTime;
        private final View indicatorView;

        BidViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBidAmount = itemView.findViewById(R.id.tv_bid_amount);
            tvBidderId = itemView.findViewById(R.id.tv_bidder_id);
            tvBidTime = itemView.findViewById(R.id.tv_bid_time);
            indicatorView = itemView.findViewById(R.id.view_indicator);
        }

        void bind(Bids bid, boolean isLatest) {
            // Format bid amount
            String amountText = String.format("%,.0f ₫", bid.getBidAmount());
            tvBidAmount.setText(amountText);
            
            // Set color based on whether it's the latest bid
            if (isLatest) {
                tvBidAmount.setTextColor(itemView.getContext().getResources().getColor(R.color.green));
                indicatorView.setBackgroundResource(R.drawable.bg_role_tag);
            } else {
                tvBidAmount.setTextColor(itemView.getContext().getResources().getColor(R.color.text_primary));
                indicatorView.setBackgroundResource(R.drawable.bg_balance_pill);
            }

            // Show bidder userName if available, else fallback to masked ID
            String name = userIdToName.get(bid.getBidderUserId());
            if (name == null || name.trim().isEmpty()) {
                name = "USER-" + String.format("%06d", bid.getBidderUserId());
            }
            tvBidderId.setText(name);

            // Set bid time
            if (bid.getBidTime() != null) {
                tvBidTime.setText(dateFormat.format(bid.getBidTime()));
            } else {
                tvBidTime.setText("--");
            }
        }
    }
}
