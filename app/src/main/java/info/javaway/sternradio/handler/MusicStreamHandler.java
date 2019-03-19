package info.javaway.sternradio.handler;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.google.common.io.Files;

import java.io.File;
import java.util.Arrays;

import info.javaway.sternradio.App;
import info.javaway.sternradio.R;
import info.javaway.sternradio.Utils;
import info.javaway.sternradio.model.OldVersionTrack;
import info.javaway.sternradio.model.Track;
import info.javaway.sternradio.retrofit.DownloadTrackController;
import info.javaway.sternradio.retrofit.RadioApi;
import info.javaway.sternradio.storage.ConstantStorage;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MusicStreamHandler {

    private String URL_STREAM = "https://a1.radioheart.ru:9011/RH6977";

    private static MusicStreamHandler instance;
//    private Track lastTrack;
    private final MediaPlayer firstPlayer = new MediaPlayer();
    private MediaPlayer.OnCompletionListener playerCompleteListener = null;
    private String playingTrack;
    private ChangeStateTrackListener eventsMusicListener;
    private static STATE state = STATE.NOT_READY;
    private File lastFile;
    private File nextTrackFile;
    private PhoneStateListener phoneStateListener;


    public MusicStreamHandler() {
        Utils.saveLog("MusicHandler constructor Thread name : " + Thread.currentThread().getName());

        if (Utils.getNetworkState()) {
            state = STATE.NOT_READY;
        } else {
            state = STATE.STATE_ERROR_LOAD_NEXT_TRACK;
        }

        setPhoneListener();
    }

    private void setPhoneListener() {
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    //Incoming call: Pause music
                    mute(true);
                } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                    //Not in call: Play music
                    mute(false);
                } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    //A call is dialing, active or on hold
                    mute(true);
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };
        TelephonyManager mgr = (TelephonyManager) App.get().getSystemService(Context.TELEPHONY_SERVICE);
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    public void mute(boolean b) {

        AudioManager am = (AudioManager) App.get().getSystemService(Context.AUDIO_SERVICE);
        if (b) {
            am.setStreamMute(AudioManager.STREAM_MUSIC, true);
        } else {
            am.setStreamMute(AudioManager.STREAM_MUSIC, false);
        }
    }

    public static MusicStreamHandler getInstance() {
        Utils.saveLog("MusicHandler getInstance");
        if (instance == null) {
            instance = new MusicStreamHandler();
        }
        return instance;
    }

    public boolean init(ChangeStateTrackListener listener) {
        Utils.saveLog("MusicHandler init");

        this.eventsMusicListener = listener;
        return false;
    }

    public MediaPlayer getMediaPlayer() {
        return firstPlayer;
    }

    public void notifyAboutNetworkState() {
        Utils.saveLog("MusicHandler notifyAboutNetworkState");

        if (state == STATE.STATE_ERROR_LOAD_NEXT_TRACK) {

        }
    }

    public void playFuckingMusic() {
        firstPlayer.start();
    }

    public void stopSuckingMusic() {
        firstPlayer.pause();
    }

    public boolean playerIsPause() {
        return firstPlayer.isPlaying();
    }

    public interface ChangeStateTrackListener {
        void updatePlayingTrack(String playingTrack);

        void showNetworkError();

        void showAttemptRestoreState();

        void updateNextTrack(String nameOfNextTrack);
    }

    public static STATE getState() {
        return state;
    }

    private void setState(STATE state) {
        this.state = state;
    }

    public enum STATE {
        READY,
        NOT_READY,
        LOAD_TRACK,
        PLAY,
        STATE_ERROR_LOAD_NEXT_TRACK
    }
}
