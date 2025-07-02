module com.example.serverm {
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
    requires org.bytedeco.javacpp;
    requires org.bytedeco.opencv;
    requires javafx.swing;

    opens com.example.serverm to javafx.fxml;
    exports com.example.serverm;
}