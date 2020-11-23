package com.example.so.vibrationcoeur;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.util.Random;

public class Home extends AppCompatActivity {

    Context context = this;

    private final Random mRandom = new Random();
    private final Handler mHandler = new Handler();

    private MediaPlayer mediaPlayer;
    private MediaPlayer firstBeatPlayer;
    private MediaPlayer secondBeatPlayer;

    private boolean keepPlaying = false;
    private boolean firstBeat = true;
    private boolean soundBeginning = true;
    private boolean backToNormal = false;
    private boolean dieSlowly = false;
    private boolean wakeUpSlowly = true;

    private int delayMillis;
    private int currentDelay = 1000;
    private int consecutiveBeats = 0;
    private int consecutiveExtreme = 1;
    private boolean frequencyIncreasing = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);

        Button button = findViewById(R.id.heartButton);

        button.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    soundBeginning = false;
                    keepPlaying = true;
                    firstBeat = true;
                    wakeUpSlowly = true;

                    currentDelay = 1200;
                }

                if(event.getAction() == MotionEvent.ACTION_UP){
                    dieSlowly = true;
                }

                return false;
            }
        });

        firstBeatPlayer = MediaPlayer.create(this, R.raw.beat1);
        secondBeatPlayer = MediaPlayer.create(this, R.raw.beat2);

        mediaPlayer = firstBeatPlayer;

        playMySound();
    }

    private void playMySound() {
        // Randomly change frequency
        int frequencyToAdd;

        if (keepPlaying) {
            if (firstBeat) {
                mediaPlayer = firstBeatPlayer;
                delayMillis = currentDelay;

                if (soundBeginning) {
                    System.out.println("First time, setup");

                    frequencyIncreasing = (mRandom.nextInt(1) > 0);
                    frequencyToAdd = frequencyIncreasing ? 40 : -40;

                    currentDelay = 800;
                    consecutiveBeats = 0;
                    consecutiveExtreme = 1;

                    soundBeginning = false;
                }
                else if (wakeUpSlowly) {
                    System.out.println("Wake up slowly");
                    frequencyToAdd = 160;

                    if (currentDelay >= 960) {
                        wakeUpSlowly = false;
                        soundBeginning = true;
                    }
                }
                else if (dieSlowly) {
                    System.out.println("Die slowly");
                    frequencyToAdd = 160;

                    if (currentDelay >= 1400) {
                        keepPlaying = false;
                        dieSlowly = false;
                    }
                }
                else {
                    if (backToNormal) {
                        System.out.println("Back to normal, frequency : " + currentDelay);

                        if (currentDelay == 800) {
                            int randomChanger = mRandom.nextInt(1);

                            System.out.println("Compare randomChanger (" + randomChanger + ") to consecutiveExtreme (" + consecutiveExtreme + ")");

                            if (consecutiveExtreme >= randomChanger) {
                                // 50% to change the first time, always change the second time
                                frequencyIncreasing = !frequencyIncreasing;
                                consecutiveExtreme = 1;

                                System.out.println("Revert");
                            }
                            else {
                                System.out.println("Continue");
                                consecutiveExtreme++;
                            }

                            consecutiveBeats = 0;
                            backToNormal = false;

                            frequencyToAdd = frequencyIncreasing ? -40 : -40;
                        }
                        else {
                            System.out.println("Go back to 1000");
                            frequencyToAdd = currentDelay < 800 ? 40 : -40;
                        }
                    }
                    else {
                        int randomChanger = mRandom.nextInt(20);

                        System.out.println("Compare randomChanger (" + randomChanger + ") to consecutiveBeats (" + consecutiveBeats + ")");

                        if (consecutiveBeats > randomChanger) {
                            System.out.println("Change extreme, back to normal");

                            backToNormal = true;
                            frequencyToAdd = currentDelay < 800 ? 40 : -40;
                        }
                        else {
                            System.out.println("Continue into the extreme");
                            frequencyToAdd = frequencyIncreasing ? 40 : -40;
                        }
                    }
                }

                consecutiveBeats++;
                currentDelay += frequencyToAdd;

                System.out.println("Frequency : " + currentDelay + " - Consecutives beats : " + consecutiveBeats + " - Consecutives extreme : " + consecutiveExtreme);
            }
            else {
                mediaPlayer = secondBeatPlayer;
                delayMillis = currentDelay / 2;
            }
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    // Check if the Activity is finishing.
                    return;
                }

                if (keepPlaying) {
                    mediaPlayer.start();
                    firstBeat = !firstBeat;
                }

                playMySound();
            }
        }, delayMillis);
    }
}
