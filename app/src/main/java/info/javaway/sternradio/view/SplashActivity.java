package info.javaway.sternradio.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import info.javaway.sternradio.App;
import info.javaway.sternradio.BuildConfig;
import info.javaway.sternradio.R;
import info.javaway.sternradio.Utils;
import info.javaway.sternradio.handler.PrefManager;
import info.javaway.sternradio.handler.SignalManager;
import info.javaway.sternradio.model.Alarm;
import info.javaway.sternradio.storage.ConstantStorage;

public class SplashActivity extends AppCompatActivity {

    TextView infoText;
    Button grantedPermissionsButton;
    private Alarm alarm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        infoText = findViewById(R.id.info_text);
        grantedPermissionsButton = findViewById(R.id.granted_perm_btn);

        Intent intent = getIntent();
        if (intent != null && intent.getAction() != null){
            if(intent.getAction().equals(ConstantStorage.ACTION_ALARM)){
                if(cancelOrContinue()){
                    if(!Utils.getNetworkState()){
                        startFakeMelodyScreen();
                    } else {
                        startRootScreen();
                    }
                }
                finish();
                return;
            }
        }

        boolean b1 = (ContextCompat.checkSelfPermission(App.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED);
        boolean b2 = (ContextCompat.checkSelfPermission(App.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED);
        boolean b3 = (ContextCompat.checkSelfPermission(App.getContext(), Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED);
        boolean b4 = (ContextCompat.checkSelfPermission(App.getContext(), Manifest.permission.ACCESS_NETWORK_STATE)
                == PackageManager.PERMISSION_GRANTED);
        boolean b5 = (ContextCompat.checkSelfPermission(App.getContext(), Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED);

        if (b1 && b2 && b3 && b4 && b5) {
            infoText.setVisibility(View.GONE);
            grantedPermissionsButton.setVisibility(View.GONE);
            startRootScreen();
        }


        grantedPermissionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions();
            }
        });

    }

    private void startFakeMelodyScreen() {
Intent intent = new Intent(App.getContext(), FakeMelodyActivity.class);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES).
                addFlags(Intent.FLAG_RECEIVER_FOREGROUND).
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void startRootScreen() {
        Intent intent = new Intent(App.getContext(), RootActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void checkPermissions() {
        boolean b1 = (ContextCompat.checkSelfPermission(App.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED);
        boolean b2 = (ContextCompat.checkSelfPermission(App.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED);
        boolean b3 = (ContextCompat.checkSelfPermission(App.getContext(), Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED);
        boolean b4 = (ContextCompat.checkSelfPermission(App.getContext(), Manifest.permission.ACCESS_NETWORK_STATE)
                == PackageManager.PERMISSION_GRANTED);
        boolean b5 = (ContextCompat.checkSelfPermission(App.getContext(), Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED);
        if (!(b1 && b2 && b3 && b4 && b5)) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.READ_PHONE_STATE},
                    2121);
        } else {
           startRootScreen();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Map<String, Integer> perms = new HashMap<String, Integer>();
        perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
        for (int i = 0; i < permissions.length; i++)
            perms.put(permissions[i], grantResults[i]);
        if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && perms.get(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && perms.get(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            // All Permissions Granted
            startRootScreen();
        } else {
            // Permission Denied
            Toast.makeText(SplashActivity.this, "Some Permission is Denied", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private boolean cancelOrContinue() {
        alarm = Alarm.getInstance();
        if (!alarm.isSingleAlarm()) {
            SignalManager.getInstance(App.getContext()).setAlarm(alarm);
        } else {
            SignalManager.getInstance(App.getContext()).cancelAlarm();
            PrefManager.setCheckedAlarm(false);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (alarm.isSingleAlarm()) return true;
        switch (dayOfWeek) {
            case 1: {
                if (alarm.isSunday()) {
                    break;
                }
                return false;
            }
            case 2: {
                if (alarm.isMonday()) {
                    break;
                }
                return false;
            }
            case 3: {
                if (alarm.isTuesday()) {
                    break;
                }
                return false;
            }
            case 4: {
                if (alarm.isWednesday()) {
                    break;
                }
                return false;
            }
            case 5: {
                if (alarm.isThursday()) {
                    break;
                }
                return false;
            }
            case 6: {
                if (alarm.isFriday()) {
                    break;
                }
                return false;
            }
            case 7: {
                if (alarm.isSaturday()) {
                    break;
                }
                return false;
            }
        }
        return true;
    }
}
