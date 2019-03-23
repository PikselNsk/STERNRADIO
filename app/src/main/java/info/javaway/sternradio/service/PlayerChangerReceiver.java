package info.javaway.sternradio.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import info.javaway.sternradio.Utils;

public class PlayerChangerReceiver extends BroadcastReceiver {

    private static PlayerChangerReceiver instance;

    public static PlayerChangerReceiver getInstance(Context context) {
        if (instance == null){
            instance = new PlayerChangerReceiver();
        }
        return instance;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Utils.simpleLog("network change");

//        MusicHandler.getInstance().notifyAboutNetworkState();
    }
}
