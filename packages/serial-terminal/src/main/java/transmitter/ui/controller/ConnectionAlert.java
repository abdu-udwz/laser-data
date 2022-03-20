package transmitter.source.ui.controller;

import com.fazecast.jSerialComm.SerialPort;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.validation.RequiredFieldValidator;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import transmitter.source.socket.SocketIdentity;
import transmitter.source.socket.SocketManager;
import transmitter.source.ui.controls.alert.AlertAction;
import transmitter.source.ui.controls.alert.AlertLayout;
import transmitter.source.ui.controls.alert.CustomAlert;
import transmitter.source.connection.BAO;
import transmitter.source.connection.BaudRate;
import transmitter.source.setting.SettingKey;
import transmitter.source.setting.Settings;
import transmitter.source.ui.Alerts;
import transmitter.source.ui.controller.main.MainController;
import transmitter.source.ui.controller.main.tab.MainTab;
import transmitter.source.util.Res;
import transmitter.source.util.Utils;
import transmitter.source.util.Windows;
import transmitter.source.util.logging.terminal.TerminalLoggable;

import java.io.IOException;
import java.net.ConnectException;
import java.util.logging.Level;

public class ConnectionAlert extends CustomAlert<ConnectionAlert.ConnectionAlertLayout> implements TerminalLoggable {

    public class ConnectionAlertLayout extends AlertLayout{

        private GridPane inputPane;
        private JFXProgressBar progressBar;
        private JFXComboBox<SerialPort> serialCombo;
        private JFXButton portReloadButton;
        private JFXComboBox<BaudRate> baudCombo;
        private JFXComboBox<SocketIdentity> socketIdentityCombo;

        private JFXButton connectButton;
        private JFXButton settingsButton;
        private JFXButton closeButton;

        private class CustomBody extends VBox{
            @FXML private GridPane inputPane;
            @FXML private JFXProgressBar progressBar;
            @FXML private JFXComboBox<SerialPort> serialCombo;
            @FXML private JFXButton portReloadButton;
            @FXML private JFXComboBox<BaudRate> baudCombo;
            @FXML private JFXComboBox<SocketIdentity> socketIdentityCombo;

            @FXML private JFXButton connectButton;
            @FXML private JFXButton settingsButton;
            @FXML private JFXButton closeButton;


            private CustomBody(){
                FXMLLoader loader = new FXMLLoader(Res.Fxml.CONNECT_DIALOG_LAYOUT.getUrl(), Utils.getBundle());
                loader.setController(this);
                loader.setRoot(this);
                try {
                    loader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ConnectionAlertLayout.this.inputPane = this.inputPane;
                ConnectionAlertLayout.this.progressBar = this.progressBar;
                ConnectionAlertLayout.this.serialCombo = this.serialCombo;
                ConnectionAlertLayout.this.portReloadButton = this.portReloadButton;
                ConnectionAlertLayout.this.baudCombo = this.baudCombo;
                ConnectionAlertLayout.this.socketIdentityCombo = this.socketIdentityCombo;
                ConnectionAlertLayout.this.connectButton = this.connectButton;
                ConnectionAlertLayout.this.settingsButton = this.settingsButton;
                ConnectionAlertLayout.this.closeButton = this.closeButton;
            }
        }

        @Override
        protected void initialize(){
            super.initialize();
            this.getStyleClass().add("connection-alert-layout");
            bodyPane.getChildren().remove(bodyLabel);
            bodyPane.getChildren().add(new CustomBody());
        }
    }

    private Task<Boolean> connectionTask;

    public ConnectionAlert(){
        super((AlertAction) null);
        setLayout(new ConnectionAlertLayout());

        setHeadingText("Connect To Board");
        setGraphicSize(0);
        initStyle(StageStyle.TRANSPARENT);

        initComponents();
        initListeners();

        updateProgressView(false);
        setSelectedBaudRate(BaudRate.valueOf(Settings.getString(SettingKey.BAUD_RATE)));
        setSelectedSocketIdentity(SocketIdentity.valueOf(Settings.getString(SettingKey.SOCKET_IDENTITY)));

        getDialogPane().getStyleClass().add("connection-alert-pane");
        layout().setPrefWidth(280);
        layout().setPrefHeight(280);
    }

    private void initComponents(){

        Callback<Boolean, JFXListCell<SerialPort> > serialCellFactory = Utils.customCellFactory(serialPort -> {
            return serialPort.getSystemPortName() + " (" + serialPort.getPortDescription() + ")";
        });

        Callback<Boolean, JFXListCell<SocketIdentity> > socketIdentityCellFactory = Utils.customCellFactory(identity -> {
            return identity.displayString;
        });

        layout().serialCombo.setButtonCell(serialCellFactory.call(true));
        layout().serialCombo.setCellFactory(param ->  serialCellFactory.call(false));

        layout().socketIdentityCombo.setButtonCell(socketIdentityCellFactory.call(true));
        layout().socketIdentityCombo.setCellFactory(param ->  socketIdentityCellFactory.call(false));

        layout().serialCombo.getItems().clear();
        layout().baudCombo.getItems().clear();

        RequiredFieldValidator fieldValidator = new RequiredFieldValidator("Required");
        layout().serialCombo.getValidators().add(fieldValidator);
        layout().baudCombo.getValidators().add(fieldValidator);
        layout().socketIdentityCombo.getValidators().add(fieldValidator);

        //TODO: check this it's not good practice
        // use background thread..
        layout().serialCombo.getItems().addAll(SerialPort.getCommPorts());
        layout().baudCombo.getItems().addAll(BaudRate.values());
        layout().socketIdentityCombo.getItems().addAll(SocketIdentity.values());
    }

