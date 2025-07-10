package com.example.demo1;

import javafx.scene.media.AudioClip;

public class RingtonePlayer {
    private AudioClip ringtone;

    public RingtonePlayer(){
        ringtone = new AudioClip(getClass().getResource("/sounds/samsung_galaxy.wav").toString());
    }

    public void play(){
        ringtone.setCycleCount(AudioClip.INDEFINITE);
        ringtone.play();
    }

    public void stop(){
        ringtone.stop();
    }
}
