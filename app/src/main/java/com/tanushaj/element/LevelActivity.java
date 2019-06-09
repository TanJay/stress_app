package com.tanushaj.element;

import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.arges.sepan.argmusicplayer.Enums.AudioType;
import com.arges.sepan.argmusicplayer.IndependentClasses.ArgAudio;
import com.arges.sepan.argmusicplayer.PlayerViews.ArgPlayerSmallView;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class LevelActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnPreparedListener {

    MediaPlayer mp;
    Button playPauseBtn;
    private int playBackPosition = 0;
    TextView playDurationTxt;
    TextView totalDurationTimeTxt;
        Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_level);
        mp = new MediaPlayer();
        handler = new Handler();

        playPauseBtn = findViewById(R.id.playOrPauseBtn);
        playDurationTxt = findViewById(R.id.playDurationTxt);
        totalDurationTimeTxt = findViewById(R.id.totalDurationTimeTxt);
        playPauseBtn.setOnClickListener(this);
//        smallView
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.playOrPauseBtn:
                playOrPause();
                break;
        }
    }

    private void playOrPause() {
        if(mp.isPlaying()) {
            playBackPosition = mp.getCurrentPosition();
            mp.pause();
            playPauseBtn.setBackground(getDrawable(R.drawable.play_button));
        }else{
            if(playBackPosition != 0){
                mp.seekTo(playBackPosition);
                mp.start();
            }else {
                try {
                    mp.setDataSource("https://sample-videos.com/audio/mp3/crowd-cheering.mp3");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mp.setOnPreparedListener(this);
                mp.prepareAsync();
//                mp.start();
            }
            playPauseBtn.setBackground(getDrawable(R.drawable.pause_button));
        }
        mp.setOnCompletionListener(this);
        mp.setOnSeekCompleteListener(this);

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playBackPosition = 0;
        playPauseBtn.setBackground(getDrawable(R.drawable.play_button));
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        Log.d("TAGGGG", "Here");
    }

    @Override
    protected void onDestroy() {
        killMediaPlayer();
        super.onDestroy();
    }

    private void killMediaPlayer() {
        if(mp!=null) {
            try {
                mp.release();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPrepared(final MediaPlayer mp) {
        mp.start();
        totalDurationTimeTxt.setText(String.format("%s Min", String.valueOf(TimeUnit.MILLISECONDS.toMinutes(mp.getDuration()))));
        Runnable notification = new Runnable() {

            public void run() {
                playDurationTxt.setText(String.format("%s Sec", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(mp.getCurrentPosition()))));

            }

        };

        handler.postDelayed(notification, 300);
    }
}
