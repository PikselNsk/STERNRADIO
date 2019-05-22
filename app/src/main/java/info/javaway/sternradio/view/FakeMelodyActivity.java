package info.javaway.sternradio.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

import info.javaway.sternradio.App;
import info.javaway.sternradio.R;
import info.javaway.sternradio.Utils;
import info.javaway.sternradio.model.Alarm;

public class FakeMelodyActivity extends AppCompatActivity {

    private MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_melody);
        Alarm alarm = Alarm.getInstance();
        ((TextView) findViewById(R.id.alarm_text)).setText(
                Utils.getStringAlarm(
                        App.getContext(),
                        alarm,
                        false)
        );

        findViewById(R.id.cancel_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_ALARM);

        try {
            player.setDataSource(this, Utils.resourceToUri(this, R.raw.ring));
            player.setLooping(true);
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.stop();
        player.release();
        ExitActivity.exitApplication(getApplicationContext());
    }
}
