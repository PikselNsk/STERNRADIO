package info.javaway.sternradio.handler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

import info.javaway.sternradio.App;
import info.javaway.sternradio.model.Alarm;
import info.javaway.sternradio.view.RootActivity;
import info.javaway.sternradio.view.WakeupActivity;

public class SignalManager {

    public static final int SHIFT_ID_FOR_BACKUP_INTENT = 333;
    private static final int SHIFT_ID_FOR_BROADCAST_INTENT = 34834;
    private static final long SHIFT_TIME_BROADCAST = 5_000;
    private static final long SHIFT_TIME_BROADCAST_SET_ALARM_CLOCK = 17_000;
    private static final int SHIFT_ID_FOR_SETALARMCLOCK_BROADCAST_INTENT = 3245;
    private static final int ID_STERN = 666;
    private static final long SHIFT_TIME = 10_000;
    public static final String STERNRADIO_ACTION_ALARM_FIRE = "sternradio.ACTION_ALARM_FIRE";

    private AlarmManager mAlarmManager;
    private static SignalManager instance;

    public SignalManager(Context context) {
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public static SignalManager getInstance(Context context) {
        if (instance == null) {
            instance = new SignalManager(context);
        }
        return instance;
    }


    public void setAlarm(Alarm alarm) {

        Calendar calendarCurrent = Calendar.getInstance();

        Intent intentMainAlarm = new Intent(App.getContext(), WakeupActivity.class);
        intentMainAlarm.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES).
                addFlags(Intent.FLAG_RECEIVER_FOREGROUND).
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntentForMainAlarm = PendingIntent.getActivity(
                App.getContext(), ID_STERN,
                intentMainAlarm,
                PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent backupPendingIntent = PendingIntent.getActivity(
                App.getContext(),
                ID_STERN + SHIFT_ID_FOR_BACKUP_INTENT,
                intentMainAlarm,
                PendingIntent.FLAG_UPDATE_CURRENT);


        Intent intentForBroadcast = new Intent(App.getContext(), WakeupActivity.class);
        intentForBroadcast.setAction(STERNRADIO_ACTION_ALARM_FIRE);
        intentForBroadcast.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES).
                addFlags(Intent.FLAG_RECEIVER_FOREGROUND).
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent sendBroadcastMainAlarmPendingIntent = PendingIntent.getBroadcast(
                App.getContext(),
                ID_STERN + SHIFT_ID_FOR_BROADCAST_INTENT,
                intentForBroadcast,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        PendingIntent sendBroadcastWithSetAlarmClockPendingIntent = PendingIntent.getBroadcast(
                App.getContext(),
                ID_STERN + SHIFT_ID_FOR_SETALARMCLOCK_BROADCAST_INTENT,
                intentForBroadcast,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        Calendar tempCal = Calendar.getInstance();
        calendarCurrent.setTimeInMillis(System.currentTimeMillis());
        tempCal.setTimeInMillis(System.currentTimeMillis());

        tempCal.set(Calendar.SECOND, 10);
        calendarCurrent.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        calendarCurrent.set(Calendar.MINUTE, alarm.getMinute());
        calendarCurrent.set(Calendar.SECOND, 0);

        if (tempCal.after(calendarCurrent)) {
            calendarCurrent.add(Calendar.DAY_OF_MONTH, 1);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //интент для открытия экрана при надатии значка будильника
            Intent openMainScreenAlarmIntent = new Intent(App.getContext(), RootActivity.class);

            PendingIntent openMainScreenPendingIntent = PendingIntent.getActivity(
                    App.getContext(),
                    39,
                    openMainScreenAlarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(
                    calendarCurrent.getTimeInMillis(),
                    openMainScreenPendingIntent);

            mAlarmManager.setAlarmClock(alarmClockInfo,pendingIntentForMainAlarm);

            AlarmManager.AlarmClockInfo alarmClockInfoToBroadcast
                    = new AlarmManager.AlarmClockInfo(
                            calendarCurrent.getTimeInMillis() + SHIFT_TIME_BROADCAST_SET_ALARM_CLOCK,
                    openMainScreenPendingIntent);

            mAlarmManager.setAlarmClock(alarmClockInfoToBroadcast, sendBroadcastWithSetAlarmClockPendingIntent);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendarCurrent.getTimeInMillis() + SHIFT_TIME, backupPendingIntent);
            mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendarCurrent.getTimeInMillis() +
                    SHIFT_TIME_BROADCAST, sendBroadcastMainAlarmPendingIntent);
        } else {
            mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, calendarCurrent.getTimeInMillis(), pendingIntentForMainAlarm);
            mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, calendarCurrent.getTimeInMillis() +
                    SHIFT_TIME_BROADCAST, sendBroadcastMainAlarmPendingIntent);
        }
    }


    private void cancelAlarm() {
        Intent intent;
        PendingIntent pendingIntent;
        intent = new Intent(App.getContext(), WakeupActivity.class);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);

        pendingIntent = PendingIntent.getActivity(
                App.getContext(),
                ID_STERN,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent backupPendingIntent = PendingIntent.getActivity(
                App.getContext(),
                ID_STERN + SHIFT_ID_FOR_BACKUP_INTENT,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //Интент для обратной совместимости
        Intent intentForBroadcast = new Intent();
        intentForBroadcast.setAction(STERNRADIO_ACTION_ALARM_FIRE);
        intentForBroadcast.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES).
                addFlags(Intent.FLAG_RECEIVER_FOREGROUND).
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent sendBroadcastMainAlarm = PendingIntent.getBroadcast(
                App.getContext(),
                ID_STERN + SHIFT_ID_FOR_BROADCAST_INTENT,
                intentForBroadcast,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        //ПИ для совместимости
        PendingIntent sendBroadcastWithSetAlarmClockPendingIntent = PendingIntent.getBroadcast(
                App.getContext(),
                ID_STERN + SHIFT_ID_FOR_SETALARMCLOCK_BROADCAST_INTENT,
                intentForBroadcast,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        Intent intentForBroadcastNew = new Intent(App.getContext(), WakeupActivity.class);
        intentForBroadcastNew.setAction(STERNRADIO_ACTION_ALARM_FIRE);
        intentForBroadcastNew.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES).
                addFlags(Intent.FLAG_RECEIVER_FOREGROUND).
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //ПИ для api >= 26
        PendingIntent sendBroadcastMainAlarmNew = PendingIntent.getBroadcast(
                App.getContext(),
                ID_STERN + SHIFT_ID_FOR_BROADCAST_INTENT,
                intentForBroadcastNew,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        PendingIntent sendBroadcastWithSetAlarmClockPendingIntentNew = PendingIntent.getBroadcast(
                App.getContext(),
                ID_STERN + SHIFT_ID_FOR_SETALARMCLOCK_BROADCAST_INTENT,
                intentForBroadcastNew,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        pendingIntent.cancel();
        backupPendingIntent.cancel();
        sendBroadcastMainAlarm.cancel();
        sendBroadcastWithSetAlarmClockPendingIntent.cancel();
        sendBroadcastMainAlarmNew.cancel();
        sendBroadcastWithSetAlarmClockPendingIntentNew.cancel();
        mAlarmManager.cancel(pendingIntent);
        mAlarmManager.cancel(backupPendingIntent);
        mAlarmManager.cancel(sendBroadcastMainAlarm);
        mAlarmManager.cancel(sendBroadcastWithSetAlarmClockPendingIntent);
        mAlarmManager.cancel(sendBroadcastMainAlarmNew);
        mAlarmManager.cancel(sendBroadcastWithSetAlarmClockPendingIntentNew);
    }
}
