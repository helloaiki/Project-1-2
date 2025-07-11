module com.example.demo1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires org.bytedeco.opencv;
    requires java.desktop;
    requires javafx.swing;
    requires com.example.serverm;
    requires com.fasterxml.jackson.core;
    requires javafx.media;

    opens com.example.demo1 to javafx.fxml;
    exports com.example.demo1;
}