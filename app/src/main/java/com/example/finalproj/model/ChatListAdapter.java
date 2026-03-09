package com.example.finalproj.model;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.finalproj.ChatActivity;
import com.example.finalproj.R;
import com.example.finalproj.services.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {
    private List<Chat> chatList;
    private Context context;
    private String currentUserId,otherUserId, otherUserName = "";

    public ChatListAdapter(Context context, List<Chat> chatList) {
        this.context = context;
        this.chatList = chatList;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        holder.lastMessage.setText(chat.getLastMessage());

        // Find the OTHER user's ID
        for (String id : chat.getMembers().keySet()) {
            if (!id.equals(currentUserId)) {
                otherUserId = id;
                break;
            }
        }

        DatabaseService.getInstance().getUser(otherUserId, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User otherUser) {
                otherUserName = otherUser.getfName() + " " + otherUser.getlName();
                holder.otherUserEmail.setText("Chat with " + otherUserName);
            }

            @Override
            public void onFailed(Exception e) {
                otherUserName = "Failed to find user";
            }
        });

        String finalOtherUserId = otherUserId;
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("otherUserId", finalOtherUserId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return chatList.size(); }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView otherUserEmail, lastMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            otherUserEmail = itemView.findViewById(R.id.chatOtherUserEmail);
            lastMessage = itemView.findViewById(R.id.chatLastMessage);
        }
    }
}