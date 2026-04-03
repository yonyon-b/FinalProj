package com.example.finalproj;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproj.model.Chat;
import com.example.finalproj.model.ChatListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatList extends BaseActivity {
    private RecyclerView recyclerView;
    private ChatListAdapter adapter;
    private List<Chat> chatList = new ArrayList<>();
    private DatabaseReference chatsRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatsRef = FirebaseDatabase.getInstance().getReference("chats");

        recyclerView = findViewById(R.id.chatListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatListAdapter(this, chatList);
        recyclerView.setAdapter(adapter);

        loadChats();
    }

    private void loadChats() {
        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                Chat aiChat = new Chat();
                aiChat.setChatId(currentUserId + "_gemini_ai_bot");
                java.util.HashMap<String, Boolean> aiMembers = new java.util.HashMap<>();
                aiMembers.put(currentUserId, true);
                aiMembers.put("gemini_ai_bot", true);
                aiChat.setMembers(aiMembers);
                aiChat.setLastMessage("Tap to chat with Gemini AI");

                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    if (chatSnapshot.getKey() != null && !chatSnapshot.getKey().contains("gemini_ai_bot")) {

                        if (chatSnapshot.child("members").hasChild(currentUserId)) {
                            Chat chat = chatSnapshot.getValue(Chat.class);

                            if (chat != null) {
                                chat.setChatId(chatSnapshot.getKey());
                                chatList.add(chat);
                            }
                        }
                    }
                }
                Collections.reverse(chatList);
                chatList.add(0, aiChat);

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    protected int getNavigationMenuItemId() {
        return R.id.nav_chat;
    }
}