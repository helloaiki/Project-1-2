package com.example.serverm;

import javafx.scene.layout.VBox;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientArray implements Runnable{
    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private List<ClientArray> clientArray;
    private VBox vBox;
    private String userName;
    public ClientArray(Socket socket, VBox vBox,List<ClientArray>clientArray) {
        try
        {
            this.socket=socket;
            this.bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.clientArray=clientArray;
            this.vBox=vBox;

            String userNamePassWordCombo=bufferedReader.readLine();
            String[]credentials=userNamePassWordCombo.split(" ");
            userName=credentials[0];
            if(!validUser(credentials[0],credentials[1]))
            {
                 bufferedWriter.write("Unsuccessful login");
                 bufferedWriter.newLine();
                 bufferedWriter.flush();
                 closeEverything(socket,bufferedReader,bufferedWriter);
                 return;
            }
            bufferedWriter.write("Successful login");
            bufferedWriter.newLine();
            bufferedWriter.flush();

        }
        catch(IOException e)
        {
            System.out.println("Error creating server");
            e.printStackTrace();
            closeEverything(socket,bufferedReader,bufferedWriter);
        }
    }

    private boolean validUser(String name,String password)
    {
        File file=new File("clients.txt");
        try
        {
            if(!file.exists())
            {
                giveEntryToNewUser(name,password);
                return true;
            }
            BufferedReader br=new BufferedReader(new FileReader(file));
            String matcher;
            while((matcher=br.readLine())!=null)
            {
                if(matcher.equals(name+" "+password))
                {
                    br.close();
                    return true;
                }
            }

            giveEntryToNewUser(name,password);
            return true;

        }
        catch(IOException e)
        {
            System.out.println("Error in client reg part");
            e.printStackTrace();
            return false;
        }



    }

    private void giveEntryToNewUser(String name,String password)
    {
       try
       {
           FileWriter fw=new FileWriter("clients.txt",true);
           fw.write(name+" "+password+"\n");
           fw.close();
       } catch (IOException e) {
           System.out.println("Error in appending new client to file");
           e.printStackTrace();
       }
    }

    //sends messages to server via bufferWriter
    //run waits for message and displays likewise
    public void sendMessageToClient(String messageToClient)
    {
        try
        {
            bufferedWriter.write(messageToClient);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            System.out.println("Error sending message to client");
            closeEverything(socket,bufferedReader,bufferedWriter);
        }
    }

    //closing particular client's everything if error happens
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
            clientArray.remove(this);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    //listening for messages: blocking operation
    //run will help deal with that
    //adds message we sent to our interface and uses vBox to transmit that to others
    @Override
    public void run()
    {
       try
       {
           String message;
           while((message=bufferedReader.readLine())!=null)
           {
               String messageForAllExceptMe=message;
               saveMessages(messageForAllExceptMe);
               javafx.application.Platform.runLater(() -> {
                   HelloController.addLabel(messageForAllExceptMe, vBox);
               });
               for (ClientArray client : clientArray) {
                   if (client != this)
                   {
                       client.sendMessageToClient(messageForAllExceptMe);
                   }
               }
           }
       }
       catch(IOException e)
       {
           System.out.println("Error in client receiving and sending message");
           e.printStackTrace();
           closeEverything(socket,bufferedReader,bufferedWriter);
       }

    }

    public void saveMessages(String message)
    {
        try(FileWriter fw=new FileWriter("chat_history.txt",true);BufferedWriter bufferedWriter=new BufferedWriter(fw);PrintWriter pw=new PrintWriter(bufferedWriter);)
        {
            pw.println(message);
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
