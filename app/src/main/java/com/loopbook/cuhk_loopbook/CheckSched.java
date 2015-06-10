package com.loopbook.cuhk_loopbook;

import java.util.Calendar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent; 
import android.content.Context;
import android.content.BroadcastReceiver;

import android.util.Log;

public class CheckSched extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        scheduleNotification(context);
    }

    public static void scheduleNotification(Context context) {
        Intent notificationIntent = new Intent(context, DueChecker.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmMgr = (AlarmManager) (context.getSystemService(Context.ALARM_SERVICE));
        Calendar calendar = Calendar.getInstance();

        if (BuildInfo.DEBUG) {
            Log.e("CheckSched", "scheduling in debugmode");
            calendar.setTimeInMillis(System.currentTimeMillis() + 5000);
            alarmMgr.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
        } else {
            Log.e("CheckSched", "scheduling in releasemode");
            // Set the alarm to start at approximately ?:??.
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 15);
            calendar.set(Calendar.MINUTE, 36);

            alarmMgr.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_HALF_DAY, pendingIntent);
        }
    }
}
