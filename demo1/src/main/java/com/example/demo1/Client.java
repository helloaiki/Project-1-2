package com.example.demo1;

import javafx.scene.layout.VBox;

import java.io.*;
import java.net.Socket;

public class Client {
    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;


    public Client(Socket socket,String name,String password) {
        try{
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedWriter.write(name+" "+password);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            String response=bufferedReader.readLine();
            if(response.equals("Unsuccessful login"))
            {
                System.out.println(response);
                closeEverything(socket,bufferedReader,bufferedWriter);
                javafx.application.Platform.runLater(() -> {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.ERROR,
                            "Login failed. Invalid username or password."
                    );
                    alert.showAndWait();
                });
            }
            else
            {
                System.out.println("Login successful");
            }

        }
        catch(IOException e)
        {
            e.printStackTrace();
            System.out.println("Error creating client");
            closeEverything(socket,bufferedReader,bufferedWriter);
        }
    }
    public void sendMessageToServer(String messageToServer)
    {
        try
        {
            bufferedWriter.write(messageToServer);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            System.out.println("Error sending message to Server");
            closeEverything(socket,bufferedReader,bufferedWriter);
        }
    }
    public  void receiveMessageFromServer(VBox vBox)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(socket.isConnected())
                {

                    try
                    {
                        String messageFromClient=bufferedReader.readLine();
                        HelloController.addLabel(messageFromClient,vBox);
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                        System.out.println("Error receiving message from client");
                        closeEverything(socket,bufferedReader,bufferedWriter);
                        break;
                    }

                }
            }
        }).start();
    }

    public void closeEverything(Socket socket,BufferedReader bufferedReader,BufferedWriter bufferedWriter)
    {
        try
        {
            if(bufferedReader!=null)
                bufferedReader.close();
            if(bufferedWriter!=null)
                bufferedWriter.close();
            if(socket!=null)
                socket.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
