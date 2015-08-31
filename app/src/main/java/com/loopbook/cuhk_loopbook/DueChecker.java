package com.loopbook.cuhk_loopbook;

import android.app.PendingIntent;
import android.support.v4.app.NotificationCompat;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.graphics.BitmapFactory;
import android.os.Build;

import java.util.*;

import android.util.Log;

public class DueChecker extends BroadcastReceiver {
    public static int notifyId = 1;

    private class AsyncBookLoader extends AsyncTask<Context, Void, ArrayList<LibConn.Book>> {
        private Context context;

        @Override
        protected ArrayList<LibConn.Book> doInBackground(Context... context) {
            Log.i("DueChecker", "doingInbackground");
            this.context = context[0];
            try {
                return DataIO.getBooks(this.context);
            } catch (java.io.IOException | java.text.ParseException e) {
                Log.e("DueChecker", "Cannot connect even there is internet. Use stored data instead");
                return DataIO.getStoredBooks(this.context);
            }
        }

        @Override
        protected void onPostExecute(ArrayList<LibConn.Book> booksGot) {
            Log.i("DueChecker", "postexecute");
            DueChecker.checker(context, booksGot);
        }
    }

    public void onReceive(Context context, Intent intent) {
        Log.i("DueChecker", "onReceive");
        AsyncBookLoader bookLoader = new AsyncBookLoader();
        bookLoader.execute(context);
    }

    public static int getAlertDays(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(prefs.getString("alert_days", "2"));
    }

    private static void checker(Context context, ArrayList<LibConn.Book> booksGot) {
        int mindiff = 999;
        int count = 0;
        String lastBookTitle = "";

        int alert_days = getAlertDays(context);

        for (LibConn.Book book: booksGot) {
            int remain = book.remainDays();
            if (remain < alert_days) {
                count++;
                if (mindiff > remain) {
                    mindiff = remain;
                    lastBookTitle = book.name;
                }
            }
        }

        if (count == 0) return;

        String title = String.format(context.getString(R.string.notification_title), count);
        NotificationManager nManager = (NotificationManager)context
            .getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = getNotification(
            context, title,
            String.format(context.getString(R.string.notification_body), mindiff, lastBookTitle));
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

