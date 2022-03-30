package transmitter.ui.controller.main;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXToggleButton;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import transmitter.socket.SocketManager;
import transmitter.ui.controls.alert.AlertAction;
import transmitter.ui.controls.alert.InfoAlert;
import transmitter.ui.controls.alert.InfoAlertType;
import transmitter.connection.BAO;
import transmitter.connection.protocol.LaserProtocol;
import transmitter.connection.receiver.VirtualReceiver;
import transmitter.connection.serial.listener.InMessageListener;
import transmitter.connection.serial.listener.InMessageListenerType;
import transmitter.message.in.LightReadingMessage;
import transmitter.setting.SettingKey;
import transmitter.setting.Settings;
import transmitter.ui.Alerts;
import transmitter.ui.controller.ConnectionAlert;
import transmitter.ui.controller.main.tab.MainTab;
import transmitter.util.Threading;
import transmitter.util.Windows;

import java.io.IOException;
import java.util.concurrent.*;

public class MainController {

    public static MainController getCurrent() {
        return Windows.MAIN_WINDOW.getController();
    }

    @FXML
    private VBox mainPane;

    @FXML
    private Label socketLabel;
    @FXML
    private Label portLabel;
    @FXML
    private JFXButton disconnectButton;
    @FXML
    private JFXListView<MainTab> sidePanel;
    @FXML
    private StackPane contentStack;

    @FXML
    private JFXButton settingsButton;
    @FXML
    private JFXToggleButton receiverToggle;

    private final ObjectProperty<MainTab> selectedTab = new SimpleObjectProperty<>();
    private final ObservableMap<MainTab, Node> tabsNodes = FXCollections.observableHashMap();

    private final ObjectProperty<CommState> state = new SimpleObjectProperty<>();
    private final ObjectProperty<VirtualReceiver> virtualReceiver = new SimpleObjectProperty<>();
    private final ObjectProperty<LaserProtocol> protocol = new SimpleObjectProperty<>();

    private final InMessageListener<LightReadingMessage> lightListener = message -> {
        getVirtualReceiver().setLightReading(message);
        // System.out.println(message.getRawMessage());
    };

    @FXML
    private void initialize() {
        setProtocol(LaserProtocol.getFromSettings());

        initComponents();
        initListeners();
        setState(CommState.STANDBY);
        updateSocketLabel(SocketManager.isConnected());
    }

    private void initComponents() {
        sidePanel.getItems().addAll(MainTab.values());
        sidePanel.setCellFactory(param -> new JFXListCell<MainTab>() {
            @Override
            protected void updateItem(MainTab item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    setText(item.displayString);
                }
            }
        });

