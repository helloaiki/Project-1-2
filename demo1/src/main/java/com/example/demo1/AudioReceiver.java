package com.example.demo1;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class AudioReceiver implements Runnable {
    private final int port;
    private boolean running = true;

    public AudioReceiver(int p) {
        port = p;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        try {
            AudioFormat format = new AudioFormat(44100.0f, 16, 1, true, false);
            SourceDataLine speakers = AudioSystem.getSourceDataLine(format);
            speakers.open(format);
            speakers.start();

            DatagramSocket socket = new DatagramSocket(port);
            System.out.println("Listening on port: "+port);
            byte[] buffer = new byte[4096];

            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, 0, buffer.length);
                socket.receive(packet);
                System.out.println("Received "+packet.getLength()+" bytes from "+packet.getAddress());
                speakers.write(packet.getData(), 0, packet.getLength());
                System.out.println("Playing "+packet.getLength()+" bytes");
            }

            speakers.drain();
            speakers.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

