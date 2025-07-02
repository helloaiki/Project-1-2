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

    private Client client;


    @Override

    public void initialize(URL url, ResourceBundle resourceBundle) {
        button_send.setDisable(true);

        loginButton.setOnAction(event -> {
            name = usernameField.getText().trim();
            password = passwordField.getText().trim();

            if (!name.isEmpty() && !password.isEmpty()) {
                try {
                    client = new Client(new Socket("localhost", 1234), name, password);

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
        CallClient.start(videoView);
    }
}