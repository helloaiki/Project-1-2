package com.example.demo1;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class AudioSender implements Runnable{
    private final String serverIP;
    private final int port;
    private boolean running = true;

    public AudioSender(String ip,int p){
        serverIP=ip;
        port=p;
    }

    public void stop(){
        running=false;
    }

    @Override
    public void run(){
        try{
            AudioFormat format= new AudioFormat(44100.0f, 16, 1, true, false);
            TargetDataLine microphone = AudioSystem.getTargetDataLine(format);
            microphone.open(format);
            microphone.start();

            DatagramSocket socket = new DatagramSocket();
            InetAddress address= InetAddress.getByName(serverIP);

            byte[] buffer = new byte[4096];
            while(running){
                int count = microphone.read(buffer, 0, buffer.length);
                if(count>0){
                    DatagramPacket packet = new DatagramPacket(buffer,count,address,port);
                    socket.send(packet);
                }
            }

            microphone.stop();
            microphone.close();
            socket.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
