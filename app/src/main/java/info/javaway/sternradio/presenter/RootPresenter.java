package info.javaway.sternradio.presenter;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.IBinder;

import info.javaway.sternradio.App;
import info.javaway.sternradio.R;
import info.javaway.sternradio.Utils;
import info.javaway.sternradio.handler.MusicHandler;
import info.javaway.sternradio.handler.MusicStreamHandler;
import info.javaway.sternradio.service.MusicService;
import info.javaway.sternradio.service.MusicServiceStream;

public class RootPresenter implements MusicHandler.ChangeStateTrackListener, ServiceConnection, MusicStreamHandler.ChangeStateTrackListener {

    private static RootPresenter instance;

    private View view;
    private MusicServiceStream musicService;
    private boolean mBound;
    private String nameOfNextTrack;

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
                        musicService.getPlayingTrack());
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
        String trackInfo = String.format(
                App.getContext().getString(R.string.actual_track_info),
                playingTrack);
        view.setTrackInfo(trackInfo);
        view.hideLoading();
    }

    @Override
    public void showNetworkError() {

    }

    @Override
    public void showAttemptRestoreState() {

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
        if (!musicService.playerIsPlay()){
//            musicService.playFuckingMusic();
            view.showPlayButton();
        } else {
//            musicService.stopFuckingMusic();
            view.showPauseButton();
        }
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
    }

}
