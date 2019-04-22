package info.javaway.sternradio;

import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;

import org.acra.ACRA;
import org.acra.BuildConfig;
import org.acra.ReportField;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.data.StringFormat;

import androidx.multidex.MultiDex;

import info.javaway.sternradio.handler.PrefManager;
import info.javaway.sternradio.service.MusicServiceStream;

public class App extends Application {

    private static Context context;
    private static App instance;
    private NetworkChangerReceiver networkStateChangeReceiver;
    private static MusicServiceStream musicService;

    public App() {
        instance = this;
        context = this;
    }

    public static App get(){
        return instance;
    }

    public static Context getContext() {
        return context;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        PrefManager.init(this);

        networkStateChangeReceiver = new NetworkChangerReceiver();
        registerReceiver(networkStateChangeReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        bindService(new Intent(this, MusicServiceStream.class), mConnection, Service.BIND_AUTO_CREATE);
        MultiDex.install(this);

        CoreConfigurationBuilder builder = new CoreConfigurationBuilder(this);

        builder.setReportField(ReportField.ANDROID_VERSION, true);
        builder.setReportField(ReportField.APP_VERSION_CODE, true);
        builder.setReportField(ReportField.APP_VERSION_NAME, true);
        builder.setReportField(ReportField.APPLICATION_LOG, true);
        builder.setReportField(ReportField.CRASH_CONFIGURATION, true);
        builder.setReportField(ReportField.DEVICE_ID, true);
        builder.setReportField(ReportField.DEVICE_FEATURES, true);
        builder.setReportField(ReportField.PHONE_MODEL, true);
        builder.setReportField(ReportField.CUSTOM_DATA, true);

        builder.setReportContent(ReportField.ANDROID_VERSION,
                ReportField.APP_VERSION_CODE,
                ReportField.APP_VERSION_NAME,
                ReportField.CRASH_CONFIGURATION,
                ReportField.DEVICE_ID,
                ReportField.DEVICE_FEATURES,
                ReportField.PHONE_MODEL,
                ReportField.CUSTOM_DATA,
                ReportField.APPLICATION_LOG,
                ReportField.LOGCAT);

        builder.setBuildConfigClass(BuildConfig.class)
                .setReportFormat(StringFormat.JSON);

        ACRA.init(this, builder);
    }



    @Override
    public void onTerminate() {
        Utils.simpleLog("Class: " + "App " + "Method: " + "onTerminate");
        unregisterReceiver(networkStateChangeReceiver);
        unbindService(mConnection);
        musicService.stopSelf();
        super.onTerminate();

    }

    public static MusicServiceStream getMusicService() {
        return musicService;
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Utils.simpleLog("Class: " + "App " + "Method: " + "onServiceConnected " + service);
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            try {
                MusicServiceStream.MediaPlayerBinder binder = (MusicServiceStream.MediaPlayerBinder) service;
                musicService = binder.getService();
            } catch (Exception e){
                Utils.saveLog(e.getMessage());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }
    };

    class NetworkChangerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            Utils.saveLog("change network state " + Utils.getNetworkState());
            if (musicService!=null) musicService.restoreNetwork();
        }
    }

}
