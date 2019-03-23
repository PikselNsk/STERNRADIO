package info.javaway.sternradio.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import info.javaway.sternradio.App;
import info.javaway.sternradio.Utils;
import info.javaway.sternradio.handler.MusicHandler;
import info.javaway.sternradio.handler.MusicStreamHandler;

public class NetworkChangerReceiver extends BroadcastReceiver {

    private static NetworkChangerReceiver instance;

    public static NetworkChangerReceiver getInstance(Context context) {
        if (instance == null){
            instance = new NetworkChangerReceiver();
        }
        return instance;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Utils.simpleLog("network change");
        MusicStreamHandler.getInstance().notifyAboutNetworkState();
    }
}
