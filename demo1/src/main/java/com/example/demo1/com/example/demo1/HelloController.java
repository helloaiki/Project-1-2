package com.example.demo1;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.image.ImageView;

public class HelloController implements Initializable {

    @FXML
    private Button button_send;
    @FXML
    private TextField tf_message;
    @FXML
    private VBox vBox_messages;
    @FXML
    private ScrollPane sp_main;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ImageView videoView;
    @FXML
    private Button loginButton;
    @FXML
    private VBox loginPane;
    @FXML
    private AnchorPane ap_main;
    private String name;
    private String password;
    private static ImageView videoViewStatic;
    private static Client client;
    private static String staticName;
    private static final String friendIP="192.168.77.4";  // It will be the first client's IP address
    private static final int friendReceivePort=5000;  // It will be 5000 for the second client
    private static final int myReceivePort=5001;  // It will be 5001 for the second client
    private Thread audioSenderThread;
    private Thread audioReceiverThread;
    private AudioSender audioSender;
    private AudioReceiver audioReceiver;
    private final String friendAudioIP= "192.168.77.4";  // It will be the first client's IP address
    private final int myAudioPort= 5555;  //It will be 6666 for the second client
    private final int friendAudioPort=6666;  //It will be 5555 for the second client
    private static RingtonePlayer ringtonePlayer= new RingtonePlayer();
    private static HelloController instance;

    @Override

