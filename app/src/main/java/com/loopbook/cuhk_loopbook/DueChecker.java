package com.loopbook.cuhk_loopbook;

import android.app.PendingIntent;
import android.support.v4.app.NotificationCompat;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DueChecker extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        NotificationManager nManager = (NotificationManager)context
                                       .getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = getNotification(context, "hello");
        nManager.notify(1, builder.build());
    }

    private NotificationCompat.Builder getNotification(Context context, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle("Scheduled Notification");
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.ic_launcher);

        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                0,
                new Intent(),
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        return builder;
    } 
} 

