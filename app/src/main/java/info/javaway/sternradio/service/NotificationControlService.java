package info.javaway.sternradio.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import info.javaway.sternradio.Utils;
import info.javaway.sternradio.view.RootActivity;

public class NotificationControlService extends Service {
    public static final String ACTION_PAUSE_CANCEL = "NotificationControlService.ACTION_PAUSE_CANCEL";
    public static final String ACTION_PAUSE = "NotificationControlService.ACTION_PAUSE";
    public static final String ACTION_CLOSE = "NotificationControlService.ACTION_CLOSE";
    private Intent bufferingPlayerIntent;

    public NotificationControlService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.simpleLog("Class: " + "NotificationControlService " + "Method: " + "onStartCommand ");
        String action = intent.getAction();

        bufferingPlayerIntent = new Intent();
        switch (action) {
            case ACTION_CLOSE:{
                bufferingPlayerIntent.setAction(MusicServiceStream.ACTION_CLOSE);
                break;
            }
            case ACTION_PAUSE:{
                bufferingPlayerIntent.setAction(MusicServiceStream.ACTION_PAUSE);
                break;
            }
            case ACTION_PAUSE_CANCEL: {
                bufferingPlayerIntent.setAction(MusicServiceStream.ACTION_PAUSE_CANCEL);
                break;
            }
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(bufferingPlayerIntent);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
