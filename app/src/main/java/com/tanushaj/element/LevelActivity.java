package com.tanushaj.element;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.arges.sepan.argmusicplayer.Enums.AudioType;
import com.arges.sepan.argmusicplayer.IndependentClasses.ArgAudio;
import com.arges.sepan.argmusicplayer.PlayerViews.ArgPlayerSmallView;

public class LevelActivity extends AppCompatActivity implements View.OnClickListener {

    Button playPauseBtn;
    ArgPlayerSmallView smallView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_level);

        playPauseBtn = findViewById(R.id.playOrPauseBtn);
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
        ArgAudio audio1 = new ArgAudio("Singer1","Audio1","https://sample-videos.com/audio/mp3/crowd-cheering.mp3",AudioType.URL);
//        audio1.
        smallView = findViewById(R.id.argmusicplayer);
        smallView.play(audio1);
    }
}
