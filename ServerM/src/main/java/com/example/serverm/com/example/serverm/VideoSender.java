//DON'T NEED IT ANYMORE

package com.example.serverm;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;
import org.bytedeco.opencv.global.opencv_videoio;

import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class VideoSender implements Runnable{
    private final String host;
    private final int port;
    VideoCapture camera= new VideoCapture(0, opencv_videoio.CAP_DSHOW);
    public VideoSender(String h, int p){
        host=h;
        port=p;
    }

    private volatile boolean running= true;

    public void stop(){
        running=false;
        camera.release();
    }

    @Override
    public void run(){

        System.out.println("VideoSender started");
        if(!camera.isOpened()){
            System.out.println("Cannot open webcam");
            return;
        }
        System.out.println("Webcam opened successfully");
        Mat frame= new Mat();
        List<Socket> sockets= new ArrayList<>();
        List<OutputStream> outputs = new ArrayList<>();

        try(Socket socket = new Socket(host,port);
            OutputStream output = socket.getOutputStream()){

            while(running && camera.read(frame)){
                BytePointer buf = new BytePointer();
                opencv_imgcodecs.imencode(".jpg",frame,buf);
                byte[] bytes = new byte[(int)buf.limit()];
                buf.get(bytes);

                try {
                    output.write((bytes.length >> 24) & 0xFF);
                    output.write((bytes.length >> 16) & 0xFF);
                    output.write((bytes.length >> 8) & 0xFF);
                    output.write(bytes.length & 0xFF);
                    output.write(bytes);
                    output.flush();
                }catch(Exception e) {
                    System.out.println("Client disconnected. Stopping video sender.");
                    break;
                }
                Thread.sleep(33);
            }
        } catch(Exception e){
            e.printStackTrace();
        } finally{
            camera.release();
            System.out.println("Camera released");

        }

    }
}
