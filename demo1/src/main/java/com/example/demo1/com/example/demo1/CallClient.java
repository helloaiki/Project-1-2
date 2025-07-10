package com.example.demo1;

import javafx.scene.image.ImageView;

public class CallClient {
    private static Thread videoThreadSender, videoThreadReceiver;
    private static VideoReceiver videoReceiver;
    private static VideoSender videoSender;

    public static void start(ImageView imageView, String remoteIP, int remotePort, int myPort){
        videoSender= new VideoSender(remoteIP,remotePort);
        videoReceiver = new VideoReceiver(myPort,imageView);

        videoThreadSender = new Thread(videoSender);
        videoThreadReceiver= new Thread(videoReceiver);

        videoThreadSender.start();
        videoThreadReceiver.start();

    }

    public static void stop(){
        if(videoSender!=null){
            videoSender.stop();
            videoSender=null;
        }

        if(videoReceiver!=null){
            videoReceiver.stop();
            videoReceiver=null;
        }

        if(videoThreadSender!=null && videoThreadSender.isAlive()){
            try{
                videoThreadSender.join(3000);
            } catch(InterruptedException e){
                e.printStackTrace();
            }
            videoThreadSender=null;
        }

        if(videoThreadReceiver!=null && videoThreadReceiver.isAlive()){
            try{
                videoThreadReceiver.join(3000);
            } catch (InterruptedException e){
                e.printStackTrace();
            }

            videoThreadReceiver=null;
        }
    }
}

