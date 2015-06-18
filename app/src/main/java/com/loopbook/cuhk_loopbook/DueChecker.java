package com.loopbook.cuhk_loopbook;

import android.app.PendingIntent;
import android.support.v4.app.NotificationCompat;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.graphics.BitmapFactory;
import android.os.Build;

import java.util.*;

import org.jsoup.nodes.Element;
import android.util.Log;

public class DueChecker extends BroadcastReceiver {
    public static int notifyId = 1;
    public static int day_threshold = BuildInfo.DEBUG ? 20 : 2;

    private class AsyncBookLoader extends AsyncTask<Context, Void, Element> {
        private Context context;

        @Override
        protected Element doInBackground(Context... context) {
            Log.i("DueChecker", "doingInbackground");
            Element elm;
            this.context = context[0];
            try {
                return DataIO.getData(this.context);
            } catch (java.io.IOException | java.text.ParseException | LibConn.NoBooksError e) {
                Log.e("DueChecker", "Cannot connect even there is internet. Use stored data instead");
                return DataIO.getStoredData(this.context);
            }
        }

        @Override
        protected void onPostExecute(Element elm) {
            if (elm != null) {
                Log.i("DueChecker", "postexecute");
                DueChecker.checker(context, elm);
            }
        }
    }

    public void onReceive(Context context, Intent intent) {
        Log.i("DueChecker", "onReceive");
        AsyncBookLoader bookLoader = new AsyncBookLoader();
        bookLoader.execute(context);
    }

    private static void checker(Context context, Element elm) {
        int mindiff = 999;
        int count = 0;
        String lastBookTitle = "";

        for (LibConn.Book book: LibConn.getBooksFromElement(elm)) {
            int remain = book.remainDays();
            if (remain <= day_threshold) {
                count++;
                if (mindiff > remain) {
                    mindiff = remain;
                    lastBookTitle = book.name;
                }
            }
        }

        if (count == 0) return;

        String title = String.format("%s Book coming due (>=%d days)", count, mindiff);
        NotificationManager nManager = (NotificationManager)context
            .getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = getNotification(context, title, lastBookTitle);
        nManager.notify(notifyId++, builder.build());
    }

    public static NotificationCompat.Builder getNotification(
            Context context, String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setSmallIcon(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                             R.drawable.ic_dialog_alert_material:
                             R.drawable.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(
                    context.getResources(), R.drawable.ic_stat_action_schedule));

        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                0,
                new Intent(context, MainActivity.class).putExtra("renew", content),
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        return builder;
    } 
} 

