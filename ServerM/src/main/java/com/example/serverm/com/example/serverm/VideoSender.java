package com.example.serverm;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;
import org.bytedeco.opencv.global.opencv_videoio;

import java.io.OutputStream;
import java.net.Socket;

public class VideoSender implements Runnable{
    private final String host;
    private final int port;

    public VideoSender(String h, int p){
        host=h;
        port=p;
    }

    @Override
    public void run(){
        VideoCapture camera= new VideoCapture(0, opencv_videoio.CAP_DSHOW);
        if(!camera.isOpened()){
            System.out.println("Cannot open webcam");
            return;
        }
        Mat frame= new Mat();

        try(Socket socket = new Socket(host,port);
            OutputStream output = socket.getOutputStream()){

            while(camera.read(frame)){
                BytePointer buf = new BytePointer();
                opencv_imgcodecs.imencode(".jpg",frame,buf);
                byte[] bytes = new byte[(int)buf.limit()];
                buf.get(bytes);

                output.write((bytes.length >> 24) & 0xFF);
                output.write((bytes.length>>16) & 0xFF);
                output.write((bytes.length>>8) & 0xFF);
                output.write(bytes.length & 0xFF);
                output.write(bytes);
                output.flush();

                Thread.sleep(33);
            }
        } catch(Exception e){
            e.printStackTrace();
        } finally{
            camera.release();
        }

    }
}
