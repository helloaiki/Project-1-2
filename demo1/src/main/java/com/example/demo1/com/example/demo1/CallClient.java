package com.example.demo1;

import com.example.serverm.VideoSender;
import javafx.scene.image.ImageView;

public class CallClient {
    private static Thread videoThread;
    private static VideoReceiver videoReceiver;

    public static void start(ImageView imageView){
       // new Thread(new VideoSender("localhost",5000)).start();
        videoReceiver = new VideoReceiver(5001,imageView);
        videoThread = new Thread(videoReceiver);
        videoThread.start();

    }

    public static void stop(){
        if(videoReceiver!=null){
            videoReceiver.stop();
            videoReceiver=null;
        }

        if(videoThread!=null && videoThread.isAlive()){
            try{
                videoThread.join(3000);
            } catch(InterruptedException e){
                e.printStackTrace();
            }
            videoThread=null;
        }
    }
}
