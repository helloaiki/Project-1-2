package com.example.serverm;

import javafx.scene.layout.VBox;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private ServerSocket serverSocket;
    private List<ClientArray> clientArrays=new CopyOnWriteArrayList<>();

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer(VBox vBox)
    {
        //start server will work on accepting clients who join and adding them to our list
        new Thread(()->
        {
            try {
                while (!serverSocket.isClosed())
                {
                    Socket socket=serverSocket.accept();
                    ClientArray clientArray=new ClientArray(socket,vBox,clientArrays);
                    clientArrays.add(clientArray);
                    new Thread(clientArray).start();
                }
            }catch (IOException e)
            {
                 e.printStackTrace();

            }

        }).start();
    }

    //this function is for saying which client left and joined etc

    public void broadCastToClients(String messageForAll)
    {
        for(ClientArray client:clientArrays)
        {
            client.sendMessageToClient(messageForAll);
        }
    }


}

