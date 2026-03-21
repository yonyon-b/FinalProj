package com.example.finalproj;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "daily_reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            return;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        SharedPreferences sharedPreferences = context.getSharedPreferences("preferences_" + userId, context.MODE_PRIVATE);
        boolean isReminderEnabled = sharedPreferences.getBoolean("daily_reminder", true);

        if (!isReminderEnabled)
            return;

        // Create the NotificationChannel (required for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Daily Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Reminds you to check for new lost or found items");
            notificationManager.createNotificationChannel(channel);
        }

        // Intent to open the app when the notification is tapped
        Intent mainIntent = new Intent(context, Splash.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.lostnfound)
                .setContentTitle("New Items?")
                .setContentText("Check out the app to see if there are any new lost or found items today!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Show the notification
        notificationManager.notify(100, builder.build());
    }
}