    private void initListeners(){

        layout().portReloadButton.setOnAction(event -> {
            layout().serialCombo.getItems().clear();
            layout().serialCombo.getItems().addAll(SerialPort.getCommPorts());
        });

        layout().connectButton.setOnAction(event -> {

            if (connectionTask != null && connectionTask.isRunning()){
                // when there's a connection task currently running
                BAO.serial().close();
                connectionTask.cancel(true);
                updateConnectButton(false, false);
            }
            else{
                connectToBoard();
            }
        });

        Settings.addChangeListener(keys -> {
            // when user changes the baud rate ... change baud rate here too
            if (keys.contains(SettingKey.BAUD_RATE)){
                BaudRate baudRate = BaudRate.valueOf(Settings.getString(SettingKey.BAUD_RATE));
                layout().baudCombo.setValue(baudRate);
            }

            if (keys.contains(SettingKey.SOCKET_IDENTITY)){
                SocketIdentity identity = SocketIdentity.valueOf(Settings.getString(SettingKey.SOCKET_IDENTITY));
                layout().socketIdentityCombo.setValue(identity);
            }
        });

        layout().settingsButton.setOnAction(event -> {
            Windows.SETTINGS_WINDOW.showAndWait();
        });

        layout().closeButton.setOnAction(event -> this.close());
    }

    private void connectToBoard(){

        if (! validateInput())
            return;

        SerialPort serialPort = getSelectedSerialPort();
        BaudRate baudRate = getSelectedBaudRate();
        SocketIdentity socketIdentity = getSelectedSocketIdentity();
        String socketHost = Settings.getString(SettingKey.SOCKET_HOST);

        connectionTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {

                logTerminal(Level.INFO, "Establishing connection..");
                boolean connected = BAO.connect(serialPort, baudRate);

                if (isCancelled()) {
                    logTerminal(Level.WARNING, "Connection was canceled.");
                    throw new InterruptedException("Connection to board was canceled.");
                }

                if (connected) {
                    logTerminal(Level.INFO, "Serial port opened successfully");
                    // serial is opened successfully
                    Platform.runLater(() -> updateConnectButton(false, true));

                    // wait for the board start-up
                    logTerminal(Level.INFO, "Waiting for board start-up");
                    Thread.sleep(2000);

                    Task<Boolean> setupTask = BAO.setupBoard();
                    setupTask.run();
                    setupTask.get();

                    SocketManager.connect(socketIdentity, socketHost);
                    return true;
                } else {
                    throw new ConnectException("Could not open port [" + serialPort.getSystemPortName() + "]");
                }
            }
        };

        connectionTask.runningProperty().addListener((observable, oldValue, newValue) -> updateProgressView(newValue));

        connectionTask.setOnFailed(event -> {
            event.getSource().getException().printStackTrace();

            logTerminal(Level.SEVERE, "An error occurred while connecting to board.");
            updateConnectButton(false, false);

            String body = "An error occurred while configuring the connection with the board on port " + serialPort.getSystemPortName() + ".\n\n" +
                    "Make sure the selected port is for the board. If this error occurred again try restarting the board.";

            Alerts.errorAlert(null,
                              "Cannot Connect to board",
                                body,
                              event.getSource().getException(),
                              AlertAction.OK);

        });

        connectionTask.setOnSucceeded(event -> {
            logTerminal(Level.FINEST, "Connected to board successfully.");

            Windows.MAIN_WINDOW.show();
            MainController.getCurrent().setSelectedTab(MainTab.MESSENGER);
            this.close();
        });


        new Thread(null, connectionTask, "connection task").start();
    }

    private boolean validateInput(){
        boolean portName = layout().serialCombo.validate();
        boolean baudRate = layout().baudCombo.validate();
        boolean socketIdentity = layout().socketIdentityCombo.validate();
        return portName && baudRate && socketIdentity;
    }

    private void updateProgressView(boolean progress){
        //TODO: fix this for performance
        if (progress)
            layout().progressBar.setProgress(-1);
        else
            layout().progressBar.setProgress(1);

        layout().inputPane.setDisable(progress);
        layout().settingsButton.setDisable(progress);
        layout().closeButton.setDisable(progress);
        layout().connectButton.setDisable(progress);

        layout().progressBar.setVisible(progress);
    }

    private void updateConnectButton(boolean disable, boolean cancel){
        layout().connectButton.setDisable(disable);
        if (cancel)
            layout().connectButton.setText("Cancel");
        else
            layout().connectButton.setText("Connect");
    }

    public void setSelectedSerialPort(SerialPort portName){
        layout().serialCombo.setValue(portName);
    }

    public SerialPort getSelectedSerialPort(){
        return layout().serialCombo.getValue();
    }

    public ObjectProperty<SerialPort> selectedSerialPortProperty(){
        return layout().serialCombo.valueProperty();
    }


    public void setSelectedBaudRate(BaudRate rate){
        layout().baudCombo.setValue(rate);
    }

    public BaudRate getSelectedBaudRate(){
        return layout().baudCombo.getValue();
    }

    public ObjectProperty<BaudRate> selectedBaudRateProperty(){
        return layout().baudCombo.valueProperty();
    }


    public void setSelectedSocketIdentity(SocketIdentity identity){
        layout().socketIdentityCombo.setValue(identity);
    }

    public SocketIdentity getSelectedSocketIdentity(){
        return layout().socketIdentityCombo.getValue();
    }

    public ObjectProperty<SocketIdentity> selectedSocketIdentityProperty(){
        return layout().socketIdentityCombo.valueProperty();
    }

    @Override
    public String getTerminalHead() {
        return "Connection";
    }
}