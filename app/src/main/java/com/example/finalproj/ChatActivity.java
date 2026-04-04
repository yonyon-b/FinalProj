package com.example.finalproj;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.ai.client.generativeai.type.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText chatBox;
    private ImageButton sendBtn, backBtn;
    private TextView otherUserName;
    private ImageView otherUserPfp;
    private LinearLayout chatToolBar;

    private String currentUserId, otherUserId, chatId;

    private List<ChatMessage> messageList = new ArrayList<>();
    private MessageAdapter adapter;
    private DatabaseReference chatRef;

    private ChatFutures aiChat;
    private Executor executor = Executors.newSingleThreadExecutor();

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
        chatToolBar = findViewById(R.id.chat_toolbar);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseService.getInstance().setupPresenceSystem(); // set user online when opening the app from chat notification
        otherUserId = getIntent().getStringExtra("otherUserId");

        if (otherUserId != null && !otherUserId.equals("gemini_ai_bot")) {
            // normal chat
            chatId = currentUserId.compareTo(otherUserId) < 0
                    ? currentUserId + "_" + otherUserId
                    : otherUserId + "_" + currentUserId;
        } else {
            // AI chat
            otherUserId = "gemini_ai_bot";
            chatId = currentUserId + "_" + otherUserId;
            chatToolBar.setVisibility(View.GONE);
            setupGeminiChat();
        }

        chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId);

        recyclerView = findViewById(R.id.messagesRecyclerView);
        chatBox = findViewById(R.id.edittext_chatbox);
        sendBtn = findViewById(R.id.button_chatbox_send);
        otherUserName = findViewById(R.id.tvOtherUserName);
        otherUserPfp = findViewById(R.id.imgOtherUserPfp);
        backBtn = findViewById(R.id.ib_back_chat);

        if (!otherUserId.equals("gemini_ai_bot")) {
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
        }

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

                if ("gemini_ai_bot".equals(otherUserId)) {
                    sendMessageToAI(msg);
                }

                chatBox.setText("");
            }
        });
        backBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, ChatList.class);
            startActivity(i);
            finish();
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

    private void setupGeminiChat() {
        Content systemInstruction = new Content.Builder()
                .addText(
                        "You are the official AI support assistant for the app 'LostLink'. " +
                                "Your primary role is to help users understand how to use the app. Be polite, helpful, and concise.\n\n" +
                                "### About LostLink:\n" +
                                "- The app allows users to post and browse lost and found items.\n" +
                                "- 'Found' items: Items someone has found and is searching for the owner.\n" +
                                "- 'Lost' items: Items someone has lost and is actively looking for.\n" +
                                "- Chat: Users can chat directly with each other to coordinate returning items.\n" +
                                "- Profile: Users can edit their personal profiles from the settings menu.\n" +
                                "- Settings: Users can change the app theme (Default / Dark / Light) and manage notifications (Chat alerts and Daily reminders).\n\n" +
                                "### Rules for Answering:\n" +
                                "1. ONLY answer questions related to the LostLink app, its features, and how to use them.\n" +
                                "2. If a user asks a question entirely unrelated to the app, politely decline and remind them that you are the LostLink support assistant.\n" +
                                "3. You CANNOT find lost items for users or access user data. If they ask you to find an item, instruct them to browse the app's feed.\n" +
                                "4. Format your responses clearly using bullet points.\n" +
                                "5. In your responses, DO NOT use bold text, if you want to emphasize something, use capital letters."
                )
                .build();

        GenerativeModel gm = new GenerativeModel(
                "gemini-2.5-flash",
                BuildConfig.GEMINI_API_KEY,
                null, // generationConfig
                null, // safetySettings
                new RequestOptions(), // requestOptions
                null, // tools
                null, // toolConfig
                systemInstruction // Inject the app context here
        );

        GenerativeModelFutures modelFutures = GenerativeModelFutures.from(gm);

        aiChat = modelFutures.startChat();
    }

    private void sendMessageToAI(String userText) {
        Content.Builder userMessageBuilder = new Content.Builder();
        userMessageBuilder.setRole("user");
        userMessageBuilder.addText(userText);
        Content userMessage = userMessageBuilder.build();

        ListenableFuture<GenerateContentResponse> response = aiChat.sendMessage(userMessage);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String aiResponse = result.getText();

                // Always update UI and Firebase on the main thread
                runOnUiThread(() -> {
                    Log.d("AiMessage", "Message sent?");
                    long timestamp = System.currentTimeMillis();
                    ChatMessage aiMessage = new ChatMessage(otherUserId, aiResponse, timestamp);

                    chatRef.child("messages").push().setValue(aiMessage);

                    Map<String, Object> chatUpdates = new HashMap<>();
                    chatUpdates.put("lastMessage", aiResponse);
                    chatUpdates.put("timestamp", timestamp);

                    chatRef.updateChildren(chatUpdates);
                });
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
                runOnUiThread(() -> {
                    // Show an error toast to the user
                });
            }
        }, executor);
    }
    protected int getNavigationMenuItemId() {
        return 0;
    }
}