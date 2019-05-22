package info.javaway.sternradio.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import info.javaway.sternradio.App;
import info.javaway.sternradio.R;
import info.javaway.sternradio.handler.PrefManager;
import info.javaway.sternradio.handler.SignalManager;

public class AlarmCancelService extends Service{

    public static final String ACTION_CLOSE = "close";
    private int id = 43;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent,  int flags, int startId) {
        startForeground(id, NotificationHelper.getNotification(getApplicationContext()));
        SignalManager.getInstance(App.getContext()).cancelAlarm();
        PrefManager.setCheckedAlarm(false);
        Toast.makeText(App.getContext(), App.getContext().getString(R.string.alarm_off), Toast.LENGTH_SHORT).show();
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }
}
