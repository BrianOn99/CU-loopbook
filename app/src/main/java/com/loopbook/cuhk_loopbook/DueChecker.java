package com.loopbook.cuhk_loopbook;

import android.app.PendingIntent;
import android.support.v4.app.NotificationCompat;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.*;
import java.text.SimpleDateFormat;

import org.jsoup.nodes.Element;

public class DueChecker extends BroadcastReceiver {
    public static int notifyId = 1;

    public void onReceive(Context context, Intent intent) {
        boolean connectable = LibConn.isConnectable();
        Element elm = connectable ?
            DataIO.refreshStoredData(context) :
            DataIO.getStoredData(context);

        SimpleDateFormat dateparser = new SimpleDateFormat("dd-MM-yy");
        Calendar DaysLater = Calendar.getInstance();
        DaysLater.add(Calendar.DATE, 20);

        for (Map<String, String> book: LibConn.getBooksFromElement(elm)) {
            String title = book.get("title");
            String date = book.get("dueDate");
            Calendar dueDate = Calendar.getInstance();
            try {
                dueDate.setTime(dateparser.parse(date));
            } catch (java.text.ParseException e) {
            }

            if (DaysLater.compareTo(dueDate) > 0) {
                NotificationManager nManager = (NotificationManager)context
                                               .getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationCompat.Builder builder = getNotification(context, title);
                nManager.notify(notifyId++, builder.build());
            }
        }
    }

    private NotificationCompat.Builder getNotification(Context context, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle("Book going to expire");
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

