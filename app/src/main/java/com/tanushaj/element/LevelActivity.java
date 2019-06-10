package com.tanushaj.element;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.robinhood.spark.SparkView;
import com.tanushaj.element.models.HRVDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import angtrim.com.fivestarslibrary.FiveStarsDialog;
import angtrim.com.fivestarslibrary.NegativeReviewListener;
import angtrim.com.fivestarslibrary.ReviewListener;

public class LevelActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnPreparedListener, NegativeReviewListener, ReviewListener {

    MediaPlayer mp;
    Button playPauseBtn;
    Button cancelSessionBtn;
    private int playBackPosition = 0;
    TextView playDurationTxt;
    TextView totalDurationTimeTxt;
    SparkView heartRateView;
    List<HRVDto> hrvDtos;
        FiveStarsDialog fiveStarsDialog;
        DoubleBounce doubleBounce;
//    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_level);
        mp = new MediaPlayer();
        fiveStarsDialog = new FiveStarsDialog(this,"tanushajayasinghe@gmail.com");
        mp.setOnPreparedListener(this);
        mp.setOnCompletionListener(this);
        mp.setOnSeekCompleteListener(this);

        hrvDtos = new ArrayList<>();
//        handler = new Handler();

        playPauseBtn = findViewById(R.id.playOrPauseBtn);
        doubleBounce = new DoubleBounce();
        doubleBounce.setBounds(0, 0, 260, 200);
        doubleBounce.setColor(getResources().getColor(R.color.colorAccent));
        playPauseBtn.setCompoundDrawables(doubleBounce, null, null, null);

        cancelSessionBtn = findViewById(R.id.cancel_session);
        playDurationTxt = findViewById(R.id.playDurationTxt);
        totalDurationTimeTxt = findViewById(R.id.totalDurationTimeTxt);
        heartRateView = findViewById(R.id.heartrateview);
        playPauseBtn.setOnClickListener(this);
        cancelSessionBtn.setOnClickListener(this);
        heartRateView.setAdapter(new HeartRateAdapter(hrvDtos));
        LocalBroadcastManager.getInstance(getApplication()).registerReceiver(heartRateListner, new IntentFilter("hrm_info_event"));

//        smallView
    }


    private BroadcastReceiver heartRateListner = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            String[] hrv_date = message.split(",");
            HRVDto hrvDto = new HRVDto(hrv_date[0], Integer.valueOf(hrv_date[1]), Float.parseFloat(hrv_date[2]));
            hrvDtos.add(hrvDto);
            List<HRVDto> selectedVisual = (hrvDtos.size() > 1000) ? hrvDtos.subList(hrvDtos.size()-1000, hrvDtos.size()) : hrvDtos;
            heartRateView.setAdapter(new HeartRateAdapter(selectedVisual));
//            synchronized(heartRateView){
//                heartRateView.notify();
//            }
//            heartRateView.notify();
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.playOrPauseBtn:
                playOrPause();
                break;
            case R.id.cancel_session:
                endSession();
                break;
        }
    }

    private void endSession() {
        fiveStarsDialog.setRateText("Help us serve you better in recommending")
                .setTitle("Please Rate")
                .setForceMode(false)
                .setUpperBound(2) // Market opened if a rating >= 2 is selected
                .setNegativeReviewListener(this) // OVERRIDE mail intent for negative review
                .setReviewListener(this) // Used to listen for reviews (if you want to track them )
                .showAfter(0);
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
                playPauseBtn.setBackground(getDrawable(R.drawable.pause_button));
            }else {
                playPauseBtn.setBackground(getDrawable(R.drawable.circle));
                doubleBounce.start();
                try {
                    mp.setDataSource("https://sample-videos.com/audio/mp3/crowd-cheering.mp3");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("TAGGGGG", e.getLocalizedMessage());
                }
                mp.prepareAsync();
                playPauseBtn.setEnabled(false);
//                mp.start();
            }
//            playPauseBtn.setBackground(getDrawable(R.drawable.pause_button));
        }


    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playBackPosition = 0;
        playPauseBtn.setBackground(getDrawable(R.drawable.play_button));
        endSession();
//        mp.release();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        Log.d("TAGGGG", "Here");
    }

    @Override
    protected void onDestroy() {
        killMediaPlayer();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(heartRateListner);
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
        doubleBounce.stop();
        playPauseBtn.setEnabled(true);
        playPauseBtn.setBackground(getDrawable(R.drawable.pause_button));
        totalDurationTimeTxt.setText(String.format("%s Min", String.valueOf(TimeUnit.MILLISECONDS.toMinutes(mp.getDuration()))));
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try{
                    playDurationTxt.setText(String.format("%s Sec", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(mp.getCurrentPosition()))));
                }
                catch (Exception e) {
                    // TODO: handle exception
                }
                finally{
                    //also call the same runnable to call it at regular interval
                    handler.postDelayed(this, 300);
                }
            }
        };

        handler.post(runnable);

    }

    @Override
    public void onNegativeReview(int i) {
        finish();
    }

    @Override
    public void onReview(int i) {
        finish();
    }
}
