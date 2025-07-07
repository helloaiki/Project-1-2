//DON'T NEED IT ANYMORE

package com.example.serverm;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import javafx.scene.image.ImageView;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class VideoReceiver implements Runnable{
    private final int port;
    private final ImageView imageView;

    public VideoReceiver(int p, ImageView i){
        port=p;
        imageView=i;
    }

    private volatile boolean running= true;

    public void stop(){
        running = false;
    }

    @Override
    public void run (){
        try(ServerSocket serverSocket = new ServerSocket(port)){
            Socket socket= serverSocket.accept();
            InputStream input= socket.getInputStream();

            while(running){
                int ln1= input.read();
                int ln2= input.read();
                int ln3= input.read();
                int ln4= input.read();
                if(ln1==-1 || ln2==-1 || ln3==-1 || ln4==-1) break;
                int len=(ln1<<24) | (ln2<<16) | (ln3<<8) | ln4;


                byte[] data = input.readNBytes(len);

                BufferedImage bufferedImage= ImageIO.read(new java.io.ByteArrayInputStream(data));
                if(bufferedImage!=null){
                    Image image= SwingFXUtils.toFXImage(bufferedImage,null);
                    Platform.runLater(()->imageView.setImage(image));
                }
            }

        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
