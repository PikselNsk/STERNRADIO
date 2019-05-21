package info.javaway.sternradio;


import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import info.javaway.sternradio.model.Alarm;

public class Utils {

    public static boolean getNetworkState;
    private static SimpleDateFormat dateFormat;
    private static Date date = new Date();
    private static int cardColor;
    private static int textColor;
    private static int favoritePick;
    private static WeakReference<AppCompatActivity> weakActivity;


    public static void simpleLog(String message) {
        Log.wtf("info.javaway.sternradio", message);
    }


    public static Uri resourceToUri(Context context, int resID) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.getResources().getResourcePackageName(resID) + '/' +
                context.getResources().getResourceTypeName(resID) + '/' +
                context.getResources().getResourceEntryName(resID));
    }

    public static String getRealPathFromURI(Context context, Uri contentURI) {
        String result = null;
        String actualUri = contentURI.getPath();
        try {
            Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
            if (cursor == null) {
                result = contentURI.getPath();
            } else {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                result = cursor.getString(idx);
                cursor.close();
            }
        } catch (Exception e) {
            try {
                return getImageUrlWithAuthority(context, contentURI);
            } catch (Exception ex) {
            }
        }
        return result;
    }

    public static String getImageUrlWithAuthority(Context context, Uri uri) {
        InputStream is = null;
        if (uri.getAuthority() != null) {
            try {
                is = context.getContentResolver().openInputStream(uri);
                Bitmap bmp = BitmapFactory.decodeStream(is);
                return writeToTempImageAndGetPathUri(context, bmp).toString();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static Uri writeToTempImageAndGetPathUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    public static SimpleDateFormat getDateFormat() {
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        }
        return dateFormat;
    }


    public static void saveLog(String logMessage) {
// TODO: 27.01.2019 раскомментировать для дебага
        Runnable runnable = () -> {
//            String dataDir = App.getContext().getApplicationInfo().dataDir;
            File path = new File("sdcard/sternradio_log/");
            File logFile = new File("sdcard/sternradio_log/sternradio_log.txt");

            if (!logFile.exists()) {
                try {
                    path.mkdirs();
                    logFile.createNewFile();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                Utils.simpleLog("Class: " + "Utils " + "Method: " + "saveLog");
                //BufferedWriter for performance, true to set append to file flag
                date.setTime(System.currentTimeMillis());
                getDateFormat();
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                buf.append(dateFormat.format(date));
                buf.append(" : ");
                buf.append(logMessage);
                buf.newLine();
                buf.close();
                Utils.simpleLog("Class: " + "Utils " + "Method: " + "saveLog close");
            } catch (Exception e) {
                Utils.simpleLog("Class: " + "Utils " + "Method: " + "saveLog " + e.getMessage());
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public static String getTextFromFile(Uri uri) {
        try {
            File file = new File(uri.getPath());
            StringBuilder text = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
            return text.toString();
        } catch (Exception e) {
            return null;
        }

    }

    public static int getCardColor() {
        return cardColor;
    }

    public static void setAttributes(AppCompatActivity activity) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = activity.getTheme();
//        theme.resolveAttribute(R.attr.noteItemBackground, typedValue, true);
//        cardColor = typedValue.data;
//        theme.resolveAttribute(R.attr.titleNoteTextColor, typedValue, true);
//        textColor = typedValue.data;
//        theme.resolveAttribute(R.attr.favoritePick, typedValue, true);
//        favoritePick = typedValue.data;
    }


    public static void searchDataBaseFuckingCordova(File file) {
        File[] files = file.listFiles();
        if (files == null) {
            Utils.saveLog(file.getAbsolutePath() + ": " + file.isFile());
            return;
        } else {
            for (File f : files) {
                searchDataBaseFuckingCordova(f);
            }
        }
    }

    public static void sendErrorMessage() {
        String dataDir = App.getContext().getApplicationInfo().dataDir;
        File logFile = new File(dataDir + "/blocknote_log/note_log.txt");
        Uri path = Uri.fromFile(logFile);

        String content = null;

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(logFile));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            String ls = System.getProperty("line.separator");
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
// delete the last new line separator
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            reader.close();

            content = stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (path != null) {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
//                    emailIntent.putExtra(Intent.EXTRA_STREAM, path); // Include  the path
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "STERNRADIO ERROR");
            emailIntent.putExtra(Intent.EXTRA_TEXT, content);
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"goodalarmclock@gmail.com"});
            App.getContext().startActivity(Intent.createChooser(emailIntent, "Send email"));
        }
    }


    public static void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.flush();
                in.close();
            }
        } catch (Exception e) {
            Utils.saveLog("Error copy backup \n");
            Utils.saveLog(e.getMessage());
        }
    }

    public static boolean getNetworkState() {
        ConnectivityManager cm =
                (ConnectivityManager) App.get().getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;

        return isNetworkAvailable &&
                cm.getActiveNetworkInfo().isConnected();
    }

    public static void setAppCompatActivity(AppCompatActivity activity) {
        weakActivity = new WeakReference<>(activity);
    }

    public static void clearAppCompatActivity() {
        weakActivity.clear();
    }

    public static void checkPermissions(AppCompatActivity activity) {
        boolean b1 = (ContextCompat.checkSelfPermission(App.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED);
        boolean b2 = (ContextCompat.checkSelfPermission(App.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED);
        if (!(b1 && b2)) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.ACCESS_NETWORK_STATE},
                    2121);
        } else {
        }
    }

    public static String getStringAlarm(Context context, Alarm alarm, boolean fullDescribe) {
        StringBuilder text = new StringBuilder();
        int i = alarm.getMinute();
        text.append(alarm.getHour()).append(":");
        if (i > 9) {
            text.append(alarm.getMinute());
        } else {
            text.append("0").append(alarm.getMinute());
        }

        if (fullDescribe && !alarm.isSingleAlarm()) {
            text
                    .append(" ( ")
                    .append(alarm.isMonday() ? App.getContext().getString(R.string.monday) : "--")
                    .append(" ")
                    .append(alarm.isTuesday() ? App.getContext().getString(R.string.tuesday) : "--")
                    .append(" ")
                    .append(alarm.isWednesday() ? App.getContext().getString(R.string.wednesday) : "--")
                    .append(" ")
                    .append(alarm.isThursday() ? App.getContext().getString(R.string.thursday) : "--")
                    .append(" ")
                    .append(alarm.isFriday() ? App.getContext().getString(R.string.friday) : "--")
                    .append(" ")
                    .append(alarm.isSaturday() ? App.getContext().getString(R.string.saturday) : "--")
                    .append(" ")
                    .append(alarm.isSunday() ? App.getContext().getString(R.string.sunday) : "--")
                    .append(" )");
        }
        return text.toString();
    }

    public static String convertPathToName(String path) {
        String[] strings = path.split("/");
        return strings[strings.length - 1] == null ? path : strings[strings.length - 1];
    }

    public static String getFormatTime(Alarm alarm) {
        int i = alarm.getMinute();
        String time = "";
        time = alarm.getHour() + ":";
        if (i > 9) {
            time = time + alarm.getMinute();
        } else {
            time = time + "0" + alarm.getMinute();
        }


        return time;
    }

    public static String getDeltaTimeBeforeRing(Alarm alarm) {
        int[] delta = deltaTime(alarm);
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, delta[0]);
        calendar.add(Calendar.MINUTE, delta[1]);
        int dayOfRing = calendar.get(Calendar.DAY_OF_WEEK);

        int ringAfterDaysCount = alarm.isSingleAlarm() ? 0 : alarm.checkEnableDay(dayOfRing, 0);

        StringBuilder textAlarm = new StringBuilder();

        textAlarm.append(
                App.getContext().getString(R.string.alarm_will_ring_in_an))
                .append(" ")
                .append(delta[0] + ringAfterDaysCount * 24)
                .append(":");
        if (delta[1] < 10) {
            textAlarm.append("0")
                    .append(delta[1]);
        } else {
            textAlarm.append(delta[1]);
        }
        return textAlarm.toString();
    }

    public static int[] deltaTime(Alarm alarm) {

        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTimeInMillis(System.currentTimeMillis());
        c2.setTimeInMillis(System.currentTimeMillis());

        c2.set(Calendar.SECOND, 10);

        c1.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        c1.set(Calendar.MINUTE, alarm.getMinute());
        c1.set(Calendar.SECOND, 0);

        int h1 = c1.get(Calendar.HOUR_OF_DAY);
        int m1 = c1.get(Calendar.MINUTE);
        int h2 = c2.get(Calendar.HOUR_OF_DAY);
        int m2 = c2.get(Calendar.MINUTE);

        long delta = (h1 * 3_600_000 + m1 * 60_000) - ((h2 * 3_600_000 + m2 * 60_000));

        if (c2.after(c1)) {
            delta += 86_400_000;
        }

        int[] result = new int[2];
        long minutes = delta % 3_600_000;
        result[0] = (int) (delta / 3_600_000);
        result[1] = (int) (minutes / 60_000);
        return result;

    }
}
