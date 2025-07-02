package com.example.serverm;

import javafx.scene.image.ImageView;

public class CallServer {
    public static void start(ImageView imageView){
        new Thread(new VideoSender("localhost",5001)).start();

        //new Thread(new VideoReceiver(5000,imageView)).start();
    }
}
