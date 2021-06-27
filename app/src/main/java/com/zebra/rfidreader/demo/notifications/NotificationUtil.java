package com.zebra.rfidreader.demo.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.zebra.rfidreader.demo.R;
import com.zebra.rfidreader.demo.common.Constants;
import com.zebra.rfidreader.demo.home.MainActivity;

public class NotificationUtil {

    public static final String CHANNEL_ID = "NotificationChannel";
    private static int NOTIFICATION_ID = 1;

    public static void displayNotification(Context context, String action, String data) {

        NotificationManager mgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notification Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );


            mgr.createNotificationChannel(serviceChannel);
        }


        Intent resultIntent;

        if (action != null && (action.equalsIgnoreCase(Constants.ACTION_READER_BATTERY_CRITICAL) || action.equalsIgnoreCase(Constants.ACTION_READER_AVAILABLE) ||
                action.equalsIgnoreCase(Constants.ACTION_READER_CONN_FAILED) || action.equalsIgnoreCase(Constants.ACTION_READER_DISCONNECTED) ||
                action.equalsIgnoreCase(Constants.ACTION_READER_CONNECTED))) {
            resultIntent = new Intent(context, MainActivity.class);
            //resultIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            resultIntent.setAction(Intent.ACTION_MAIN);
            resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        } else {
            resultIntent = new Intent(context, MainActivity.class);
            resultIntent.setAction(Intent.ACTION_MAIN);
            resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        }

        if (action != null && ((action.equalsIgnoreCase(Constants.ACTION_READER_BATTERY_CRITICAL)) || (action.equalsIgnoreCase(Constants.ACTION_READER_BATTERY_LOW))))
            resultIntent.putExtra(Constants.SETTING_ITEM_ID, R.id.regulatory);
        else
            resultIntent.putExtra(Constants.SETTING_ITEM_ID, R.id.readers_list);
        resultIntent.putExtra(Constants.FROM_NOTIFICATION, true);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.custom_notification);
        contentView.setImageViewResource(R.id.image, R.drawable.ic_stat_notify_msg);
        contentView.setTextViewText(R.id.text, data);
        contentView.setTextViewText(R.id.title, context.getString(R.string.app_title));

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContent(contentView)
                .setSmallIcon(R.drawable.ic_stat_notify_msg)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        mgr.notify(NOTIFICATION_ID++, mBuilder.build());

    }
}
