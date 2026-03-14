package com.example.finalproj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproj.model.ChatMessage;
import com.example.finalproj.model.ImageUtil;
import com.example.finalproj.model.MessageAdapter;
import com.example.finalproj.model.User;
import com.example.finalproj.services.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText chatBox;
    private ImageButton sendBtn;
    private TextView otherUserName;
    private ImageView otherUserPfp;

    private String currentUserId, otherUserId, chatId;

    private List<ChatMessage> messageList = new ArrayList<>();
    private MessageAdapter adapter;
    private DatabaseReference chatRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        otherUserId = getIntent().getStringExtra("otherUserId");

        // Generate consistent Chat ID based on alphabetical order of UIDs
        chatId = currentUserId.compareTo(otherUserId) < 0
                ? currentUserId + "_" + otherUserId
                : otherUserId + "_" + currentUserId;

        chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId);

        recyclerView = findViewById(R.id.messagesRecyclerView);
        chatBox = findViewById(R.id.edittext_chatbox);
        sendBtn = findViewById(R.id.button_chatbox_send);
        otherUserName = findViewById(R.id.tvOtherUserName);
        otherUserPfp = findViewById(R.id.imgOtherUserPfp);

        otherUserName.setOnClickListener(v -> {
            Intent i = new Intent(this, UserProfile.class);
            i.putExtra("USER_UID", otherUserId);
            startActivity(i);
        });

        DatabaseService.getInstance().getUser(otherUserId, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User otherUser) {
                otherUserName.setText(otherUser.getfName() + " " + otherUser.getlName());
                if (otherUser.getProfilePicture() != null)
                    otherUserPfp.setImageBitmap(ImageUtil.convertFrom64base(otherUser.getProfilePicture()));
            }

            @Override
            public void onFailed(Exception e) {
                otherUserName.setText("Unknown User");
            }
        });
        // scroll listener for gradient for sent messages
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    View child = recyclerView.getChildAt(i);
                    View bubble = child.findViewById(R.id.gradient_bubble);
                    if (bubble != null) {
                        bubble.invalidate();
                    }
                }
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Start from bottom
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(adapter);

        loadMessages();

        sendBtn.setOnClickListener(v -> {
            String msg = chatBox.getText().toString().trim();
            if (!msg.isEmpty()) {
                sendMessage(msg);
                chatBox.setText("");
            }
        });
    }

    private void loadMessages() {
        chatRef.child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot msgSnapshot : snapshot.getChildren()) {
                    ChatMessage message = msgSnapshot.getValue(ChatMessage.class);
                    messageList.add(message);
                }
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void sendMessage(String text) {
        long timestamp = System.currentTimeMillis();
        ChatMessage message = new ChatMessage(currentUserId, text, timestamp);

        // Save Message
        chatRef.child("messages").push().setValue(message);

        // Update Chat Metadata
        Map<String, Object> chatUpdates = new HashMap<>();
        chatUpdates.put("lastMessage", text);
        chatUpdates.put("timestamp", timestamp);
        chatUpdates.put("members/" + currentUserId, true);
        chatUpdates.put("members/" + otherUserId, true);

        chatRef.updateChildren(chatUpdates);
    }
    protected int getNavigationMenuItemId() {
        return 0;
    }
}