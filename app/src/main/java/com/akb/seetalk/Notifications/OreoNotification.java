package com.akb.seetalk.Notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;

import androidx.annotation.RequiresApi;

import com.akb.seetalk.R;

public class OreoNotification extends ContextWrapper {
    private static final String CHANNEL_ID = "com.akb.seetalk";
    private static final String CHANNEL_NAME = "SeeTalk";

    public NotificationManager notificationManager;


    public OreoNotification(Context base) {
        super(base);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        channel.enableLights(true);
        channel.setSound(defaultSound, null);
        channel.setVibrationPattern(new long[] {0});
        channel.enableVibration(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        }

        return notificationManager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getOreoNotification(String title, String body, PendingIntent pendingIntent, Uri defaultSound, String icon) {
        return new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_chat)
                .setSound(defaultSound)
                .setAutoCancel(true);

    }
}
