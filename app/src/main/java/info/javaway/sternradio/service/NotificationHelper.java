package info.javaway.sternradio.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import info.javaway.sternradio.App;
import info.javaway.sternradio.R;
import info.javaway.sternradio.Utils;
import info.javaway.sternradio.view.RootActivity;

import static info.javaway.sternradio.service.MusicServiceStream.ACTION_CLOSE;
import static info.javaway.sternradio.service.MusicServiceStream.ACTION_PAUSE;
import static info.javaway.sternradio.service.MusicServiceStream.ACTION_PAUSE_CANCEL;

public class NotificationHelper {
    private static final String CHANNEL_ID_1 = "info.javaway.CHANNEL_ID_1";
    private static final String CHANNEL_ID_2 = "info.javaway.CHANNEL_ID_2";

    public static void sendNotification(
            Context ctx,
            String title,
            int notificationNumber,
            String message,
            String subtext,
            Intent intent,
            boolean isPause) {
        try {

            NotificationManager notificationManager =
                    (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        CHANNEL_ID_1,
                        "My Notifications",
                        NotificationManager.IMPORTANCE_LOW);

                // Configure the notification channel.
                notificationChannel.setDescription("Sternradio");
                notificationChannel.enableLights(false);
                notificationChannel.setSound(null, null);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                notificationChannel.enableVibration(false);
                notificationManager.createNotificationChannel(notificationChannel);
            }


            NotificationCompat.Builder notificationBuilder = new
                    NotificationCompat.Builder(ctx, CHANNEL_ID_1);


            notificationBuilder.setAutoCancel(false)
                    .setOngoing(true)
                    .setSound(null)
                    .setSound(null, AudioManager.STREAM_NOTIFICATION)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker("Ticker")
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    //     .setPriority(Notification.PRIORITY_MAX)
                    .setContentTitle("Sternradio")
                    .setContentText("Live Stream")
                    .setContentInfo("Info sternradio");

            if (isPause) {
                notificationBuilder.addAction(generateAction(android.R.drawable.ic_media_play, "Play", NotificationControlService.ACTION_PAUSE_CANCEL));
            } else {
                notificationBuilder.addAction(generateAction(android.R.drawable.ic_media_pause, "Pause", NotificationControlService.ACTION_PAUSE));
            }
            notificationBuilder.addAction(generateAction(android.R.drawable.ic_menu_close_clear_cancel, "Close", NotificationControlService.ACTION_CLOSE));

            Notification notification = notificationBuilder.build();
            notificationManager.notify(/*notification id*/1, notification);
        } catch (Exception e) {
            Utils.saveLog(e.getMessage());
        }
    }

    private static NotificationCompat.Action generateAction(int icon, String title, String intentAction) {

//        Intent intent = new Intent(intentAction);
//        intent.setAction(intentAction);
        Intent intent = new Intent(App.getContext(), NotificationControlService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(App.getContext(), 1, intent, 0);
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }


}
