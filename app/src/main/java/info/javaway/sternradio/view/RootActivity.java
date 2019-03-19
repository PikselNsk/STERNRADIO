package info.javaway.sternradio.view;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.view.View;

import com.google.android.material.navigation.NavigationView;
import com.wang.avi.AVLoadingIndicatorView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import info.javaway.sternradio.R;
import info.javaway.sternradio.Utils;
import info.javaway.sternradio.model.Track;
import info.javaway.sternradio.presenter.RootPresenter;
import info.javaway.sternradio.service.MusicService;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class RootActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
RootPresenter.View{

    public static final String UPDATE_PLAYER = "UPDATE_PLAYER";
    public static final String BUFFERING = "BUFFERING";
    private static RootPresenter presenter;
    BarVisualizer barVisualizer;
    private Toolbar toolbar;
    private TextView trackNameTv;
    private TextView nextTrackNameTv;
    private AVLoadingIndicatorView avi;
    private ImageView playButtonIv;


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
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        presenter = RootPresenter.getInstance();
        presenter.takeView(this);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
        playButtonIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.clickOnPlayButton();
            }
        });
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (barVisualizer != null)
            barVisualizer.release();
        presenter.dropView();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

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

    }

    @Override
    public void showLoading() {
        avi.show();
        trackNameTv.setVisibility(View.GONE);
    }

    @Override
    public void hideLoading() {
        avi.hide();
        trackNameTv.setVisibility(View.VISIBLE);

    }

    @Override
    public void setTrackInfo(String trackName) {
        trackNameTv.setText(trackName);
    }

    @Override
    public void setNextTrackInfo(String trackName){
        nextTrackNameTv.setText(trackName);
    }

    @Override
    public void initialVisualBar() {
        int audioSessionId = presenter.getMediaPlayer().getAudioSessionId();
        if (audioSessionId != -1) {
            barVisualizer.setAudioSessionId(audioSessionId);
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

}
