package com.es.fteam;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private final String ADMIN_CHANNEL_ID ="fsp_channel";

    @Override
    public void onNewToken(String token) {
        if(ActivityLogin.currentUserID != null)
            Utils.subscribe();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        String openedMatch = ActivitySelectMatch.matchID;

        //If the current user sent the message or this match is currently browsed by the user do nothing
        if(!ActivityLogin.currentUserID.equals(remoteMessage.getData().get("sender")) &&
                (openedMatch == null || !openedMatch.equals(remoteMessage.getData().get("match")))){
            final Intent intent = new Intent(this, ActivitySelectMatch.class);
            intent.putExtra("type", "notification");
            intent.setAction("onNotificationClicked");
            intent.putExtra("match", remoteMessage.getData().get("match"));
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int notificationID = 1;
            String notificationTAG = remoteMessage.getData().get("match");

      /*
        Apps targeting SDK 26 or above (Android O) must implement notification channels and add its notifications
        to at least one of them. Therefore, confirm if version is Oreo or higher, then setup notification channel
      */
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                setupChannels(notificationManager);
            }

            PendingIntent pendingIntent = PendingIntent.getActivity(this , 0, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_message);

            Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_message)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(remoteMessage.getData().get("title"))
                    .setContentText(remoteMessage.getData().get("body"))
                    .setAutoCancel(true)
                    .setSound(notificationSoundUri)
                    .setContentIntent(pendingIntent);

            //Set notification color to match your app color template
            notificationBuilder.setColor(getResources().getColor(R.color.colorAccent));
            notificationManager.notify(notificationTAG, notificationID, notificationBuilder.build());
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(NotificationManager notificationManager){
        CharSequence adminChannelName = "New notification";
        String adminChannelDescription = "Device to device notification";

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }
}
