package info.javaway.sternradio.presenter;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;

import info.javaway.sternradio.App;
import info.javaway.sternradio.handler.MusicHandler;

public class RootPresenter {

    private MusicHandler musicHandler;
    private static RootPresenter instance;

    SimpleExoPlayer player;

    public static RootPresenter getInstance() {
        if (instance == null) {
            instance = new RootPresenter();
        }
        return instance;
    }

    public RootPresenter() {
        musicHandler = MusicHandler.getInstance();
        player = ExoPlayerFactory.newSimpleInstance(App.getContext());
    }

    public void clickOnPlay() {
        musicHandler.playLastTrack();
    }
}
