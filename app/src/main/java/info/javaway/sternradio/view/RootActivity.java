package info.javaway.sternradio.view;

import android.Manifest;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.suke.widget.SwitchButton;
import com.wang.avi.AVLoadingIndicatorView;

import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import info.javaway.sternradio.App;
import info.javaway.sternradio.R;
import info.javaway.sternradio.Utils;
import info.javaway.sternradio.handler.PrefManager;
import info.javaway.sternradio.presenter.RootPresenter;
import info.javaway.sternradio.service.NotificationHelper;
import info.javaway.sternradio.storage.ConstantStorage;
import info.javaway.sternradio.view.dialog.InfoDialog;
import info.javaway.sternradio.view.dialog.SettingsDialog;
import info.javaway.sternradio.view.fragment.AlarmFragment;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static info.javaway.sternradio.service.MusicServiceStream.ACTION_CLOSE;
import static info.javaway.sternradio.service.MusicServiceStream.ACTION_PAUSE;
import static info.javaway.sternradio.service.MusicServiceStream.ACTION_PAUSE_CANCEL;
import static info.javaway.sternradio.service.NotificationHelper.STERN_NOTIFICATION_ID;

public class RootActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        AlarmFragment.OnFragmentInteractionListener,
        RootPresenter.View {

    public static final String UPDATE_PLAYER = "info.javaway.sternradio.UPDATE_PLAYER";
    public static final String BUFFERING = "info.javaway.sternradio.BUFFERING";
    private static RootPresenter presenter;
    BarVisualizer barVisualizer;
    private Toolbar toolbar;
    private TextView trackNameTv;
    private TextView nextTrackNameTv;
    private AVLoadingIndicatorView avi;
    private ImageView playButtonIv;
    private PlayerChangerReceiver playerStateChangeReceiver;
    private long back_pressed;
    private SwitchButton switcher;
    private MenuItem menuAlarmItem;


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.simpleLog("RootActivity onCreate Thread name : " + Thread.currentThread().getName());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils.setAppCompatActivity(this);
        toolbar = findViewById(R.id.toolbar);
        trackNameTv = findViewById(R.id.track_name_tv);
        nextTrackNameTv = findViewById(R.id.next_track_tv);
        barVisualizer = findViewById(R.id.bar_visualiser);
        playButtonIv = findViewById(R.id.play_btn_iv);
        avi = findViewById(R.id.avi);
        menuAlarmItem = ((NavigationView) findViewById(R.id.nav_view))
                .getMenu()
                .findItem(R.id.nav_alarm);
        switcher = menuAlarmItem
                .getActionView()
                .findViewById(R.id.alarm_switcher);

        toolbar.setBackgroundColor(ContextCompat.getColor(App.getContext(),R.color.transparent));
        presenter = RootPresenter.getInstance();
        presenter.takeView(this);

        switcher.setChecked(PrefManager.isCheckedAlarm());
        switcher.setOnCheckedChangeListener((view, isChecked) -> presenter.switchAlarm(isChecked));
        playButtonIv.setOnClickListener(v -> {
            if (avi.getVisibility() == View.VISIBLE) return;
            presenter.clickOnPlayButton();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    boolean ignoringBatteryOptimizations = ((PowerManager) getSystemService(Context.POWER_SERVICE)).isIgnoringBatteryOptimizations(getPackageName());
                    if (!ignoringBatteryOptimizations) {
                        startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + getPackageName())));
                    }
                } catch (Throwable e) {
                }
            }
        });
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(App.getContext());
        playerStateChangeReceiver = new PlayerChangerReceiver();
        IntentFilter playerFilter = new IntentFilter();
        playerFilter.addAction(BUFFERING);
        playerFilter.addAction(UPDATE_PLAYER);
        playerFilter.addAction(ACTION_PAUSE_CANCEL);
        playerFilter.addAction(ACTION_PAUSE);
        playerFilter.addAction(ACTION_CLOSE);
        localBroadcastManager.registerReceiver(playerStateChangeReceiver,
                playerFilter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                boolean ignoringBatteryOptimizations = ((PowerManager) getSystemService(Context.POWER_SERVICE)).isIgnoringBatteryOptimizations(getPackageName());
                if (!ignoringBatteryOptimizations) {
                    startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + getPackageName())));
                }
            } catch (Throwable e) {
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.simpleLog("Class: " + "RootActivity " + "Method: " + "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Utils.simpleLog("Class: " + "RootActivity " + "Method: " + "onStop");
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResume();
        Utils.simpleLog("Class: " + "RootActivity " + "Method: " + "onResume");
    }

    @Override
    protected void onDestroy() {
        Utils.saveLog("Class: " + "RootActivity " + "Method: " + "onDestroy");
        Utils.simpleLog("Class: " + "RootActivity " + "Method: " + "onDestroy");
        NotificationManager notificationManager =
                (NotificationManager) App.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(STERN_NOTIFICATION_ID);
        presenter.dropView();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(App.getContext());
        localBroadcastManager.unregisterReceiver(playerStateChangeReceiver);
        if (barVisualizer != null) {
            barVisualizer.release();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            if (back_pressed + 2000 > System.currentTimeMillis()) {
                finish();
                presenter.release();
                System.exit(0);
            } else {
                Toast.makeText(this, "Нажмите \"назад\" для выхода", Toast.LENGTH_SHORT).show();
            }
            back_pressed = System.currentTimeMillis();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            new SettingsDialog().show(getSupportFragmentManager(), "Settings");
        } else if (id == R.id.nav_info) {
            InfoDialog infoDialog = new InfoDialog();
            Bundle args = new Bundle();
            args.putString(ConstantStorage.INFO_TEXT, ConstantStorage.ABOUT_RADIO_TEXT);
            infoDialog.setArguments(args);
            infoDialog.show(getSupportFragmentManager(), "About radio");
        } else if (id == R.id.nav_share) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, "Sternradio"
                    + "\n"
                    + "https://play.google.com/store/apps/details?id=info.javaway.sternradio");
            startActivity(Intent.createChooser(i, "Share Sternradio"));
        } else if (id == R.id.nav_email) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "goodalarmclock@gmail.com", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Sternradio");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "");
            startActivity(Intent.createChooser(emailIntent, "Send email to Sternradio"));
        } else if (id == R.id.nav_www) {
            String url = "http://sternradio.ru/";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        } else if (id == R.id.nav_beta) {
            InfoDialog infoDialog = new InfoDialog();
            Bundle args = new Bundle();
            args.putString(ConstantStorage.INFO_TEXT, ConstantStorage.BETA_VERSION_TEXT);
            infoDialog.setArguments(args);
            infoDialog.show(getSupportFragmentManager(), "Beta info");
        } else if (id == R.id.nav_alarm) {
            AlarmFragment alarmFragment = AlarmFragment.newInstance("", "");
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, alarmFragment)
                    .commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void showError(String messageError) {
        trackNameTv.setText(getString(R.string.network_error));
    }

    @Override
    public void showMessage(String message) {
        Snackbar.make(findViewById(R.id.drawer_layout), message,
                Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showLoading() {
        avi.show();
        barVisualizer.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideLoading() {
        Utils.simpleLog("Class: " + "RootActivity " + "Method: " + "hideLoading");
        avi.hide();
        barVisualizer.setVisibility(View.VISIBLE);
    }

    @Override
    public void setTrackInfo(String trackName) {
        trackNameTv.setText(trackName);
    }

    @Override
    public void setNextTrackInfo(String trackName) {
        nextTrackNameTv.setText(trackName);
    }

    @Override
    public void initialVisualBar() {
        int audioSessionId = presenter.getMediaPlayer().getAudioSessionId();
        if (audioSessionId != -1) {
            try {
                barVisualizer.setAudioSessionId(audioSessionId);
            } catch (IllegalStateException ex) {
                Utils.saveLog(ex.getMessage());
            }
        }
    }

    @Override
    public void showPlayButton() {
        playButtonIv.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.play));
    }

    @Override
    public void showPauseButton() {
        playButtonIv.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pause));
    }

    @Override
    public void showDescribeAlarm(String text) {
        menuAlarmItem.setTitle(text);
    }

    @Override
    public void cancelAlarm() {
        switcher.setChecked(false);
    }

    @Override
    public void alarmSwitch(boolean isSwitch) {
        switcher.setChecked(isSwitch);
        presenter.switchAlarm(isSwitch);
    }


    public class PlayerChangerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            Utils.simpleLog("Class: " + "PlayerChangerReceiver " + "Method: " + "onReceive " + intent.getAction());
            switch (intent.getAction()) {
                case BUFFERING: {
                    showLoading();
                    break;
                }
                case UPDATE_PLAYER: {
                    hideLoading();
                    break;
                }
                case ACTION_PAUSE: {
                    presenter.clickOnPlayButton();
                    break;
                }
                case ACTION_PAUSE_CANCEL: {
                    presenter.clickOnPlayButton();
                    break;
                }
                case ACTION_CLOSE: {
                    finish();

                    presenter.release();
                    break;
                }
            }
        }
    }

}
