package info.javaway.sternradio;


import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class Utils {

    public static boolean getNetworkState;
    private static SimpleDateFormat dateFormat;
    private static int cardColor;
    private static int textColor;
    private static int favoritePick;


    public static void simpleLog(String message){
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



    public static void sendToast(String text) {
        Toast.makeText(App.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    public static SimpleDateFormat getDateFormat() {
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        }
        return dateFormat;
    }

    public static boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                App.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }



    public static void saveLog(String logMessage) {
// TODO: 27.01.2019 раскомментировать для дебага
        Runnable runnable = () -> {
            String dataDir = App.getContext().getApplicationInfo().dataDir;
            File path = new File("sdcard/blocknote_log/");
            File logFile = new File("sdcard/blocknote_log/note_log.txt");

            if (!logFile.exists()) {
                try {
                    path.mkdirs();
                    logFile.createNewFile();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {

                //BufferedWriter for performance, true to set append to file flag
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                buf.append(" : ");
                buf.append(logMessage);
                buf.newLine();
                buf.close();
            } catch (Exception e) {
                e.printStackTrace();
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
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "My Notebook Error");
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
        } catch (Exception e){
            Utils.saveLog("Error copy backup \n");
            Utils.saveLog(e.getMessage());
        }
    }

    public static boolean getNetworkState() {
        // TODO: 26.02.2019 добавить проверку доступности сетиэ
        return true;
    }
}
