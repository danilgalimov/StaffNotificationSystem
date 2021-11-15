package com.staffns.staffnotificationsystem;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;



public class MyFirebaseMessagingService extends FirebaseMessagingService {
    String val1, val2;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            val1 = remoteMessage.getData().get("val1");
            val2 = remoteMessage.getData().get("val2");
            ShowNotification(val1, val2);
        }
    }

    @Override
    public void onDeletedMessages() {}

    void ShowNotification(String title, String text) {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent resultIntent = new Intent(this, AuthActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel("channel-01", "Channel Name", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "channel-01")
                .setSmallIcon(R.mipmap.icon)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(1, mBuilder.build());
    }


}