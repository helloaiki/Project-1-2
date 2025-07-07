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
    private static final String friendIP="192.168.77.4";// It will be the first client's IP address
    private static final int friendReceivePort=5001;// It will be 5000 for the second client
    private static final int myReceivePort=5000;// It will be 5001 for the second client


    @Override

    public void initialize(URL url, ResourceBundle resourceBundle) {
        button_send.setDisable(true);
        videoViewStatic=videoView;
        loginButton.setOnAction(event -> {
            name = usernameField.getText().trim();
            password = passwordField.getText().trim();

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

    public static  void addLabel(String messageFromServer,VBox vBox)
    {
        if(messageFromServer.equals("_VIDEO_CALL_REQUEST_")){
            Platform.runLater(()->{
                Alert alert= new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Incoming Video Call");
                alert.setHeaderText("You have an incoming call.");
                alert.setContentText("Accept or Reject the call.");

                ButtonType accept= new ButtonType("Accept");
                ButtonType reject= new ButtonType("Reject");
                alert.getButtonTypes().setAll(accept,reject);

                alert.showAndWait().ifPresent(response->{
                    if(response==accept){
                        client.sendMessageToServer("_START_VIDEO_");
                        CallClient.start(videoViewStatic,friendIP,friendReceivePort,myReceivePort);
                    } else{
                        client.sendMessageToServer("_REJECT_VIDEO_CALL_");
                    }
                });
            });
            return;
        }

        if(messageFromServer.equals("_REJECT_VIDEO_CALL_")){
            Platform.runLater(()->{
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Call Rejected");
                alert.setHeaderText(null);
                alert.setContentText("Your video call was rejected");
                alert.show();
            });
            return;
        }

        if(messageFromServer.equals("_START_VIDEO_")){
            CallClient.start(videoViewStatic,friendIP,friendReceivePort,myReceivePort);
            return;
        }

        if(messageFromServer.equals("_END_VIDEO_")){
            CallClient.stop();
            Platform.runLater(()->{
                videoViewStatic.setImage(null);
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
        client.sendMessageToServer("_VIDEO_CALL_REQUEST_");
        //CallClient.start(videoView,friendIP,friendReceivePort,myReceivePort);
    }

    @FXML
    private void endVideoCall(){
        CallClient.stop();
        client.sendMessageToServer("_END_VIDEO_");

        Platform.runLater(()->{
            videoView.setImage(null);
        });
    }
}