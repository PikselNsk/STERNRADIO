package info.javaway.sternradio.service;

import android.content.Context;
import android.content.Intent;

import androidx.legacy.content.WakefulBroadcastReceiver;

import java.util.Calendar;

import info.javaway.sternradio.App;
import info.javaway.sternradio.handler.PrefManager;
import info.javaway.sternradio.storage.ConstantStorage;
import info.javaway.sternradio.view.RootActivity;

public class WakeUpBroadcastReciever extends WakefulBroadcastReceiver {

    private static String TAG = "info.javaway.sternradio.service.WakeupBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        switch (dayOfWeek) {
            case 1: {
                if (PrefManager.getSunday()) {
                    break;
                }
                return;
            }
            case 2: {
                if (PrefManager.getMonday()) {
                    break;
                }
                return;
            }
            case 3: {
                if (PrefManager.getTuesday()) {
                    break;
                }
                return;
            }
            case 4: {
                if (PrefManager.getWednesday()) {
                    break;
                }
                return;
            }
            case 5: {
                if (PrefManager.getThursday()) {
                    break;
                }
                return;
            }
            case 6: {
                if (PrefManager.getFriday()) {
                    break;
                }
                return;
            }
            case 7: {
                if (PrefManager.getSaturday()) {
                    break;
                }
                return;
            }
        }

        Intent myIntent;
        myIntent = new Intent(context, RootActivity.class);
        myIntent.
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES).
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        myIntent.setAction(ConstantStorage.ACTION_ALARM);
        App.getContext().startActivity(myIntent);
    }
}
