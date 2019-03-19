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

public class MusicHandler {

    private static MusicHandler instance;
    private Track lastTrack;
    private OldVersionTrack nextTrack;
    private final MediaPlayer firstPlayer = new MediaPlayer();
    private TrackLoader trackLoader;
    private MediaPlayer.OnCompletionListener playerCompleteListener = null;
    private String playingTrack;
    private ChangeStateTrackListener eventsMusicListener;
    private static STATE state = STATE.NOT_READY;
    private File lastFile;
    private File nextTrackFile;
    private PhoneStateListener phoneStateListener;


    public MusicHandler() {
        Utils.saveLog("MusicHandler constructor Thread name : " + Thread.currentThread().getName());
        trackLoader = new TrackLoader();
        playerCompleteListener = mp -> {
            Utils.saveLog("playerCompleteListener Thread name : " + Thread.currentThread().getName());
            state = STATE.NOT_READY;
            if (nextTrack == null) {
                Utils.saveLog("playerCompleteListener nextTrack == null : " + (nextTrack == null));
                if (Utils.getNetworkState()) {
                    Utils.saveLog("playerCompleteListener Utils.getNetworkState() : " + Utils.getNetworkState());
                    eventsMusicListener.showAttemptRestoreState();
                    trackLoader.loadNextTrackAfterRestoreNetwork();
                } else {
                    state = STATE.STATE_ERROR_LOAD_NEXT_TRACK;
                    eventsMusicListener.showNetworkError();
                }
                return;
            }
            try {
                Utils.saveLog("playerCompleteListener nextTrack == null : " + (nextTrack == null));

                playingTrack = nextTrack.getName();
                nextTrack = null;
                eventsMusicListener.updatePlayingTrack(playingTrack);
                eventsMusicListener.updateNextTrack(App.getContext().getString(R.string.buffering));
                if (firstPlayer.isPlaying()) {
                    firstPlayer.stop();
                }
                firstPlayer.reset();
                firstPlayer.setDataSource(nextTrackFile.getPath());
                firstPlayer.prepare();
                firstPlayer.start();
                state = STATE.PLAY;
                trackLoader.loadNextTrack();
            } catch (Exception e) {
                Utils.saveLog("playerCompleteListener Exception : " + e.getMessage());

                Utils.saveLog(e.getMessage());
            }
            firstPlayer.setOnCompletionListener(playerCompleteListener);
        };

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

    public static MusicHandler getInstance() {
        Utils.saveLog("MusicHandler getInstance");
        if (instance == null) {
            instance = new MusicHandler();
        }
        return instance;
    }

    public boolean init(ChangeStateTrackListener listener) {
        Utils.saveLog("MusicHandler init");

        this.eventsMusicListener = listener;
        return trackLoader.loadInfoAboutLastTrack();
    }

    public void loadAndPlayLastTrack() {
        Utils.saveLog("MusicHandler loadAndPlayLastTrack");

        RadioApi radioApi = DownloadTrackController.getApi();
        Call<ResponseBody> call = radioApi.apiDownloadTrack(lastTrack.getId());
        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utils.saveLog("MusicHandler loadAndPlayLastTrack onResponse");

                File tempFolder = new File(App.
                        getContext().
                        getApplicationInfo().
                        dataDir + ConstantStorage.TEMP_FOLDER);
                if (!tempFolder.exists()) {
                    tempFolder.mkdir();
                }
                lastFile = new File(App.
                        getContext().
                        getApplicationInfo().
                        dataDir + ConstantStorage.TEMP_FOLDER + "/lastTrack");
                try {
//                    lastFile.createNewFile();
                    Files.asByteSink(lastFile).write(response.body().bytes());
                    if (firstPlayer.isPlaying()) {
                        firstPlayer.stop();
                    }
                    playingTrack = lastTrack.getName();
                    eventsMusicListener.updatePlayingTrack(playingTrack);
                    firstPlayer.reset();
                    firstPlayer.setDataSource(lastFile.getPath());
                    firstPlayer.prepare();
                    firstPlayer.start();
                    state = STATE.PLAY;
                    firstPlayer.setOnCompletionListener(playerCompleteListener);
                    trackLoader.loadNextTrack();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utils.saveLog("MusicHandler loadAndPlayLastTrack onFailure " + t.getMessage());

            }
        });
    }

    public MediaPlayer getMediaPlayer() {
        return firstPlayer;
    }

    public String getPlayingTrack() {
        return playingTrack;
    }

    public void notifyAboutNetworkState() {
        Utils.saveLog("MusicHandler notifyAboutNetworkState");

        if (state == STATE.STATE_ERROR_LOAD_NEXT_TRACK) {
            Utils.simpleLog("nextTrack == null && Utils.getNetworkState()" + String.valueOf(nextTrack == null) + " " + Utils.getNetworkState());
            if (nextTrack == null && Utils.getNetworkState()) {
                trackLoader.loadNextTrackAfterRestoreNetwork();
            }
        }
    }

    public void playFuckingMusic() {
        firstPlayer.start();
    }

    public void stopSuckingMusic() {
        firstPlayer.pause();
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
                            loadAndPlayLastTrack();
                        }

                    }

                    @Override
                    public void onFailure(Call<Track[]> call, Throwable t) {
                        Utils.saveLog("TrackLoader loadInfoAboutLastTrack onFailure");

                        Utils.simpleLog("MusicHandler");
                        Utils.simpleLog(t.getMessage());
                    }
                });
                return true;
            }
            return false;
        }

        public void loadNextTrackAfterRestoreNetwork() {
            Utils.saveLog("TrackLoader loadNextTrackAfterRestoreNetwork");
            loadInfoAboutLastTrack();
//            RadioApi api = DownloadTrackController.getApi();
//            api.loadNextTracks().enqueue(new Callback<OldVersionTrack[]>() {
//                @Override
//                public void onResponse(Call<OldVersionTrack[]> call, Response<OldVersionTrack[]> response) {
//
//                    OldVersionTrack[] oldVersionTracks = response.body();
//                    nextTrack = oldVersionTracks[0];
//                    Utils.saveLog("TrackLoader loadNextTracks onResponse nextTrack " + nextTrack.getName());
//                    eventsMusicListener.updateNextTrack(nextTrack.getName());
//                    api.apiDownloadTrack(nextTrack.getId()).enqueue(new Callback<ResponseBody>() {
//                        @Override
//                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                            Utils.saveLog("TrackLoader apiDownloadTrack onResponse nextTrack " + nextTrack.getName());
//
//                            nextTrackFile = new File(App.
//                                    getContext().
//                                    getApplicationInfo().
//                                    dataDir + ConstantStorage.TEMP_FOLDER + "nextTrack" + nextTrack.getId());
//                            try {
////                                nextTrackFile.createNewFile();
//                                Files.asByteSink(nextTrackFile).write(response.body().bytes());
//
//                                playingTrack = nextTrack.getName();
//                                nextTrack = null;
//                                eventsMusicListener.updatePlayingTrack(playingTrack);
//                                firstPlayer.reset();
//                                firstPlayer.setDataSource(nextTrackFile.getPath());
//                                firstPlayer.prepare();
//                                firstPlayer.start();
//                                state = STATE.PLAY;
//                                firstPlayer.setOnCompletionListener(playerCompleteListener);
//                                trackLoader.loadNextTrack();
//
//                            } catch (Exception e) {
//                                Utils.saveLog("TrackLoader apiDownloadTrack onResponse " + e.getMessage());
//
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(Call<ResponseBody> call, Throwable t) {
//                            Utils.saveLog("TrackLoader loadNextTracks onFailure nextTrack " + nextTrack.getName());
//
//                            Utils.simpleLog("error  "  + t.getMessage());
//
//                        }
//                    });
//                }
//
//                @Override
//                public void onFailure(Call<OldVersionTrack[]> call, Throwable t) {
//                    Utils.saveLog("TrackLoader loadNextTracks onFailure " + t.getMessage());
//
//                }
//            });
        }

        public boolean loadNextTrack() {
            Utils.saveLog("TrackLoader loadNextTrack");

            if (Utils.getNetworkState()) {
                RadioApi api = DownloadTrackController.getApi();
                api.loadNextTracks().enqueue(new Callback<OldVersionTrack[]>() {
                    @Override
                    public void onResponse(Call<OldVersionTrack[]> call, Response<OldVersionTrack[]> response) {

                        OldVersionTrack[] oldVersionTracks = response.body();
                        nextTrack = oldVersionTracks[0];
                        Utils.saveLog("TrackLoader loadNextTrack onResponse nextTrack " + nextTrack.getName());

                        eventsMusicListener.updateNextTrack(nextTrack.getName());
                        api.apiDownloadTrack(nextTrack.getId()).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                Utils.saveLog("TrackLoader apiDownloadTrack onResponse nextTrack " + nextTrack.getName());

                                nextTrackFile = new File(App.
                                        getContext().
                                        getApplicationInfo().
                                        dataDir + ConstantStorage.TEMP_FOLDER + "nextTrack" + nextTrack.getId());
                                try {
//                                    nextTrackFile.createNewFile();
                                    Files.asByteSink(nextTrackFile).write(response.body().bytes());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Utils.saveLog("TrackLoader apiDownloadTrack onFailure nextTrack " + nextTrack.getName());

                                nextTrack = null;
                                state = STATE.STATE_ERROR_LOAD_NEXT_TRACK;
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<OldVersionTrack[]> call, Throwable t) {
                        Utils.saveLog("TrackLoader loadNextTracks onFailure");

                        nextTrack = null;
                        state = STATE.STATE_ERROR_LOAD_NEXT_TRACK;
                    }
                });
                return true;
            }
            return false;

        }
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
