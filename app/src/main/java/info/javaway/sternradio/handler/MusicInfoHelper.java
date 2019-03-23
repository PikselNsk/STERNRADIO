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
import info.javaway.sternradio.Utils;
import info.javaway.sternradio.model.OldVersionTrack;
import info.javaway.sternradio.model.Track;
import info.javaway.sternradio.retrofit.DownloadTrackController;
import info.javaway.sternradio.retrofit.RadioApi;
import info.javaway.sternradio.service.MusicServiceStream;
import info.javaway.sternradio.storage.ConstantStorage;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MusicInfoHelper {


    private static MusicInfoHelper instance;
    private String playingTrack;
    private TrackLoader loader;
    private Track lastTrack;
    private OldVersionTrack nextTrack;
    private static ChangeStateTrackListener eventsMusicListener;


    public MusicInfoHelper() {
        Utils.saveLog("MusicHandler constructor Thread name : " + Thread.currentThread().getName());
        loader = new TrackLoader();
//        setPhoneListener();
    }

    private void setPhoneListener() {
        PhoneStateListener phoneStateListener = new PhoneStateListener() {
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

    public static MusicInfoHelper getInstance( ChangeStateTrackListener listener) {
        Utils.saveLog("MusicHandler getInstance");
        if (instance == null) {
            instance = new MusicInfoHelper();
        }
        eventsMusicListener = listener;
        return instance;
    }

    public void updateInfo() {
        loader.loadInfoAboutLastTrack();
        loader.loadNextTrackInfo();
    }

    private class TrackLoader {

        public boolean loadInfoAboutLastTrack() {
            Utils.saveLog("TrackLoader loadInfoAboutLastTrack");

            if (Utils.getNetworkState()) {
                RadioApi api = DownloadTrackController.getApi();
                api.loadLastTracks().enqueue(new Callback<Track[]>() {
                    @Override
                    public void onResponse(Call<Track[]> call, Response<Track[]> response) {
                        Utils.saveLog("TrackLoader loadInfoAboutLastTrack onResponse");

                        Utils.simpleLog("MusicHandler");
                        Track[] tracks = response.body();
                        Utils.simpleLog(Arrays.toString(tracks));
                        if (tracks != null && tracks.length > 0) {
                            lastTrack = tracks[0];
                            eventsMusicListener.updatePlayingTrack(lastTrack.getName());
                        }
                    }

                    @Override
                    public void onFailure(Call<Track[]> call, Throwable t) {

                    }
                });
                return true;
            }
            return false;
        }


        public void loadNextTrackInfo() {

                RadioApi api = DownloadTrackController.getApi();
                api.loadNextTracks().enqueue(new Callback<OldVersionTrack[]>() {
                    @Override
                    public void onResponse(Call<OldVersionTrack[]> call, Response<OldVersionTrack[]> response) {
                        OldVersionTrack[] oldVersionTracks = response.body();
                        nextTrack = oldVersionTracks[0];
                        eventsMusicListener.updateNextTrack(nextTrack.getName());
                    }

                    @Override
                    public void onFailure(Call<OldVersionTrack[]> call, Throwable t) {
                        Utils.saveLog("TrackLoader loadNextTracks onFailure");
                        nextTrack = null;
                    }
                });
            }


    }
    public interface ChangeStateTrackListener {
        void updatePlayingTrack(String playingTrack);

        void showNetworkError();

        void showAttemptRestoreState();

        void updateNextTrack(String nameOfNextTrack);
    }

}
