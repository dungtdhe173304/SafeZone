package com.group5.safezone.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.group5.safezone.R;
import com.group5.safezone.model.entity.ChatCommunity;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CommunityChatAdapter extends RecyclerView.Adapter<CommunityChatAdapter.MessageViewHolder> {
    private List<ChatCommunity> messages;
    private SimpleDateFormat dateFormat;
    
    public CommunityChatAdapter(List<ChatCommunity> messages) {
        this.messages = messages;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_community_message, parent, false);
        return new MessageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatCommunity message = messages.get(position);
        holder.bind(message);
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    public void updateMessages(List<ChatCommunity> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }
    
    class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView userNameText;
        private TextView messageText;
        private TextView timeText;
        
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameText = itemView.findViewById(R.id.user_name);
            messageText = itemView.findViewById(R.id.message_text);
            timeText = itemView.findViewById(R.id.time_text);
        }
        
        public void bind(ChatCommunity message) {
            userNameText.setText(message.getUserName());
            messageText.setText(message.getMessage());
            
            if (message.getCreatedAt() != null) {
                timeText.setText(dateFormat.format(message.getCreatedAt()));
            } else {
                timeText.setText("");
            }
        }
    }
}