        portLabel.setText(BAO.serial().getSystemPortName());
    }

    private void initListeners() {

        stateProperty().addListener((observable, oldValue, newValue) -> onStateChanged(oldValue, newValue));

        SocketManager.addEventListener(SocketManager.EVENT_TOGGLE_RECEIVER, args -> {
            receiverToggle.fire();
        });

        receiverToggle.addEventHandler(ActionEvent.ACTION, event -> {
            receiverToggle.setDisable(true);

            boolean newReceiverState = receiverToggle.isSelected();
            BAO.serial().removeInMessageListener(InMessageListenerType.LIGHT_READ, lightListener);
            Task<Boolean> task = BAO.changeReceiverState(newReceiverState);

            task.setOnSucceeded(event1 -> {
                if (newReceiverState) {
                    receiverToggle.setText("Receiver ON");
                    setState(CommState.RECEIVING);
                } else {
                    receiverToggle.setText("Receiver OFF");
                    setState(CommState.STANDBY);
                }
            });

            task.setOnFailed(event1 -> {
                // return the toggle to it's previous state
                receiverToggle.setSelected(!receiverToggle.isSelected());
                SocketManager.emit(SocketManager.EVENT_RECEIVER_TOGGLE_ERROR);
                Alerts.errorAlert(null,
                        "Couldn't Change Receiver State",
                        "An Error occurred while trying to turn the receiver on/off.\n\n" +
                                "Please try again, if the problem continues try resetting the board.",
                        event1.getSource().getException(),
                        AlertAction.OK);
            });

            task.runningProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    Threading.SCHEDULED_POOL.schedule(() -> receiverToggle.setDisable(false), 1500,
                            TimeUnit.MILLISECONDS);
                }
            });

            Threading.FIXED_POOL.submit(task);
        });

        selectedTabProperty().addListener((observable, oldValue, newMainTab) -> onTabChanged());
        selectedTab.bind(sidePanel.getSelectionModel().selectedItemProperty());

        disconnectButton.setOnAction(event -> {
            Windows.MAIN_WINDOW.close();
            new ConnectionAlert().show();
        });

        Settings.addChangeListener(keys -> {
            boolean protocolChanged = false;

            for (SettingKey protocolKey : SettingKey.protcolKeys()) {
                if (keys.contains(protocolKey)) {
                    protocolChanged = true;
                    break;
                }
            }

            if (protocolChanged) {

                if (!isStandby()) {
                    String bodyText = "Changed settings will take effect in the next receiving or transmitting process."
                            +
                            "\nYou have to restart the receiver manually.";
                    Alerts.warningAlert(Windows.MAIN_WINDOW,
                            "Communication Settings Changed",
                            bodyText,
                            AlertAction.OK);
                }

                // for bit-delay, we got to change the setting on board..
                if (keys.contains(SettingKey.BIT_DELAY)) {

                    if (!isStandby()) {

                        Settings.putInteger(SettingKey.BIT_DELAY, getProtocol().getBitDelay());
                        Alerts.warningAlert(Windows.MAIN_WINDOW,
                                "Cannot Change Bit Delay",
                                "An attempt to change bit delay while a transmitting or receiving process is going failed. You have to change the bit delay again after the current process is done.\n\nError: bit_delay_changed_error",
                                AlertAction.OK);
                    } else {
                        Task<Boolean> bitDelayTask = BAO.setupBoard();
                        bitDelayTask.runningProperty()
                                .addListener(observable -> updateBackgroundProcessView(bitDelayTask.isRunning()));
                        bitDelayTask.setOnFailed(event -> {
                            Alerts.errorAlert(Windows.MAIN_WINDOW,
                                    "Couldn't Re-configure Board",
                                    "An error occurred while trying to change on-board settings.\n\n Error: bit_delay_board_change_error",
                                    event.getSource().getException(),
                                    AlertAction.OK);
                        });
                        Threading.FIXED_POOL.submit(bitDelayTask);
                    }
                }

                setProtocol(LaserProtocol.getFromSettings());
            }

        });

        settingsButton.setOnAction(event -> Windows.SETTINGS_WINDOW.show());

        // FIXME: currently this event handler gets registered many times, because it's
        // connected to the
        // final instance of LoadableState in the `Windows` class and its listeners
        // don't get destroyed
        // when the window is closed because (the window itself is not destroyed only
        // the scene)
        // Dec 22nd 2019.
        Windows.MAIN_WINDOW.addEventHandler(WindowEvent.WINDOW_HIDING, closeEvent -> disconnect());

        // ==== socket status binding ====== //
        SocketManager.connectedProperty().addListener((observable, oldValue, newValue) -> {
            updateSocketLabel(newValue);
        });
    }

    private void onTabChanged() {
        FXMLLoader loader = new FXMLLoader(getSelectedTab().fxmlUrl);
        try {
            if (getSelectedTab() == null)
                throw new NullPointerException("Selected Tab cannot be null.");

            Node tabNode = getTabNode(getSelectedTab());
            if (tabNode == null) {
                tabNode = loader.load();
                tabsNodes.put(getSelectedTab(), tabNode);
            }
            contentStack.getChildren().clear();
            contentStack.getChildren().add(tabNode);

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.gc();
    }

    private long lastDataLossWarning = 0;

    private void onStateChanged(CommState oldCommState, CommState newCommState) {

        SocketManager.emit(SocketManager.EVENT_SET_STATE, newCommState.name());

        updateViewForState();

        if (newCommState == CommState.RECEIVING) {
            setVirtualReceiver(new VirtualReceiver(getProtocol()));

            getVirtualReceiver().lossFlagProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {

                    long timeSinceLastWarning = System.currentTimeMillis() - lastDataLossWarning;

                    if (timeSinceLastWarning > 5 * 1000) {
                        lastDataLossWarning = System.currentTimeMillis();

                        InfoAlert warningAlert = new InfoAlert(InfoAlertType.WARNING, AlertAction.OK);
                        warningAlert.setHeadingText("Possible Data Lost");

                        int lostIndex = getVirtualReceiver().getReceivedPackets().size();
                        warningAlert.setBodyText(
                                "Some data might have been lost while receiving. The receiving process will continue but no grants for what the data will look like!\n\nLost packet index: "
                                        + lostIndex);
                        warningAlert.show();
                    }
                }
            });

            if (SocketManager.isConnected()) {
                getVirtualReceiver().bitBufferProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue.length() == 8) {
                        SocketManager.emit(SocketManager.EVENT_BIT_BUFFER_UPDATED, newValue);
                    }
                });
            }

            Threading.FIXED_POOL
                    .submit(() -> BAO.serial().addInMessageListener(InMessageListenerType.LIGHT_READ, lightListener));
        }
        System.gc();
    }

    /*
     * ****************************************** *
     * *
     * UTILS *
     * ******************************************
     */

    private void disconnect() {

        Task<Boolean> closeSerialTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                SocketManager.disconnect();
                Task<Boolean> disconnectTask = BAO.disconnect();
                disconnectTask.run();
                disconnectTask.get(2500, TimeUnit.MILLISECONDS);
                return true;
            }
        };

        mainPane.disableProperty().bind(closeSerialTask.runningProperty());

        closeSerialTask.setOnFailed(event -> {

            event.getSource().getException().printStackTrace();

            Alerts.errorAlert(null,
                    "Couldn't Close Port",
                    "An error occurred while attempting to close the connection with the board",
                    event.getSource().getException(),
                    AlertAction.OK);
        });

        closeSerialTask.setOnSucceeded(event -> {
            System.out.println("=== successfully disconnected ===");
        });

        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Threading.FIXED_POOL.submit(closeSerialTask);
    }

    private void updateSocketLabel(boolean connected) {
        socketLabel.setText(connected ? "Connected" : "Not connected");
    }

    private void updateBackgroundProcessView(boolean processing) {
        receiverToggle.setDisable(processing);
        disconnectButton.setDisable(processing);
    }

    private void updateViewForState() {
        boolean con = isTransmitting() || isReceiving();
        receiverToggle.setDisable(con);
        disconnectButton.setDisable(con);
    }

    public void setSelectedTab(MainTab tab) {
        sidePanel.getSelectionModel().select(tab);
    }

    public Node getTabNode(MainTab tab) {
        return tabsNodes.get(tab);
    }

    public boolean isTransmitting() {
        return getState() == CommState.TRANSMITTING;
    }

    public boolean isReceiving() {
        return getState() == CommState.RECEIVING;
    }

    public boolean isStandby() {
        return getState() == CommState.STANDBY;
    }

    // ********* setters && getters ********* //

    public MainTab getSelectedTab() {
        return selectedTab.get();
    }

    public ReadOnlyObjectProperty<MainTab> selectedTabProperty() {
        return selectedTab;
    }

    public CommState getState() {
        return state.get();
    }

    public ReadOnlyObjectProperty<CommState> stateProperty() {
        return state;
    }

    public void setState(CommState commState) {
        this.state.set(commState);
    }

    public VirtualReceiver getVirtualReceiver() {
        return virtualReceiver.get();
    }

    public ReadOnlyObjectProperty<VirtualReceiver> virtualReceiverProperty() {
        return virtualReceiver;
    }

    public void setVirtualReceiver(VirtualReceiver virtualReceiver) {
        this.virtualReceiver.set(virtualReceiver);
    }

    public LaserProtocol getProtocol() {
        return protocol.get();
    }

    public ReadOnlyObjectProperty<LaserProtocol> protocolProperty() {
        return protocol;
    }

    private void setProtocol(LaserProtocol protocol) {
        this.protocol.set(protocol);
    }
}