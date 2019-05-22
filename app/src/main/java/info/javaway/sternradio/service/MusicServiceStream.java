package info.javaway.sternradio.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import info.javaway.sternradio.App;
import info.javaway.sternradio.Utils;
import info.javaway.sternradio.handler.MusicInfoHelper;
import info.javaway.sternradio.view.RootActivity;

/**
 * Service that controls the media player and the notification that represents it.
 */
public class MusicServiceStream extends Service
        implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnCompletionListener {

    private static final String TAG = MusicServiceStream.class.getSimpleName();

    private final IBinder mMediaPlayerBinder = new MediaPlayerBinder();
    public static final String ACTION_PLAY = "info.javaway.sternradio.PLAY";
    public static final String ACTION_PAUSE = "info.javaway.sternradio.PAUSE";
    public static final String ACTION_PAUSE_CANCEL = "info.javaway.sternradio.ACTION_PAUSE_CANCEL";
    public static final String ACTION_CLOSE = "info.javaway.sternradio.APP_CLOSE";
    public static final String ACTION_CLOSE_IF_PAUSED = "info.javaway.sternradio.services.APP_CLOSE_IF_PAUSED";
    private static final int NOTIFICATION_ID = 4223;
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private PhoneStateReceiver phoneStateReceiver = new PhoneStateReceiver();
    private AudioManager mAudioManager = null;

    private static final String mStreamUrl = "https://a1.radioheart.ru:9011/RH6977";

    //Wifi Lock to ensure the wifi does not ge to sleep while we are stearming music.
//    private WifiManager.WifiLock mWifiLock;
    private ArrayList<MusicInfoHelper.ChangeStateTrackListener> changeStateTrackListeners = new ArrayList<>();
    private String currentTrackName = "not implementation";
    private State mState = State.Stopped;
    private AudioFocus mAudioFocus = AudioFocus.NoFocusNoDuck;


    public boolean playerIsPlay() {
        return mState == State.Playeng;
    }

    public String getPlayingTrack() {
        return currentTrackName;
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public void initPlayer() {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
//        Utils.simpleLog(TAG + " onError " + what);
//        startMediaPlayer();
        Utils.saveLog("Class: " + "MusicServiceStream " + "Method: " + "onError " + what + " " + extra);
        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        Utils.saveLog("Class: " + "MusicServiceStream " + "Method: " + "onBufferingUpdate");
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        Utils.saveLog("Class: " + "MusicServiceStream " + "Method: " + "onInfo " + what + " " + extra);
        return false;
    }


    private void setupAudioManager() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
    }

    private void setupMediaPlayer() {

        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(mStreamUrl);
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }

    }

    private void setPhoneListener() {
        PhoneStateListener phoneStateListener = new PhoneStateListener() {

            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                Utils.simpleLog("Class: " + "MusicServiceStream " + "Method: " + "onCallStateChanged CALL_STATE_RINGING");

                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    Utils.simpleLog("Class: " + "MusicServiceStream " + "Method: " + "onCallStateChanged CALL_STATE_RINGING");
                    //Incoming call: Pause music
                    mute(true);
                } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                    Utils.simpleLog("Class: " + "MusicServiceStream " + "Method: " + "onCallStateChanged CALL_STATE_IDLE");
                    //Not in call: Play music
                    mute(false);
                } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    Utils.simpleLog("Class: " + "MusicServiceStream " + "Method: " + "onCallStateChanged CALL_STATE_OFFHOOK");
                    //A call is dialing, active or on hold
                    mute(true);
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter("android.intent.action.PHONE_STATE");
        registerReceiver(phoneStateReceiver, intentFilter);
        TelephonyManager mgr = (TelephonyManager) App.get().getSystemService(Context.TELEPHONY_SERVICE);
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    /**
     * The radio streaming service runs in foreground mode to keep the Android OS from killing it.
     * The OnStartCommand is called every time there is a call to start service and the service is
     * already started. By Passing an intent to the onStartCommand we can play and pause the music.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.simpleLog("Class: " + "MusicServiceStream " + "Method: " + "onStartCommand " + intent.getAction());
        String action = null;
        if (intent != null) {
            action = intent.getAction();
        }
        if (action != null) {
            switch (action) {
                case ACTION_PLAY:
                    processPlayRequest();
                    break;
                case ACTION_PAUSE:
                    processPauseRequest();
                    break;
                case ACTION_CLOSE_IF_PAUSED:
                    closeIfPaused();
                    break;
                case ACTION_CLOSE:
                    close();
                    break;

                case ACTION_PAUSE_CANCEL:
                    if (mMediaPlayer.isPlaying()) {
                        mute(false);
                        mState = State.Playeng;
                    }
            }
        }
        setPhoneListener();
        return START_NOT_STICKY; //do not restart service if it is killed.
    }


    //if the media player is paused or stopped and this method has been triggered then stop the service.
    private void closeIfPaused() {
        if (mState == State.Paused || mState == State.Stopped) {
            close();
        }
    }

    public void close() {
        stopMediaPlayer();
        removeNotification();
        stopSelf();
        System.exit(0);
    }

    private void initMediaPlayer() {
        setupMediaPlayer();
        requestResources();
    }

    /**
     * Check if the media player was initialized and we have audio focus.
     * Without audio focus we do not start the media player.
     * change state and start to prepare async
     */
    private void configAndPrepareMediaPlayer() {
        initMediaPlayer();
        mState = State.Preparing;

        // TODO: 23.03.2019 отправить нотификацию
        NotificationHelper.sendNotificationToManagment(
                App.getContext(),
                "",
                0,
                null,
                null,
                null,
                mState == MusicServiceStream.State.Paused || mState == MusicServiceStream.State.Stopped
        );
        mMediaPlayer.prepareAsync();

    }

    /**
     * The media player is prepared check to make sure we are not in the stopped or paused states
     * before starting the media player
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mState != State.Paused && mState != State.Stopped) {
            startMediaPlayer();
        }
    }

    /*
        Check if the media player is available and start it.
     */
    private void startMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
            sendUpdatePlayerIntent();
            mState = State.Playeng;
            NotificationHelper.sendNotificationToManagment(
                    App.getContext(),
                    "",
                    0,
                    null,
                    null,
                    null,
                    false
            );
        }
    }

    private void sendUpdatePlayerIntent() {
        Utils.simpleLog("Class: " + "MusicServiceStream " + "Method: " + "sendUpdatePlayerIntent");
        Intent updatePlayerIntent = new Intent(RootActivity.UPDATE_PLAYER);
        LocalBroadcastManager.getInstance(this).sendBroadcast(updatePlayerIntent);
    }

    /*
        Request audio focus and aquire a wifi lock. Returns true if audio focus was granted.
     */
    private void requestResources() {
        setupAudioManager();
    }


    /**
     * if the Media player is playing then stop it. Change the state and relax the wifi lock and
     * audio focus.
     */
    private void stopMediaPlayer() {
        Utils.simpleLog("Class: " + "MusicServiceStream " + "Method: " + "stopMediaPlayer");
        // Lost focus for an unbounded amount of time: stop playback and release media player
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mState = State.Stopped;
        //relax the resources because we no longer need them.
        relaxResources();
//        giveUpAudioFocus();
    }

    private void processPlayRequest() {
        if (mState == State.Stopped) {
            sendBufferingIntent();
            configAndPrepareMediaPlayer();
        } else if (mState == State.Paused) {
            requestResources();
            startMediaPlayer();
        }
    }

    //send an intent telling any activity listening to this intent that the media player is buffering.
    private void sendBufferingIntent() {
        Utils.simpleLog("Class: " + "MusicServiceStream " + "Method: " + "sendBufferingIntent");
        Intent bufferingPlayerIntent = new Intent(RootActivity.BUFFERING);
        LocalBroadcastManager.getInstance(this).sendBroadcast(bufferingPlayerIntent);
    }

    private void processPauseRequest() {

        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mute(true);
//            sendUpdatePlayerIntent();
            mState = State.Paused;
//            relaxResources();
            NotificationHelper.sendNotificationToManagment(
                    App.getContext(),
                    "",
                    0,
                    null,
                    null,
                    null,
                    mState == MusicServiceStream.State.Paused || mState == MusicServiceStream.State.Stopped
            );
        }
    }

    /**
     * There is no media style notification for operating systems below api 21. So This method builds
     * a simple compat notification that has a play or pause button depending on if the player is
     * paused or played. if foreGroundOrUpdate then the service should go to the foreground. else
     * just update the notification.
     */

    private PendingIntent getMainContentIntent() {
        Intent resultIntent = new Intent(this, RootActivity.class);
        return PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    @Override
    public IBinder onBind(Intent intent) {
        Utils.simpleLog("Class: " + "MusicServiceStream " + "Method: " + "onBind");
        return mMediaPlayerBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Utils.simpleLog("Class: " + "MusicServiceStream " + "Method: " + "onUnbind");
        return false;
    }

    @Override
    public void onDestroy() {
        Utils.simpleLog("Class: " + "MusicServiceStream " + "Method: " + "onDestroy");
        stopMediaPlayer();
        NotificationHelper.clearNotification();

    }

    //give up wifi lock if it is held and stop the service from being a foreground service.
    private void relaxResources() {

        //Release the WifiLock resource
//        if (mWifiLock != null && mWifiLock.isHeld()) {
//            mWifiLock.release();
//        }


        // stop service from being a foreground service. Passing true removes the notification as well.
        stopForeground(true);

    }

    private void removeNotification() {
        NotificationHelper.clearNotification();
    }

//    private void giveUpAudioFocus() {
//        if ((mAudioFocus == AudioFocus.Focused || mAudioFocus == AudioFocus.NoFocusCanDuck) &&
//                AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager.abandonAudioFocus(this)) {
//            mAudioFocus = AudioFocus.NoFocusNoDuck;
//        }
//    }

    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    public void restoreNetwork() {
        if (!Utils.getNetworkState()) {
            for (MusicInfoHelper.ChangeStateTrackListener listener : changeStateTrackListeners) {
                listener.showNetworkError();
            }
            mState = State.Retrieving;
        } else {
            if (mState == State.Retrieving) {
                for (MusicInfoHelper.ChangeStateTrackListener listener : changeStateTrackListeners) {
                    listener.showAttemptRestoreState();
                }
                Utils.simpleLog("Class: " + "MusicServiceStream " + "Method: " + "restoreNetwork");
                mMediaPlayer.stop();
                mMediaPlayer.reset();
                configAndPrepareMediaPlayer();
            }
        }

    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        Utils.simpleLog(TAG + " onCompletion");
        mState = State.Retrieving;
        restoreNetwork();
    }

    public void mute(boolean mute) {
        AudioManager am = (AudioManager) App.get().getSystemService(Context.AUDIO_SERVICE);

        if (mute) {
            mState = State.Paused;
            mMediaPlayer.setVolume(0f, 0f);
            am.setStreamMute(AudioManager.STREAM_MUSIC, true);
        } else {
            mState = State.Playeng;
            mMediaPlayer.setVolume(1f, 1f);

            am.setStreamMute(AudioManager.STREAM_MUSIC, false);
        }
    }

    public class MediaPlayerBinder extends Binder {

        public MusicServiceStream getService() {
            return MusicServiceStream.this;
        }
    }

    public void registerChangeTrackListener(MusicInfoHelper.ChangeStateTrackListener listener) {
        changeStateTrackListeners.add(listener);
    }

    public void unregisterChangeTrackListener(MusicInfoHelper.ChangeStateTrackListener listener) {
        changeStateTrackListeners.remove(listener);
    }

    enum State {
        Retrieving, // the MediaRetriever is retrieving music
        Stopped,  //Media player is stopped and not prepared to play
        Preparing, // Media player is preparing to play
        Playeng,  // MediaPlayer playback is active.
        // There is a chance that the MP is actually paused here if we do not have audio focus.
        // We stay in this state so we know to resume when we gain audio focus again.
        Paused // Audio Playback is paused
    }

    enum AudioFocus {
        NoFocusNoDuck, // service does not have audio focus and cannot duck
        NoFocusCanDuck, // we don't have focus but we can play at low volume ("ducking")
        Focused  // media player has full audio focus
    }

    public class PhoneStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
                String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);    // outgoing call
                Log.i(TAG, "call OUT:" + phoneNumber);
            } else {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);

                switch (tm.getCallState()) {
                    case TelephonyManager.CALL_STATE_RINGING:  // incoming call
                        Utils.simpleLog("Class: " + "MusicServiceStream " + "Method: " + "onCallStateChanged CALL_STATE_RINGING");
                        //Incoming call: Pause music
                        mute(true);
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        Utils.simpleLog("Class: " + "MusicServiceStream " + "Method: " + "onCallStateChanged CALL_STATE_OFFHOOK");
                        //A call is dialing, active or on hold
                        mute(true);
                        break;

                    case TelephonyManager.CALL_STATE_IDLE:
                        Utils.simpleLog("Class: " + "MusicServiceStream " + "Method: " + "onCallStateChanged CALL_STATE_IDLE");
                        //Not in call: Play music
                        mute(false);
                        break;
                }

            }
        }
    }
}