package com.example.finalproj.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.example.finalproj.ChatActivity;
import com.example.finalproj.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "chat_notifications";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        SharedPreferences sharedPreferences = getSharedPreferences("preferences_" + userId, MODE_PRIVATE);

        boolean notificationsEnabled = sharedPreferences.getBoolean("chat_notifications", true);

        // check if message contains a data payload & if was sent by AI & if notifs aren't turned off
        if (notificationsEnabled && !remoteMessage.getData().get("senderId").contains("gemini_ai_bot") && remoteMessage.getData().size() > 0) {
            String senderId = remoteMessage.getData().get("senderId");
            String messageText = remoteMessage.getData().get("message");

            showNotification(senderId, messageText);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        // If the token changes, update it in the database
        // DatabaseService.getInstance().updateFcmToken(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    private void showNotification(String senderId, String messageText) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("otherUserId", senderId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Chat Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("New Message")
                .setContentText(messageText)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
