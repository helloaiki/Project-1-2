package com.example.serverm;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ResourceBundle;

public class HelloController implements Initializable {

    @FXML
    private ImageView videoView;
    @FXML
    private Button button_send;
    @FXML
    private TextField tf_message;
    @FXML
    private VBox vBox_messages;
    @FXML
    private ScrollPane sp_main;


    private Server server;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


         //Start the server in a background thread

        new Thread(() -> {
            try {
                server = new Server(new ServerSocket(1234));

                // Start listening for client messages (on its own thread inside Server)
                server.startServer(vBox_messages);

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error starting server");
            }
        }).start();


        // When "Send" button is clicked
        button_send.setOnAction(event -> {
            String messageToSend = tf_message.getText();

            if (!messageToSend.isEmpty()) {
                // Create message bubble (right-aligned)
                HBox hBox = new HBox();
                hBox.setAlignment(Pos.CENTER_RIGHT);
                hBox.setPadding(new Insets(5, 5, 5, 10));

                Text text = new Text(messageToSend);
                TextFlow textFlow = new TextFlow(text);
                textFlow.setStyle(
                        "-fx-background-color: rgb(15,25,242); " +
                                "-fx-background-radius: 20px;"
                );
                textFlow.setPadding(new Insets(5, 10, 5, 10));
                text.setFill(Color.color(0.934, 0.945, 0.996));

                hBox.getChildren().add(textFlow);
                vBox_messages.getChildren().add(hBox);

                // Send message to the client
                server.broadCastToClients(messageToSend);

                // Clear input field
                tf_message.clear();
            }
        });

        // Automatically scroll down as new messages appear
        vBox_messages.heightProperty().addListener((observable, oldValue, newValue) -> {
            sp_main.setVvalue((Double) newValue);
        });

    }


    public  static void addLabel(String messageFromClient,VBox vBox)
    {
        HBox hBox=new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5,5,5,10));


        Text text=new Text(messageFromClient);
        TextFlow textFlow=new TextFlow(text);
        textFlow.setStyle("-fx-background-color: rgb(233,233,235); "+ "-fx-background-radius: 20px");
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
        CallServer.start(videoView);
    }

    @FXML
    private void endVideoCall(){
        CallServer.stop();

        Platform.runLater(()->{
            videoView.setImage(null);
        });
    }

}