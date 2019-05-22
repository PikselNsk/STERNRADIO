package info.javaway.sternradio.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import info.javaway.sternradio.App;
import info.javaway.sternradio.R;
import info.javaway.sternradio.Utils;
import info.javaway.sternradio.handler.PrefManager;
import info.javaway.sternradio.view.RootActivity;

import static info.javaway.sternradio.service.MusicServiceStream.ACTION_CLOSE;
import static info.javaway.sternradio.service.MusicServiceStream.ACTION_PAUSE;
import static info.javaway.sternradio.service.MusicServiceStream.ACTION_PAUSE_CANCEL;

public class NotificationHelper {
    private static final String CHANNEL_ID_1 = "info.javaway.CHANNEL_ID_1";
    private static final String CHANNEL_ID_2 = "info.javaway.CHANNEL_ID_2";
    public static final int STERN_NOTIFICATION_ID = 45315;
    public static final int NOTIFICATION_ALARM = 523;
    private static NotificationCompat.Builder notificationBuilder;

    public static void sendNotificationToManagment(
            Context ctx,
            String title,
            int notificationNumber,
            String message,
            String subtext,
            Intent intent,
            boolean isPause) {
        if(!PrefManager.getNotification()) return;
        Utils.saveLog("Class: " + "NotificationHelper " + "Method: " + "sendNotificationToManagment");
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
                notificationChannel.enableVibration(false);
                notificationManager.createNotificationChannel(notificationChannel);

                notificationBuilder = new
                        NotificationCompat.Builder(ctx, CHANNEL_ID_1);
            } else {
                notificationBuilder = new
                        NotificationCompat.Builder(ctx);
            }


            notificationBuilder
                    .setContentTitle("Sternradio")
                    .setContentText("Live Stream")
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setSound(null)
                    .setSmallIcon(R.mipmap.ic_launcher);

            if (isPause) {
                notificationBuilder.addAction(generateAction(android.R.drawable.ic_media_play, "Play", NotificationControlService.ACTION_PAUSE_CANCEL));
            } else {
                notificationBuilder.addAction(generateAction(android.R.drawable.ic_media_pause, "Pause", NotificationControlService.ACTION_PAUSE));
            }
            notificationBuilder.addAction(generateAction(android.R.drawable.ic_menu_close_clear_cancel, "Close", NotificationControlService.ACTION_CLOSE));

            Notification notification = notificationBuilder.build();
            notificationManager.notify(STERN_NOTIFICATION_ID, notification);
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

    private static NotificationCompat.Action generateActionCancelAlarm(int icon, String title, String intentAction) {

//        Intent intent = new Intent(intentAction);
//        intent.setAction(intentAction);
        Intent intent = new Intent(App.getContext(), AlarmCancelService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(App.getContext(), 1, intent, 0);
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    public static void sendNotificationToAlarm(Context ctx){

        try {
            Intent intentMainAlarmNotification = new Intent(ctx, RootActivity.class);
            intentMainAlarmNotification.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

            PendingIntent pendingIntent =

                    PendingIntent.getActivity(ctx,
                    333,
                    intentMainAlarmNotification,
                    PendingIntent.FLAG_NO_CREATE);

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
                notificationChannel.enableVibration(false);
                notificationManager.createNotificationChannel(notificationChannel);

                notificationBuilder = new
                        NotificationCompat.Builder(ctx, CHANNEL_ID_1);
            } else {
                notificationBuilder = new
                        NotificationCompat.Builder(ctx);
            }


            notificationBuilder
//                    .setContentIntent(pendingIntent)
                    .setContentTitle(App.getContext().getString(R.string.sternradio_alarm))
                    .setContentText(Utils.getStringAlarm(ctx, null, false))
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setSound(null)
                    .setSmallIcon(R.mipmap.ic_launcher);


            notificationBuilder.addAction(generateActionCancelAlarm(android.R.drawable.ic_menu_close_clear_cancel, App.getContext().getString(R.string.cancel_alarm), AlarmCancelService.ACTION_CLOSE));

            Notification notification = notificationBuilder.build();
            notificationManager.notify(NOTIFICATION_ALARM, notification);
        } catch (Exception e) {
            Utils.saveLog(e.getMessage());
        }
    }


    public static void clearNotification() {
        Utils.simpleLog("Class: " + "NotificationHelper " + "Method: " + "clearNotification");
        NotificationManager notificationManager =
                (NotificationManager) App.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(STERN_NOTIFICATION_ID);
    }

    public static void clearNotificationAlarm() {
        NotificationManager notificationManager =
                (NotificationManager) App.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ALARM);
    }

    public static Notification getNotification(Context context) {
        Notification notification;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intentMainAlarmNotification = new Intent(context, RootActivity.class);
        intentMainAlarmNotification.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                333,
                intentMainAlarmNotification,
                PendingIntent.FLAG_UPDATE_CURRENT);

        String alarmTimeText = "Cancel alarm";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            String channelId = "alarm_channel";
            int importance = android.app.NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = notificationManager
                    .getNotificationChannel(channelId);
            if (mChannel == null) {
                CharSequence title = "stern";
                mChannel = new NotificationChannel(channelId, title, importance);
                mChannel.enableVibration(false);
                mChannel.setSound(null, null);
                mChannel.enableLights(false);
                mChannel.setVibrationPattern(null);
                notificationManager.createNotificationChannel(mChannel);
            }


            NotificationCompat.Builder builderApi27 = new NotificationCompat.Builder(context, channelId);
            builderApi27.setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(false)
                    .setTicker(alarmTimeText)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setOngoing(true)
                    .setContentText(alarmTimeText); // Текст уведомления
            notification = builderApi27.build();
        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setSmallIcon(R.drawable.ic_launcher_foreground).
                    setContentIntent(pendingIntent).
                    setAutoCancel(false).
                    setContentText(alarmTimeText).
                    setOngoing(false);
            notification = builder.build();
        }
        return notification;
    }

}
