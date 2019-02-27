package info.javaway.sternradio.handler;

import android.media.MediaPlayer;

import com.google.common.io.Files;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Queue;

import info.javaway.sternradio.App;
import info.javaway.sternradio.Utils;
import info.javaway.sternradio.model.OldVersionTrack;
import info.javaway.sternradio.model.Track;
import info.javaway.sternradio.retrofit.DownloadTrackController;
import info.javaway.sternradio.retrofit.RadioApi;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MusicHandler {
    private static MusicHandler instance;
    private HashMap<String, Integer> allTracks;
    private Track lastTrack;
    private Track nextTrack;
    private Track nTrack;
    private MediaPlayer firstPlayer = new MediaPlayer();
    private MediaPlayer secondPlayer = new MediaPlayer();
    private TrackLoader trackLoader;
    private Queue<File> trackQueue = new ArrayDeque<>(2);
    private Queue<OldVersionTrack> oldVersionTracks = new ArrayDeque<>(2);
    private MediaPlayer.OnCompletionListener playerCompleteListener = null;


    public MusicHandler() {
        trackLoader = new TrackLoader();
        playerCompleteListener = mp -> {

            try {
                if (firstPlayer.isPlaying()) {
                    firstPlayer.stop();
                }
                firstPlayer.reset();
                firstPlayer.setDataSource(trackQueue.poll().getPath());
                firstPlayer.prepare();
                firstPlayer.start();
            } catch (Exception e){
                Utils.saveLog("Error of open file : MusicHandler");
                Utils.saveLog(e.getMessage());
            }
            firstPlayer.setOnCompletionListener(playerCompleteListener);
        };
    }

    public static MusicHandler getInstance() {
        if (instance == null) {
            instance = new MusicHandler();
        }
        return instance;
    }

    public void loadFirstTrack() {
        trackLoader.loadFirstTrack();
    }

    public void playLastTrack() {
        RadioApi radioApi = DownloadTrackController.getApi();
        Call<ResponseBody> call = radioApi.apiDownloadTrack(lastTrack.getId());
        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Thread t1 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        File file = new File(App.
                                getContext().
                                getApplicationInfo().
                                dataDir + "/lastTrack");
                        try {
                            file.createNewFile();
                            Files.asByteSink(file).write(response.body().bytes());
                            if (firstPlayer.isPlaying()) {
                                firstPlayer.stop();
                            }
                            firstPlayer.reset();
                            firstPlayer.setDataSource(file.getPath());
                            firstPlayer.prepare();
                            firstPlayer.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                t1.start();

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private class TrackLoader {

        public boolean loadFirstTrack() {
            if (Utils.getNetworkState()) {
                RadioApi api = DownloadTrackController.getApi();
                api.loadLastTracks().enqueue(new Callback<Track[]>() {
                    @Override
                    public void onResponse(Call<Track[]> call, Response<Track[]> response) {
                        Utils.simpleLog("MusicHandler");
                        Track[] tracks = response.body();
                        Utils.simpleLog(Arrays.toString(tracks));
                        if (tracks != null && tracks.length > 0) {
                            lastTrack = tracks[0];
                        }
                    }

                    @Override
                    public void onFailure(Call<Track[]> call, Throwable t) {
                        Utils.simpleLog("MusicHandler");
                        Utils.simpleLog(t.getMessage());
                    }
                });
                return true;
            }
            return false;
        }

        public boolean loadNextTracks() {
            if (Utils.getNetworkState()) {
                RadioApi api = DownloadTrackController.getApi();
                api.loadNextTracks().enqueue(new Callback<OldVersionTrack[]>() {
                    @Override
                    public void onResponse(Call<OldVersionTrack[]> call, Response<OldVersionTrack[]> response) {
                        OldVersionTrack[] oldVersionTracks = response.body();
                        for (OldVersionTrack track : oldVersionTracks){
                            MusicHandler.this.oldVersionTracks.add(track);
                        }
                    }

                    @Override
                    public void onFailure(Call<OldVersionTrack[]> call, Throwable t) {

                    }
                });
                return true;
            }
            return false;

        }
    }
}
