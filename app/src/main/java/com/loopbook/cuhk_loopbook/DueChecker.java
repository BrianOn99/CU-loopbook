package com.loopbook.cuhk_loopbook;

import android.app.PendingIntent;
import android.support.v4.app.NotificationCompat;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.util.*;
import java.text.SimpleDateFormat;

import org.jsoup.nodes.Element;
import android.util.Log;

public class DueChecker extends BroadcastReceiver {
    public static int notifyId = 1;
    public static int day_threshold = BuildInfo.DEBUG ? 20 : 2;

    private class AsyncBookLoader extends AsyncTask<Context, Void, Element> {
        private Exception caughtException = null;
        private Context context;

        @Override
        protected Element doInBackground(Context... context) {
            Log.e("DueChecker", "doingInbackground");
            Element elm;
            this.context = context[0];
            boolean connectable = LibConn.isConnectable();
            try {
                elm = connectable ?
                    DataIO.refreshStoredData(this.context) :
                    DataIO.getStoredData(this.context);
                return elm;
            } catch (RuntimeException e) {
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

    public static NotificationCompat.Builder getNotification(Context context, String content) {
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

