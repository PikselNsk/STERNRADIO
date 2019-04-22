package info.javaway.sternradio.handler;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {

    public static final String PREFERENCES = "info.javaway.sternradio.handler.PREF";
    private static final String PREF_NOTIFICATION = "PREF_NOTIFICATION";
    private static PrefManager instance;
    private static SharedPreferences mySharedPreferences;

    private PrefManager(Context context) {
        mySharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public static PrefManager init(Context context){
        if(instance == null){
            instance = new PrefManager(context);
        }
        return instance;
    }

    public static boolean getNotification() {
        return mySharedPreferences.getBoolean(PREF_NOTIFICATION, true);
    }

    public static void setNotification(boolean value){
        mySharedPreferences.edit().putBoolean(PREF_NOTIFICATION, value).apply();
    }
}
