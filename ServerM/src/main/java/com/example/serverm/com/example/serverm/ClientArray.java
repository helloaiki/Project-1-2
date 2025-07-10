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
            if(checkForDuplicateUser(credentials[0],credentials[1]))
            {
                bufferedWriter.write("Duplicate username");
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
            else
            {
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
                //here we will send the file text by text to the client as login was a success
                sendClientAvailable();
            }

        }
        catch(IOException e)
        {
            System.out.println("Error creating server");
            e.printStackTrace();
            closeEverything(socket,bufferedReader,bufferedWriter);
        }
    }

    private boolean checkForDuplicateUser(String name,String pw)
    {
        File file=new File("clients.txt");
        try
        {
            if(!file.exists())
            {
                //if file doesn't exist yet, it returns false,so that the call goes to validUser
                return false;
            }
            BufferedReader br=new BufferedReader(new FileReader(file));
            String matcher;
            //checks for a previous user
            while((matcher=br.readLine())!=null)
            {
                String namePartFromClientFile=matcher.split(" ")[0];
                String passwordPart=matcher.split(" ")[1];
                if(namePartFromClientFile.equalsIgnoreCase(name) && !passwordPart.equals(pw))
                {
                    return true;
                }
            }
        }
        catch(IOException e)
        {
            System.out.println("Error in client reg part");
            e.printStackTrace();
            return false;
        }
        //if it ever reaches this point, then that username is new or user used proper credentials
        return false;
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

    private void sendClientAvailable()
    {
        try
        {
            File file=new File("clients.txt");
            if(!file.exists())
                return;
            BufferedReader br=new BufferedReader(new FileReader(file));
            String linePicker;
            bufferedWriter.write("USER_INFORMATION_S");
            bufferedWriter.newLine();
            while((linePicker=br.readLine())!=null)
            {
                String name=linePicker.split(" ")[0];
                bufferedWriter.write(name);
                bufferedWriter.newLine();
            }
            bufferedWriter.write("USER_INFORMATION_E");
            bufferedWriter.newLine();
            bufferedWriter.flush();
            br.close();
        }
        catch(IOException e)
        {
            System.out.println("Error in sending across client list info");
            e.printStackTrace();
            closeEverything(socket,bufferedReader,bufferedWriter);
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

               if(message.startsWith("_START_VIDEO_|")){
                   System.out.println("Broadcasting start video command to clients.");
                   for(ClientArray client: clientArray){
                       if(client!=this){
                           client.sendMessageToClient(message);
                       }
                   }
                   continue;
               }

               if(message.startsWith("_END_VIDEO_|")){
                   System.out.println("Broadcasting end video command to clients.");
                   for(ClientArray client: clientArray){
                       if(client!=this){
                           client.sendMessageToClient(message);
                       }
                   }

                   for(ClientArray client:clientArray){
                           client.sendMessageToClient("END_AUDIO");
                   }
                   continue;
               }

               if(message.startsWith("_VIDEO_CALL_REQUEST_|")){
                   for(ClientArray client:clientArray){
                       if(client!=this){
                           client.sendMessageToClient(message);
                       }
                   }
                   continue;
               }

               if(message.startsWith("_REJECT_VIDEO_CALL_|")){
                   for(ClientArray client:clientArray){
                       if(client!=this){
                           client.sendMessageToClient(message);
                       }
                   }
                   continue;
               }

               if(message.startsWith("AUDIO_CALL_REQUEST")){
                   for(ClientArray client:clientArray){
                       if(client!=this){
                           client.sendMessageToClient(message);
                       }
                   }
                   continue;
               }

               if(message.startsWith("AUDIO_CALL_ACCEPT|") || message.startsWith("AUDIO_CALL_REJECT|")){
                   for(ClientArray client:clientArray){
                       if(client!=this){
                           client.sendMessageToClient(message);
                       }
                   }
                   continue;
               }

               if(message.startsWith("END_AUDIO_CALL|")){
                   System.out.println("Broadcasting end audio command to clients.");
                   for(ClientArray client:clientArray) {
                       if (client != this) {
                           client.sendMessageToClient(message);
                       }
                   }
                   continue;
               }

               saveMessages(messageForAllExceptMe);
               /*Platform.runLater(() -> {
                   HelloController.addLabel(messageForAllExceptMe, vBox);
               });*/
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
