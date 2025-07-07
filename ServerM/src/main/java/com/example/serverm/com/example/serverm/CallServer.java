//DON'T NEED IT ANYMORE

package com.example.serverm;

import javafx.scene.image.ImageView;

public class CallServer {
    private static Thread videoThread;
    private static VideoSender videoSender;

    public static void start(ImageView imageView){
        videoSender = new VideoSender("localhost", 5001);
        videoThread= new Thread(videoSender);
        videoThread.start();

        //new Thread(new VideoReceiver(5000,imageView)).start();
    }

    public static void stop(){
        if(videoSender!= null){
            videoSender.stop();
            videoSender=null;
        }

        if(videoThread!=null && videoThread.isAlive()){
            try{
                videoThread.join(3000);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            videoThread=null;
        }
    }
}