    public void initialize(URL url, ResourceBundle resourceBundle) {
        instance=this;
        button_send.setDisable(true);
        videoViewStatic=videoView;
        loginButton.setOnAction(event -> {
            name = usernameField.getText().trim();
            password = passwordField.getText().trim();
            staticName=name;

            if (!name.isEmpty() && !password.isEmpty()) {
                try {
                    client = new Client(new Socket("localhost", 1234), name, password);//Change the localhost with the ip address of the other computer

                    // Start receiving messages only after login
                    if(client!=null)
                    client.receiveMessageFromServer(vBox_messages);

                    loginPane.setVisible(false);
                    ap_main.setVisible(true);
                    button_send.setDisable(false);

                    System.out.println("Logged in as " + name);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Could not connect to server.");
                }
            }
        });

        vBox_messages.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                sp_main.setVvalue((Double)t1);
            }
        });



        button_send.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                String messageToSend=tf_message.getText();
                if(!messageToSend.isEmpty())
                {
                    HBox hBox=new HBox();
                    hBox.setAlignment(Pos.CENTER_RIGHT);
                    hBox.setPadding(new Insets(5,5,5,10));
                    Text text=new Text(messageToSend);
                    TextFlow textFlow=new TextFlow(text);
                    textFlow.setStyle(
                            "-fx-background-color: rgb(15,25,242); " +
                                    "-fx-background-radius: 20px"
                    );
                    textFlow.setPadding(new Insets(5, 10, 5, 10));
                    text.setFill(Color.color(0.934, 0.945, 0.996));
                    textFlow.setPadding(new Insets(5,10,5,10));
                    hBox.getChildren().add(textFlow);
                    vBox_messages.getChildren().add(hBox);
                    client.sendMessageToServer(messageToSend);
                    tf_message.clear();
                }
            }
        });

    }

    public static HelloController getInstance(){
        return instance;
    }

    private void stopAudioCall(){
        if(audioSender!=null) audioSender.stop();
        if(audioReceiver!=null) audioReceiver.stop();

        if(audioSenderThread!=null && audioSenderThread.isAlive()) {
            audioSenderThread.interrupt();
            try{
                audioSenderThread.join(3000);
            }catch(InterruptedException e){e.printStackTrace();}
            audioSenderThread=null;
        }
        if(audioReceiverThread!=null && audioReceiverThread.isAlive()) {
            audioReceiverThread.interrupt();
            try{
                audioReceiverThread.join(3000);
            }catch (InterruptedException e){e.printStackTrace();}
            audioReceiverThread=null;
        }

        audioSender=null;
        audioReceiver=null;
    }

    private void startAudioCall(){
        stopAudioCall();
        audioSender= new AudioSender(friendAudioIP, friendAudioPort);
        audioReceiver = new AudioReceiver(myAudioPort);

        audioSenderThread = new Thread(audioSender);
        audioReceiverThread = new Thread(audioReceiver);

        audioSenderThread.start();
        audioReceiverThread.start();
    }

    public static  void addLabel(String messageFromServer,VBox vBox)
    {
        if(messageFromServer.startsWith("AUDIO_CALL_REQUEST|")){
            String caller= messageFromServer.split("\\|")[1];
            Platform.runLater(()->{
                ringtonePlayer.play();
                Alert alert= new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Incoming Audio Call");
                alert.setHeaderText("Audio call from "+caller);
                alert.setContentText("Accept or Reject?");

                ButtonType accept= new ButtonType("Accept", ButtonBar.ButtonData.OK_DONE);
                ButtonType reject= new ButtonType("Reject", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(accept,reject);

                alert.showAndWait().ifPresent(response->{
                    ringtonePlayer.stop();
                    if(response==accept){
                        client.sendMessageToServer("AUDIO_CALL_ACCEPT|"+staticName);
                        HelloController.getInstance().startAudioCall();
                    } else {
                        client.sendMessageToServer("AUDIO_CALL_REJECT|"+staticName);
                    }
                });
            });
            return;
        }

        if(messageFromServer.startsWith("AUDIO_CALL_ACCEPT|")){
            Platform.runLater(()->{
                HelloController.getInstance().startAudioCall();
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Audio Call");
                info.setHeaderText(null);
                info.setContentText("Call accepted.");
                info.show();
            });
            return;
        }

        if(messageFromServer.startsWith("AUDIO_CALL_REJECT|")){
            Platform.runLater(()->{
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Audio Call");
                info.setHeaderText(null);
                info.setContentText("Call rejected.");
                info.show();
            });
            return;
        }

        if(messageFromServer.startsWith("END_AUDIO_CALL|")){
            HelloController.getInstance().stopAudioCall();
            String caller= messageFromServer.split("\\|")[1];
            Platform.runLater(()->{
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Audio Call");
                alert.setHeaderText(null);
                alert.setContentText(caller+" has ended the audio call.");
                    alert.show();
            });
            return;
        }

        if(messageFromServer.equals("END_AUDIO")){
            HelloController.getInstance().stopAudioCall();
            return;
        }

        if(messageFromServer.startsWith("_VIDEO_CALL_REQUEST_|")){
            String caller= messageFromServer.split("\\|")[1];
            Platform.runLater(()->{
                ringtonePlayer.play();
                Alert alert= new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Incoming Video Call");
                alert.setHeaderText("Video call from "+caller);
                alert.setContentText("Accept or Reject the call?");

                ButtonType accept= new ButtonType("Accept", ButtonBar.ButtonData.OK_DONE);
                ButtonType reject= new ButtonType("Reject", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(accept,reject);

                alert.showAndWait().ifPresent(response->{
                    ringtonePlayer.stop();
                    if(response==accept){
                        client.sendMessageToServer("_START_VIDEO_|"+staticName);
                        CallClient.start(videoViewStatic,friendIP,friendReceivePort,myReceivePort);
                    } else{
                        client.sendMessageToServer("_REJECT_VIDEO_CALL_|"+staticName);
                    }
                });
            });
            return;
        }

        if(messageFromServer.startsWith("_REJECT_VIDEO_CALL_|")){
            Platform.runLater(()->{
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Call Rejected");
                alert.setHeaderText(null);
                alert.setContentText("Your video call was rejected");
                alert.show();
            });
            return;
        }

        if(messageFromServer.startsWith("_START_VIDEO_|")){
            CallClient.start(videoViewStatic,friendIP,friendReceivePort,myReceivePort);
            HelloController.getInstance().startAudioCall();
            return;
        }

        if(messageFromServer.startsWith("_END_VIDEO_|")){
            CallClient.stop();
            HelloController.getInstance().stopAudioCall();
            String caller= messageFromServer.split("\\|")[1];
            Platform.runLater(()->{
                videoViewStatic.setImage(null);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Video Call");
                alert.setHeaderText(null);
                alert.setContentText(caller+" has ended the video call.");
                alert.show();
            });
            return;
        }

        HBox hBox=new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5,5,5,10));


        Text text=new Text(messageFromServer);
        TextFlow textFlow=new TextFlow(text);
        textFlow.setStyle("-fx-background-color: rgb(233,233,235)"+ "-fx-background-radius: 20px");
        textFlow.setPadding(new Insets(5,10,5,10));
        hBox.getChildren().add(textFlow);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                vBox.getChildren().add(hBox);
            }
        });

    }


    @FXML
    private void startVideoCall(){
        client.sendMessageToServer("_VIDEO_CALL_REQUEST_|"+name);
        //CallClient.start(videoView,friendIP,friendReceivePort,myReceivePort);
    }

    @FXML
    private void endVideoCall(){
        CallClient.stop();
        client.sendMessageToServer("_END_VIDEO_|"+name);

        Platform.runLater(()->{
            videoView.setImage(null);
        });
    }

    @FXML
    private void handleStartAudioCall(){
        if(client!=null){
            client.sendMessageToServer("AUDIO_CALL_REQUEST|"+name);
        }
    }

    @FXML
    private void handleEndAudioCall(){
        if(audioSender!=null || audioReceiver!=null){
            stopAudioCall();
            client.sendMessageToServer("END_AUDIO_CALL|"+name);
        }
    }
}