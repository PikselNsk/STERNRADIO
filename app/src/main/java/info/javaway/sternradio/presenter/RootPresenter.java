package info.javaway.sternradio.presenter;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;

import info.javaway.sternradio.App;
import info.javaway.sternradio.R;
import info.javaway.sternradio.Utils;
import info.javaway.sternradio.handler.MusicHandler;
import info.javaway.sternradio.handler.MusicInfoHelper;
import info.javaway.sternradio.service.MusicServiceStream;
import info.javaway.sternradio.service.NotificationHelper;

public class RootPresenter implements  ServiceConnection, MusicInfoHelper.ChangeStateTrackListener {

    private static RootPresenter instance;

    private View view;
    private MusicServiceStream musicService;
    private boolean mBound;
    private String nameOfNextTrack;
    private TrackInfoUpdater trackInfoUpdater;
    private String currentTrackInfo;

    public static RootPresenter getInstance() {
        if (instance == null) {
            instance = new RootPresenter();
        }
        return instance;
    }

    public RootPresenter() {
        Intent service = new Intent(App.getContext(), MusicServiceStream.class);
        service.setAction(MusicServiceStream.ACTION_PLAY);
        App.getContext().startService(service);
        App.getContext().bindService(
                service,
                this,
                Service.BIND_AUTO_CREATE);
        trackInfoUpdater = new TrackInfoUpdater("TrackInfoUpdater");
        trackInfoUpdater.start();
    }

    public void takeView(View view) {
        this.view = view;
        if(musicService == null) {
            if (Utils.getNetworkState()) {
                view.setTrackInfo("Буферизация...");
            } else {
                view.showError(App.getContext().getString(R.string.network_error));
            }
        } else {
            if (musicService.playerIsPlay()){
                view.showPlayButton();
            } else {
                view.showPauseButton();
            }
            if (Utils.getNetworkState()) {
                String trackInfo = String.format(
                        App.getContext().getString(R.string.actual_track_info),
                        currentTrackInfo);
                view.setTrackInfo(trackInfo);

                trackInfo = String.format(
                        App.getContext().getString(R.string.next_track_info),
                        nameOfNextTrack);
                view.setNextTrackInfo(trackInfo);
            } else {
                view.showError(App.getContext().getString(R.string.network_error));
            }

            view.initialVisualBar();

            if (musicService.isPlaying()){
                view.hideLoading();
            }
        }


    }

    public void dropView() {
        Utils.clearAppCompatActivity();
        view = null;
    }

    public MediaPlayer getMediaPlayer() {
        return musicService.getMediaPlayer();
    }

    @Override
    public void updatePlayingTrack(String playingTrack) {

        currentTrackInfo = playingTrack;

        String infoAboutTrack = String.format(
                App.getContext().getString(R.string.actual_track_info),
                playingTrack);
        view.setTrackInfo(infoAboutTrack);
    }

    @Override
    public void showNetworkError() {
        view.showLoading();
        view.setNextTrackInfo("");
        view.setTrackInfo(App.getContext().getString(R.string.network_error));
    }

    @Override
    public void showAttemptRestoreState() {
        view.showLoading();
        view.setNextTrackInfo("");
        view.setTrackInfo(App.getContext().getString(R.string.buffering));
    }

    @Override
    public void updateNextTrack(String nameOfNextTrack) {
        this.nameOfNextTrack = nameOfNextTrack;
        String trackInfo = String.format(
                App.getContext().getString(R.string.next_track_info),
                nameOfNextTrack);
        view.setNextTrackInfo(trackInfo);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicServiceStream.MediaPlayerBinder binder = (MusicServiceStream.MediaPlayerBinder) service;
        musicService = binder.getService();
        musicService.registerChangeTrackListener(this);
        musicService.initPlayer();
        view.initialVisualBar();
        if (Utils.getNetworkState()) {

        } else {
            view.showError(App.getContext().getString(R.string.network_error));
        }
        mBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBound = false;
    }

    public void clickOnPlayButton() {
        Utils.simpleLog("Class: " + "RootPresenter " + "Method: " + "clickOnPlayButton");
        boolean isPause = musicService.playerIsPlay();
        if (!musicService.playerIsPlay()){
            musicService.mute(false);
            view.showPlayButton();
        } else {
            musicService.mute(true);
            view.showPauseButton();
        }
        NotificationHelper.sendNotification(App.getContext(),
                "",
                0,
                null,
                null,
                null,
                isPause);
    }

    public void restoreNetwork() {
        if (musicService!= null) {
            musicService.restoreNetwork();
        }
    }

    public interface View {
        void showError(String messageError);

        void showMessage(String message);

        void showLoading();

        void hideLoading();

        void setTrackInfo(String trackName);

        void setNextTrackInfo(String trackName);

        void initialVisualBar();

        void showPlayButton();

        void showPauseButton();
    }

    public void release() {
        musicService.unregisterChangeTrackListener(this);
        trackInfoUpdater.interrupt();
    }

    public class TrackInfoUpdater extends Thread {

        public TrackInfoUpdater(String name) {
            super(name);
        }

        @Override
        public void run() {
            MusicInfoHelper musicInfoHelper = MusicInfoHelper.getInstance(RootPresenter.this);
            while (true) {
                if (Utils.getNetworkState()) {
                    SystemClock.sleep(3000);
                    musicInfoHelper.updateInfo();
                }
            }
        }
    }

}
