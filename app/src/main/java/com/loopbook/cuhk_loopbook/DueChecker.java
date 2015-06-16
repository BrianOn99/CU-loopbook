package com.loopbook.cuhk_loopbook;

import android.app.PendingIntent;
import android.support.v4.app.NotificationCompat;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.graphics.BitmapFactory;

import java.util.*;
import java.text.SimpleDateFormat;

import org.jsoup.nodes.Element;
import android.util.Log;

public class DueChecker extends BroadcastReceiver {
    public static int notifyId = 1;
    public static int day_threshold = BuildInfo.DEBUG ? 20 : 2;

    private class AsyncBookLoader extends AsyncTask<Context, Void, Element> {
        private Context context;

        @Override
        protected Element doInBackground(Context... context) {
            Log.e("DueChecker", "doingInbackground");
            Element elm;
            this.context = context[0];
            boolean connectable = LibConn.isConnectable(context[0]);
            try {
                return DataIO.getData(this.context);
            } catch (java.io.IOException | java.text.ParseException | LibConn.NoBooksError e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Element elm) {
            if (elm != null) {
                Log.e("DueChecker", "postexecute");
                DueChecker.checker(context, elm);
            }
        }
    }

    public void onReceive(Context context, Intent intent) {
        Log.e("DueChecker", "onReceive");
        AsyncBookLoader bookLoader = new AsyncBookLoader();
        bookLoader.execute(context);
    }

    private static void checker(Context context, Element elm) {
        SimpleDateFormat dateparser = new SimpleDateFormat("dd-MM-yy");
        Calendar DaysLater = Calendar.getInstance();
        DaysLater.add(Calendar.DATE, day_threshold);

        int mindiff = 999;
        int count = 0;
        String lastBookTitle = "";

        for (Map<String, String> book: LibConn.getBooksFromElement(elm)) {
            String bookTitle = book.get("title");
            String date = book.get("dueDate");
            Calendar dueDate = Calendar.getInstance();
            try {
                dueDate.setTime(dateparser.parse(date));
            } catch (java.text.ParseException e) {
            }

            if (DaysLater.compareTo(dueDate) > 0) {
                int diff = dueDate.get(Calendar.DAY_OF_YEAR) -
                           Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
                count++;
                if (mindiff > diff) {
                    mindiff = diff;
                    lastBookTitle = bookTitle;
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
        builder.setSmallIcon(R.drawable.ic_launcher);
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

