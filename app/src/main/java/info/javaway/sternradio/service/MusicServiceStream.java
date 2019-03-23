package info.javaway.sternradio.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SubtitleData;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import info.javaway.sternradio.R;
import info.javaway.sternradio.Utils;
import info.javaway.sternradio.handler.MusicHandler;
import info.javaway.sternradio.handler.MusicStreamHandler;
import info.javaway.sternradio.view.RootActivity;

/**
 * Service that controls the media player and the notification that represents it.
 */
public class MusicServiceStream extends Service
        implements
        AudioManager.OnAudioFocusChangeListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnSubtitleDataListener, MediaPlayer.OnCompletionListener {

    private static final String TAG = MusicServiceStream.class.getSimpleName();

    private final IBinder mMediaPlayerBinder = new MediaPlayerBinder();
    public static final String ACTION_PLAY = "info.javaway.sternradio.PLAY";
    public static final String ACTION_PAUSE = "info.javaway.sternradio.PAUSE";
    private static final String ACTION_CLOSE = "info.javaway.sternradio.APP_CLOSE";
    public static final String ACTION_CLOSE_IF_PAUSED = "info.javaway.sternradio.services.APP_CLOSE_IF_PAUSED";
    private static final int NOTIFICATION_ID = 4223;
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private AudioManager mAudioManager = null;

    private static final String mStreamUrl = "https://a1.radioheart.ru:9011/RH6977";

    //Wifi Lock to ensure the wifi does not ge to sleep while we are stearming music.
//    private WifiManager.WifiLock mWifiLock;
    private ArrayList<MusicStreamHandler.ChangeStateTrackListener> changeStateTrackListeners = new ArrayList<>();
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
    public void onAudioFocusChange(int focusChange) {
        Utils.simpleLog(TAG + " onAudioFocusChange " + focusChange);
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                mAudioFocus = AudioFocus.Focused;
                // resume playback
                if (mState == State.Playeng) {
                    startMediaPlayer();
                    mMediaPlayer.setVolume(1.0f, 1.0f);
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                mAudioFocus = AudioFocus.NoFocusNoDuck;
                // Lost focus for an unbounded amount of time: stop playback and release media player

                stopMediaPlayer();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                mAudioFocus = AudioFocus.NoFocusNoDuck;
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                processPauseRequest();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                mAudioFocus = AudioFocus.NoFocusCanDuck;
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
//        Utils.simpleLog(TAG + " onError " + what);
//        startMediaPlayer();
        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        Utils.simpleLog(TAG + " onBufferingUpdate");
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        Utils.simpleLog(TAG + " onInfo " + extra);

        return false;
    }


    private void setupAudioManager() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
    }

    private void setupWifiLock() {
//        if (mWifiLock == null) {
//            mWifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE))
//                    .createWifiLock(WifiManager.WIFI_MODE_FULL, "mediaplayerlock");
//        }
    }

    private void setupMediaPlayer() {

        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnSubtitleDataListener(this);
        mMediaPlayer.setOnCompletionListener(this);
//            mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(mStreamUrl);
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }

    }

    /**
     * The radio streaming service runs in foreground mode to keep the Android OS from killing it.
     * The OnStartCommand is called every time there is a call to start service and the service is
     * already started. By Passing an intent to the onStartCommand we can play and pause the music.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.simpleLog(TAG + " " + " onStartCommand");
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
            }
        }
        return START_STICKY; //do not restart service if it is killed.
    }

    //if the media player is paused or stopped and this method has been triggered then stop the service.
    private void closeIfPaused() {
        if (mState == State.Paused || mState == State.Stopped) {
            close();
        }
    }

    private void close() {
        removeNotification();
        stopSelf();
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
//        buildNotification(true);
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
//            buildNotification(false);
            new AsyncHeaderParcer().execute();
        }
    }

    private void sendUpdatePlayerIntent() {
        Log.d(TAG, "updatePlayerIntent");
        Intent updatePlayerIntent = new Intent(RootActivity.UPDATE_PLAYER);
        LocalBroadcastManager.getInstance(this).sendBroadcast(updatePlayerIntent);
    }

    /*
        Request audio focus and aquire a wifi lock. Returns true if audio focus was granted.
     */
    private void requestResources() {
        setupAudioManager();
        setupWifiLock();
//        mWifiLock.acquire();

        tryToGetAudioFocus();
    }

    private void tryToGetAudioFocus() {
        if (mAudioFocus != AudioFocus.Focused && AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN))
            mAudioFocus = AudioFocus.Focused;

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
        giveUpAudioFocus();
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
        Utils.simpleLog(TAG + " sendBufferingIntent");
        Intent bufferingPlayerIntent = new Intent(RootActivity.BUFFERING);
        LocalBroadcastManager.getInstance(this).sendBroadcast(bufferingPlayerIntent);
    }

    private void processPauseRequest() {

        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            sendUpdatePlayerIntent();
            mState = State.Paused;
            relaxResources();
//            buildNotification(false);
        }
    }

    /**
     * There is no media style notification for operating systems below api 21. So This method builds
     * a simple compat notification that has a play or pause button depending on if the player is
     * paused or played. if foreGroundOrUpdate then the service should go to the foreground. else
     * just update the notification.
     */
    private void buildNotification(boolean startForeground) {
        Intent intent = new Intent(getApplicationContext(), MusicServiceStream.class);
        intent.setAction(ACTION_CLOSE);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("KZFR Radio").setContentText("Streaming Live")
                .setSmallIcon(R.mipmap.ic_launcher).setOngoing(true)
                .setContentIntent(getMainContentIntent())
                .setDeleteIntent(pendingIntent);
        if (mState == State.Paused || mState == State.Stopped) {
            builder.addAction(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY));
        } else {
            builder.addAction(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
        }
        builder.addAction(generateAction(android.R.drawable.ic_menu_close_clear_cancel, "Close", ACTION_CLOSE));

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        if (startForeground)
            startForeground(NOTIFICATION_ID, builder.build());
        else
            notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
    }

    private PendingIntent getMainContentIntent() {
        Intent resultIntent = new Intent(this, RootActivity.class);
        return PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), MusicServiceStream.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMediaPlayerBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onDestroy() {
        stopMediaPlayer();
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
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());

        notificationManagerCompat.cancel(NOTIFICATION_ID);
    }

    private void giveUpAudioFocus() {
        if ((mAudioFocus == AudioFocus.Focused || mAudioFocus == AudioFocus.NoFocusCanDuck) &&
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager.abandonAudioFocus(this)) {
            mAudioFocus = AudioFocus.NoFocusNoDuck;
        }
    }

    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    public void restoreNetwork() {
        Utils.simpleLog("Class: " + "MusicServiceStream " + "Method: " + "restoreNetwork");
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        configAndPrepareMediaPlayer();
    }

    @Override
    public void onSubtitleData(@NonNull MediaPlayer mp, @NonNull SubtitleData data) {
        Utils.simpleLog("Class: " + "MusicServiceStream " + "Method: " + "onSubtitleData");
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Utils.simpleLog(TAG + " onCompletion");
    }

    public class MediaPlayerBinder extends Binder {

        public MusicServiceStream getService() {
            return MusicServiceStream.this;
        }
    }

    public void registerChangeTrackListener(MusicStreamHandler.ChangeStateTrackListener listener) {
        changeStateTrackListeners.add(listener);
    }

    public void unregisterChangeTrackListener(MusicStreamHandler.ChangeStateTrackListener listener) {
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

    public class AsyncHeaderParcer extends AsyncTask<Void, Void, Void> {

        private ParsingHeaderData.TrackData trackData;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL(
                        mStreamUrl);
                ParsingHeaderData streaming = new ParsingHeaderData();
                trackData = streaming.getTrackDetails(url);
                Log.e("Song Artist Name ", trackData.artist);
                Log.e("Song Artist Title", trackData.title);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            for (MusicStreamHandler.ChangeStateTrackListener listener : changeStateTrackListeners){
                listener.updatePlayingTrack(trackData.artist + " " + trackData.title);
            }
        }
    }
}