module transmitter {
  requires javafx.controls;
  requires javafx.fxml;
  // ikonli
  requires org.kordamp.ikonli.core;
  requires org.kordamp.ikonli.javafx;
  requires org.kordamp.ikonli.materialdesign2;

  requires com.fazecast.jSerialComm;
  requires com.jfoenix;

  requires json;
  requires socket.io.client;
  requires engine.io.client;

  requires java.logging;
  requires java.prefs;

  opens transmitter.ui.controls.alert to javafx.fxml;
  opens transmitter.ui.controller to javafx.fxml;
  opens transmitter.ui.controller.main to javafx.fxml;
  opens transmitter.ui.controller.main.tab to javafx.fxml;

  exports transmitter;
  exports transmitter.ui;
  exports transmitter.ui.controller;
}
