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

import java.util.Calendar;

import info.javaway.sternradio.App;
import info.javaway.sternradio.R;
import info.javaway.sternradio.Utils;
import info.javaway.sternradio.handler.MusicHandler;
import info.javaway.sternradio.handler.MusicInfoHelper;
import info.javaway.sternradio.handler.PrefManager;
import info.javaway.sternradio.handler.SignalManager;
import info.javaway.sternradio.model.Alarm;
import info.javaway.sternradio.service.MusicServiceStream;
import info.javaway.sternradio.service.NotificationHelper;

public class RootPresenter implements ServiceConnection, MusicInfoHelper.ChangeStateTrackListener {

    private static RootPresenter instance;

    private View view;
    private MusicServiceStream musicService;
    private boolean mBound;
    private String nameOfNextTrack;
    private TrackInfoUpdater trackInfoUpdater;
    private String currentTrackInfo;
    private boolean isPause;
    private Alarm alarm;

    public static RootPresenter getInstance() {
        Utils.saveLog("Class: " + "RootPresenter " + "Method: " + "getInstance " + (instance == null));
        if (instance == null) {
            instance = new RootPresenter();
        }
        return instance;
    }

    public RootPresenter() {
        Utils.saveLog("Class: " + "RootPresenter " + "Method: " + "RootPresenter");
        Intent service = new Intent(App.getContext(), MusicServiceStream.class);
        service.setAction(MusicServiceStream.ACTION_PLAY);
        App.getContext().startService(service);
        App.getContext().bindService(
                service,
                this,
                Service.BIND_AUTO_CREATE);
        trackInfoUpdater = new TrackInfoUpdater("TrackInfoUpdater");
        trackInfoUpdater.start();
        alarm = Alarm.getInstance();
    }

    public void takeView(View view) {
        this.view = view;
        if (musicService == null) {
            if (Utils.getNetworkState()) {
                view.setTrackInfo("Буферизация...");
            } else {
                view.showError(App.getContext().getString(R.string.network_error));
            }
        } else {
            if (musicService.playerIsPlay()) {
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

            if (musicService.isPlaying()) {
                view.hideLoading();
            }
        }
        if (PrefManager.isCheckedAlarm()) {
            view.showDescribeAlarm(App.getContext().getString(R.string.alarm_clock) + " " +
                    Utils.getStringAlarm(App.getContext(), alarm, false));
        } else {
            view.showDescribeAlarm(App.getContext().getString(R.string.alarm_clock));
        }
    }

    public void dropView() {
        Utils.simpleLog("Class: " + "RootPresenter " + "Method: " + "dropView");
        Utils.clearAppCompatActivity();
        NotificationHelper.clearNotification();
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
        if (view != null) {
            view.setTrackInfo(infoAboutTrack);
        }
    }

    @Override
    public void showNetworkError() {
        Utils.saveLog("Class: " + "RootPresenter " + "Method: " + "showNetworkError");
        view.showLoading();
        view.setNextTrackInfo("");
        view.setTrackInfo(App.getContext().getString(R.string.network_error));
    }

    @Override
    public void showAttemptRestoreState() {
        Utils.saveLog("Class: " + "RootPresenter " + "Method: " + "showAttemptRestoreState");
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
        if (view != null) {
            view.setNextTrackInfo(trackInfo);
        }
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
        Utils.saveLog("Class: " + "RootPresenter " + "Method: " + "clickOnPlayButton" +
                " Utils.getNetworkState() " + Utils.getNetworkState());
        if (!Utils.getNetworkState()) return;
        isPause = musicService.playerIsPlay();
        if (!musicService.playerIsPlay()) {
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
        if (musicService != null) {
            musicService.restoreNetwork();
        }
    }

    public void onResume() {
        NotificationHelper.sendNotification(
                App.getContext(),
                "",
                0,
                null,
                null,
                null,
                isPause);
    }

    public void switchAlarm(boolean isChecked) {
        PrefManager.setCheckedAlarm(isChecked);
        if (isChecked) {
            SignalManager.getInstance(App.getContext()).setAlarm(alarm);
            view.showDescribeAlarm(App.getContext().getString(R.string.alarm_clock) + " " +
                    Utils.getStringAlarm(App.getContext(), alarm, false));
            view.showMessage(Utils.getDeltaTimeBeforeRing(alarm));

        } else {
            SignalManager.getInstance(App.getContext()).cancelAlarm();
            view.showDescribeAlarm(App.getContext().getString(R.string.alarm_clock));
            view.showMessage(App.getContext().getString(R.string.alarm_off));

        }
    }

    public void settingAlarm() {
        cancelOrContinue();
    }

    private boolean cancelOrContinue() {
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

        void showDescribeAlarm(String text);

    }

    public void release() {
        Utils.saveLog("Class: " + "RootPresenter " + "Method: " + "release");
        instance = null;
        musicService.unregisterChangeTrackListener(this);
        App.getContext().unbindService(this);
        musicService.close();
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
                if (isInterrupted()) return;
                if (Utils.getNetworkState()) {
                    SystemClock.sleep(3000);
                    musicInfoHelper.updateInfo();
                }
            }
        }
    }

}
