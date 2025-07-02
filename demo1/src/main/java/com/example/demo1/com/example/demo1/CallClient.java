package com.example.demo1;

import javafx.scene.image.ImageView;

public class CallClient {
    public static void start(ImageView imageView){
       // new Thread(new VideoSender("localhost",5000)).start();

        new Thread(new VideoReceiver(5001, imageView)).start();
    }
}
