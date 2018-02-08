package com.example.aliasgarbs.konnect;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by AliAsgar BS on 1/5/2018.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notification_title = remoteMessage.getNotification().getTitle();
        String notification_body = remoteMessage.getNotification().getBody();

        String click_action = remoteMessage.getNotification().getClickAction();

        String from_sender_id = remoteMessage.getData().get("from_sender_id").toString();





        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.mychat)
                        .setContentTitle(notification_title)
                        .setContentText(notification_body);


        Intent resultIntent = new Intent(click_action);
        resultIntent.putExtra("visit_user_id",from_sender_id);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);




        // Sets an ID for the notification
        int mNotificationId = (int) System.currentTimeMillis();
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}
