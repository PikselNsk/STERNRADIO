package info.javaway.sternradio.handler;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {

    public static final String PREFERENCES = "info.javaway.sternradio.handler.PREF";
    private static final String PREF_NOTIFICATION = "PREF_NOTIFICATION";
    public static final String STERNRADIO_HOUR = "sternradio.HOUR";
    public static final String STERNRADIO_MINUTE = "sternradio.MINUTE";
    public static final String STERNRADIO_SUNDAY = "sternradio.sunday";
    public static final String STERNRADIO_MONDAY = "sternradio.monday";
    public static final String STERNRADIO_TUESDAY = "sternradio.tuesday";
    public static final String STERNRADIO_WEDNESDAY = "sternradio.wednesday";
    public static final String STERNRADIO_THURSDAY = "sternradio.thursday";
    public static final String STERNRADIO_FRIDAY = "sternradio.friday";
    public static final String STERNRADIO_SATURDAY = "sternradio.satyrday";
    public static final String STERNRADIO_ALARM = "sternradio.alarm";

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

    public static boolean getSunday() {
        return mySharedPreferences.getBoolean(STERNRADIO_SUNDAY, true);
    }

    public static void setSunday(boolean value){
        mySharedPreferences.edit().putBoolean(STERNRADIO_SUNDAY, value).apply();
    }

    public static boolean getMonday() {
        return mySharedPreferences.getBoolean(STERNRADIO_MONDAY, true);
    }

    public static void setMonday(boolean value){
        mySharedPreferences.edit().putBoolean(STERNRADIO_MONDAY, value).apply();
    }

    public static boolean getTuesday() {
        return mySharedPreferences.getBoolean(STERNRADIO_TUESDAY, true);

    }
    public static void setTuesday(boolean value){
        mySharedPreferences.edit().putBoolean(STERNRADIO_TUESDAY, value).apply();
    }
    public static boolean getWednesday() {
        return mySharedPreferences.getBoolean(STERNRADIO_WEDNESDAY, true);

    }
    public static void setWednesday(boolean value){
        mySharedPreferences.edit().putBoolean(STERNRADIO_WEDNESDAY, value).apply();
    }
    public static boolean getThursday() {
        return mySharedPreferences.getBoolean(STERNRADIO_THURSDAY, true);

    }
    public static void setThursday(boolean value){
        mySharedPreferences.edit().putBoolean(STERNRADIO_THURSDAY, value).apply();
    }
    public static boolean getFriday() {
        return mySharedPreferences.getBoolean(STERNRADIO_FRIDAY, true);
    }
    public static void setFriday(boolean value){
        mySharedPreferences.edit().putBoolean(STERNRADIO_FRIDAY, value).apply();
    }
    public static boolean getSaturday() {
        return mySharedPreferences.getBoolean(STERNRADIO_SATURDAY, true);
    }
    public static void setSaturday(boolean value){
        mySharedPreferences.edit().putBoolean(STERNRADIO_SATURDAY, value).apply();
    }
    public static int getHour() {
        return mySharedPreferences.getInt(STERNRADIO_HOUR, 8);
    }
    public static void setHour(int value){
        mySharedPreferences.edit().putInt(STERNRADIO_HOUR, value).apply();
    }
    public static int getMinute() {
        return mySharedPreferences.getInt(STERNRADIO_MINUTE, 30);
    }
    public static void setMinute(int value){
        mySharedPreferences.edit().putInt(STERNRADIO_MINUTE, value).apply();
    }

    public static boolean isCheckedAlarm() {
        return mySharedPreferences.getBoolean(STERNRADIO_ALARM, false);
    }
    public static void setCheckedAlarm(boolean value){
        mySharedPreferences.edit().putBoolean(STERNRADIO_ALARM, value).apply();
    }
}
