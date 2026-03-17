package com.example.finalproj.model;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.finalproj.ChatActivity;
import com.example.finalproj.R;
import com.example.finalproj.services.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {
    private List<Chat> chatList;
    private Context context;
    private String currentUserId;

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

        String otherUserId = "";
        for (String id : chat.getMembers().keySet()) {
            if (!id.equals(currentUserId)) {
                otherUserId = id;
                break;
            }
        }
        final String finalOtherUserId = otherUserId;

        DatabaseService.getInstance().getUser(finalOtherUserId, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User otherUser) {
                String otherUserFullName = otherUser.getfName() + " " + otherUser.getlName();
                holder.otherUserName.setText("Chat with " + otherUserFullName);

                String base64String = otherUser.getProfilePicture();
                if (base64String != null && !base64String.isEmpty()) {
                    byte[] imageByteArray = Base64.decode(base64String, Base64.DEFAULT);
                    // Use Glide to load the byte array into the single ImageView
                    Glide.with(context)
                            .asBitmap()
                            .load(imageByteArray)
                            .placeholder(R.drawable.user_pfp_for_item) // placeholder while loading
                            .error(R.drawable.user_pfp_for_item)       // fallback on error
                            .diskCacheStrategy(DiskCacheStrategy.NONE)      // don't cache raw byte arrays to disk
                            .into(holder.otherUserPfp);
                } else {
                    holder.otherUserPfp.setImageResource(R.drawable.user_pfp_for_item);
                }
            }

            @Override
            public void onFailed(Exception e) {
                holder.otherUserName.setText("Unknown User");
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            Log.d("otherUserId: ", finalOtherUserId.toString());
            intent.putExtra("otherUserId", finalOtherUserId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return chatList.size(); }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView otherUserName, lastMessage;
        ImageView otherUserPfp;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            otherUserName = itemView.findViewById(R.id.chatOtherUserName);
            lastMessage = itemView.findViewById(R.id.chatLastMessage);
            otherUserPfp = itemView.findViewById(R.id.chatProfilePic);
        }
    }
}