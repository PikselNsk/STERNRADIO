package info.javaway.sternradio.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import java.util.ArrayList;

import info.javaway.sternradio.Utils;
import info.javaway.sternradio.handler.MusicHandler;

public class MusicService extends Service implements MusicHandler.ChangeStateTrackListener {

    private final IBinder musicBinder = new MusicBinder();
    MusicHandler musicHandler;
    ArrayList<MusicHandler.ChangeStateTrackListener> changeStateTrackListeners = new ArrayList<>();

    public IBinder onBind(Intent arg0) {
        Utils.simpleLog("MusicService onBind");
        return musicBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.simpleLog("MusicService onCreate");
        musicHandler = MusicHandler.getInstance();
    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    public IBinder onUnBind(Intent arg0) {
        // TO DO Auto-generated method
        Utils.simpleLog("MusicService onUnBind");
        return null;
    }

    public void onStop() {

    }
    public void onPause() {

    }

    public void initPlayer(){
        Utils.simpleLog("MusicService initPlayer");
        musicHandler.init(this);
    }

    public void registerChangeTrackListener(MusicHandler.ChangeStateTrackListener listener){
        changeStateTrackListeners.add(listener);
    }

    public void unregisterChangeTrackListener(MusicHandler.ChangeStateTrackListener listener){
        changeStateTrackListeners.remove(listener);
    }

    @Override
    public void onDestroy() {
        Utils.simpleLog("MusicService onDestroy");
        Utils.clearTempFolder();
    }

    @Override
    public void onLowMemory() {
        Utils.simpleLog("MusicService onLowMemory");

    }

    @Override
    public void updatePlayingTrack(String playingTrack) {
        for (MusicHandler.ChangeStateTrackListener listener: changeStateTrackListeners){
            listener.updatePlayingTrack(playingTrack);
        }
    }

    public boolean playerIsPlay(){
        return musicHandler.playerIsPause();
    }

    @Override
    public void showNetworkError() {

    }

    @Override
    public void showAttemptRestoreState() {

    }

    @Override
    public void updateNextTrack(String nameOfNextTrack) {
        for (MusicHandler.ChangeStateTrackListener listener: changeStateTrackListeners){
            listener.updateNextTrack(nameOfNextTrack);
        }
    }

    public void playLastTrack() {
        musicHandler.loadAndPlayLastTrack();
    }

    public String getPlayingTrack() {
        return musicHandler.getPlayingTrack();
    }

    public MediaPlayer getMediaPlayer() {
        return musicHandler.getMediaPlayer();
    }

    public MusicHandler.STATE getState() {
        return MusicHandler.getState();
    }

    public void playFuckingMusic() {
        musicHandler.playFuckingMusic();
    }

    public void stopFuckingMusic() {
        musicHandler.stopSuckingMusic();
    }


    public class MusicBinder extends Binder{
        public MusicService getService(){
            return MusicService.this;
        }

    }
}