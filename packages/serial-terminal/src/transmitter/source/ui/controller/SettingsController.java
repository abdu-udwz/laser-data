package transmitter.source.ui.controller;

import com.jfoenix.controls.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import transmitter.source.socket.SocketIdentity;
import transmitter.source.ui.controls.alert.AlertAction;
import transmitter.source.connection.BaudRate;
import transmitter.source.encode.Determiner;
import transmitter.source.setting.SettingKey;
import transmitter.source.setting.Settings;
import transmitter.source.ui.Alerts;
import transmitter.source.util.Windows;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Supplier;

import static transmitter.source.util.Utils.customCellFactory;

public class SettingsController {

    @FXML private JFXButton serialCategoryButton;
    @FXML private JFXButton commCategoryButton;

    @FXML private VBox settingsContentContainer;
    @FXML private StackPane categoryStack;

    @FXML private VBox serialContentPane;

    @FXML private JFXComboBox<BaudRate> baudRateCombo;
    @FXML private JFXComboBox<Integer> transmitterPinCombo;
    @FXML private JFXComboBox<Integer> receiverPinCombo;

    @FXML private JFXComboBox<SocketIdentity> socketIdentityCombo;
    @FXML private JFXTextField socketHostField;

    @FXML private JFXButton cancelButton;
    @FXML private JFXButton okButton;

    @FXML private VBox commContentPane;

    @FXML private JFXSlider bitDelaySlider;
    @FXML private Label bitDelayLabel;
    @FXML private JFXComboBox<Determiner> determinerCombo;
    @FXML private JFXComboBox<String> encodingCombo;

    @FXML private JFXSlider packetSizeSlider;
    @FXML private Label packetSizeLabel;

    @FXML private JFXSlider highValueSlider;
    @FXML private Label highValueLabel;
    @FXML private JFXSlider byteCorrectionSlider;
    @FXML private Label byteCorrectionLabel;


    @FunctionalInterface
    private interface SettingSetter {
        void set();
    }

    private final ObservableMap<SettingKey, SettingSetter> settingNodesMap = FXCollections.observableHashMap();
    private final ObservableMap<SettingKey, Supplier<Object>> currentSettings = FXCollections.observableHashMap();

    @FXML
    private void initialize(){

        initListeners();
        initComponents();

        initMapping();

        serialCategoryButton.fire();

        loadSettings(Settings.getSettingsMap());
    }

    private void initMapping(){
        settingNodesMap.put(SettingKey.BAUD_RATE, () ->  {
            BaudRate baudRate = BaudRate.valueOf(Settings.getString(SettingKey.BAUD_RATE));
            baudRateCombo.setValue(baudRate);
        });
        settingNodesMap.put(SettingKey.TRANSMITTER_PIN, () -> {
            int transmitterPin = Settings.getInteger(SettingKey.TRANSMITTER_PIN);
            transmitterPinCombo.setValue(transmitterPin);
        });
        settingNodesMap.put(SettingKey.RECEIVER_PIN, () -> {
            int receiverPin = Settings.getInteger(SettingKey.RECEIVER_PIN);
            receiverPinCombo.setValue(receiverPin);
        });

        // ==== socket getters ===== //
        settingNodesMap.put(SettingKey.SOCKET_IDENTITY, () -> {
            SocketIdentity identity = SocketIdentity.valueOf(Settings.getString(SettingKey.SOCKET_IDENTITY));
            socketIdentityCombo.setValue(identity);
        });
        settingNodesMap.put(SettingKey.SOCKET_HOST, () -> {
            String socketHost = Settings.getString(SettingKey.SOCKET_HOST);
            socketHostField.setText(socketHost);
        });

        settingNodesMap.put(SettingKey.BIT_DELAY, () -> {
            int bitDelay = Settings.getInteger(SettingKey.BIT_DELAY);
            bitDelaySlider.setValue(bitDelay);
        });
        settingNodesMap.put(SettingKey.PACKET_DETERMINER, () -> {
            Determiner determiner = Determiner.valueOf(Settings.getString(SettingKey.PACKET_DETERMINER));
            determinerCombo.setValue(determiner);
        });
        settingNodesMap.put(SettingKey.TEXT_ENCODING_CHARSET, () -> {
            String encodingCharset = Settings.getString(SettingKey.TEXT_ENCODING_CHARSET);
            encodingCombo.setValue(encodingCharset);
        });

        settingNodesMap.put(SettingKey.PACKET_SIZE, () -> {
            int packetSize = Settings.getInteger(SettingKey.PACKET_SIZE);
            packetSizeSlider.setValue(packetSize);
        });

        settingNodesMap.put(SettingKey.LIGHT_HIGH_VALUE, () -> {
            int highRate  = Settings.getInteger(SettingKey.LIGHT_HIGH_VALUE);
            highValueSlider.setValue(highRate);
        });

        settingNodesMap.put(SettingKey.BYTE_CORRECTION, () -> {
            int byteCorrection = Settings.getInteger(SettingKey.BYTE_CORRECTION);
            byteCorrectionSlider.setValue(byteCorrection);
        });

        currentSettings.put(SettingKey.BAUD_RATE, () -> baudRateCombo.getValue().name());
        currentSettings.put(SettingKey.TRANSMITTER_PIN, () -> transmitterPinCombo.getValue());
        currentSettings.put(SettingKey.RECEIVER_PIN, () -> receiverPinCombo.getValue());

        currentSettings.put(SettingKey.SOCKET_IDENTITY, () -> socketIdentityCombo.getValue().name());
        currentSettings.put(SettingKey.SOCKET_HOST, () -> socketHostField.getText());

        currentSettings.put(SettingKey.BIT_DELAY, () -> (int)bitDelaySlider.getValue());
        currentSettings.put(SettingKey.PACKET_DETERMINER, () -> determinerCombo.getValue().toString());
        currentSettings.put(SettingKey.TEXT_ENCODING_CHARSET, ()-> encodingCombo.getValue());

        currentSettings.put(SettingKey.PACKET_SIZE, () -> (int)packetSizeSlider.getValue());

        currentSettings.put(SettingKey.LIGHT_HIGH_VALUE, () -> (int) highValueSlider.getValue());
        currentSettings.put(SettingKey.BYTE_CORRECTION, () -> (int) byteCorrectionSlider.getValue());
    }

    private void initComponents(){

        Callback<Boolean, JFXListCell<BaudRate>> baudCellFactory =
                customCellFactory( rate -> String.valueOf(rate.val));
        baudRateCombo.setButtonCell(baudCellFactory.call(true));
        baudRateCombo.setCellFactory(param -> baudCellFactory.call(false));

        Callback<Boolean, JFXListCell<Integer>> analogPinCellFactory =
                customCellFactory( pinNum -> "A"+ pinNum );
        receiverPinCombo.setButtonCell(analogPinCellFactory.call(true));
        receiverPinCombo.setCellFactory(p -> analogPinCellFactory.call(true));

        baudRateCombo.getItems().addAll(BaudRate.values());
        for (int i = 0; i < 14; i++) {
            transmitterPinCombo.getItems().add(i);
        }
        for (int i = 0; i < 6; i++) {
            receiverPinCombo.getItems().add(i);
        }

        BaudRate baudRate = BaudRate.valueOf(Settings.getString(SettingKey.BAUD_RATE));
        baudRateCombo.setValue(baudRate);
        transmitterPinCombo.setValue(Settings.getInteger(SettingKey.TRANSMITTER_PIN));
        receiverPinCombo.setValue(Settings.getInteger(SettingKey.RECEIVER_PIN));

        // ======== SOCKET SETTINGS ==========//

        Callback<Boolean, JFXListCell<SocketIdentity>> socketIdentityCellFactory =
                customCellFactory( identity -> identity.displayString);
        socketIdentityCombo.setButtonCell(socketIdentityCellFactory.call(true));
        socketIdentityCombo.setCellFactory(param -> socketIdentityCellFactory.call(false));
        socketIdentityCombo.getItems().addAll(SocketIdentity.values());

        // ========= protocol settings =============== //
        bitDelaySlider.setMin(6);
        bitDelaySlider.setMax(32);
        bitDelaySlider.setMajorTickUnit(8);
        bitDelaySlider.setMinorTickCount(3);

        Callback<Boolean, JFXListCell<Determiner>> determinerCellFactory =
                customCellFactory( determiner -> "D"+determiner.index + " - ("+ determiner.string + ")");

        determinerCombo.setButtonCell(determinerCellFactory.call(true));
        determinerCombo.setCellFactory(p -> determinerCellFactory.call(false));
        determinerCombo.getItems().addAll(Determiner.values());

        encodingCombo.getItems().addAll("ASCII", "UTF-8", "UTF-16", "windows-1256", "ISO-8859-6");

        // sending settings
        packetSizeSlider.setMin(54);
        packetSizeSlider.setMax(74);
        packetSizeSlider.setBlockIncrement(2);

        packetSizeSlider.setMinorTickCount(0);
        packetSizeSlider.setMajorTickUnit(2);

        // receiving settings
        byteCorrectionSlider.setMin(0);
        byteCorrectionSlider.setMax(30);
        byteCorrectionSlider.setBlockIncrement(1);
        byteCorrectionSlider.setMinorTickCount(2);
        byteCorrectionSlider.setMajorTickUnit(3);

        highValueSlider.setMax(1024);
        highValueSlider.setMin(128);

        highValueSlider.setBlockIncrement(1);

        highValueSlider.setMinorTickCount(0);
        highValueSlider.setMajorTickUnit(180);


    }

    private void initListeners(){

        setSliderLabel(bitDelaySlider, bitDelayLabel, "ms");
        setSliderLabel(packetSizeSlider, packetSizeLabel, "bytes");
        setSliderLabel(byteCorrectionSlider, byteCorrectionLabel, "");
        setSliderLabel(highValueSlider, highValueLabel, "");

        bitDelaySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            syncCorrectionRangeWithDelay(oldValue.intValue(), newValue.intValue());
        });

        cancelButton.setOnAction(event -> Windows.SETTINGS_WINDOW.close());

        okButton.setOnAction(event -> okButtonAction());

        serialCategoryButton.setOnAction(event -> categoryStack.getChildren().setAll(serialContentPane));
        commCategoryButton.setOnAction(event -> categoryStack.getChildren().setAll(commContentPane));
    }

    private void loadSettings(Map<SettingKey,Object> settingMap){

        settingMap.forEach((key, val) -> {

            SettingSetter settingSetter = settingNodesMap.get(key);
            if (settingSetter == null){
                return;
//                throw new NullPointerException("Could not find node setter. key="+ key );
            }

            settingSetter.set();
        });

        Integer bitDelay = getCurrentSetting(SettingKey.BIT_DELAY, Integer.class);
        syncCorrectionRangeWithDelay(bitDelay, bitDelay);
        byteCorrectionSlider.adjustValue(Settings.getInteger(SettingKey.BYTE_CORRECTION));
    }

    private void okButtonAction(){

        Map<SettingKey, Object> changedSettings = getChangedSettings();
        if (changedSettings.size() > 0) {
            Task<Integer> saveTask = new Task<Integer>() {
                @Override
                protected Integer call() throws Exception {

                    Thread.sleep(200); // for the effect of loading
                    Settings.setSettingsFromMap(changedSettings);
                    Settings.invokeChangeListeners(new ArrayList<>(changedSettings.keySet()));

                    return changedSettings.size();
                }
            };

            saveTask.runningProperty().addListener((observable, oldValue, newValue) -> updateLoadingView(newValue));

            saveTask.setOnFailed(event -> {
                Alerts.errorAlert(Windows.SETTINGS_WINDOW,
                        "Couldn't Save Settings",
                          "An error occurred while saving settings, please try again later.\n\nIf this error occurred again refer to the developer",
                                event.getSource().getException(),
                                AlertAction.OK);
            });

            saveTask.setOnSucceeded(event -> {
                Windows.SETTINGS_WINDOW.close();
            });

            new Thread(saveTask).start();
        }
        else{
            Windows.SETTINGS_WINDOW.close();
        }

    }

    private Map<SettingKey, Object> getChangedSettings(){

        ObservableMap<SettingKey, Object> changedSettings = FXCollections.observableHashMap();
        Map<SettingKey, Object> originalSettings = Settings.getSettingsMap();

        originalSettings.forEach((key, orgVal) -> {
            Supplier<Object> valueSupplier = currentSettings.get(key);

            if (valueSupplier == null){
                return;
//                throw new IllegalStateException("Settings Controller doesn't map setting key=" + key);
            }

            Object currentValue = valueSupplier.get();
            if (currentValue == null)
                throw new NullPointerException("Setting value is null. key="+ key);

            if (! currentValue.equals(orgVal))
                changedSettings.put(key, currentValue);
        });

        return changedSettings;
    }

    private void syncCorrectionRangeWithDelay(int oldDelay, int newDelay){

        int maxByteCorrection = Math.round( newDelay / 2 ) -2;
        byteCorrectionSlider.setMax(maxByteCorrection);

        double oldCorrection = byteCorrectionSlider.getValue();
        double newCorrection = (newDelay * oldCorrection) / oldDelay;
        byteCorrectionSlider.adjustValue(newCorrection);

    }

    /* ************************************* *
     *              Utils
     *
     * ************************************* */

    private void setSliderLabel(Slider slider, Label sliderLabel, String suffix){

        sliderLabel.setText(formatSliderLabel(((int) slider.getValue()), suffix));
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            sliderLabel.setText(formatSliderLabel(newValue.intValue(), suffix));
        });
    }

    private String formatSliderLabel(int value, String suffix){
        return String.format("%d %s", value, suffix);
    }

    private <T> T getCurrentSetting(SettingKey key, Class<T> clazz){
        Supplier<Object> valSupplier = getCurrentSettingsMap().get(key);
        if (valSupplier == null)
            return null;
        else
            return (T) valSupplier.get();
    }

    private Map<SettingKey, Supplier<Object>> getCurrentSettingsMap(){
        return currentSettings;
    }

    private void updateLoadingView(boolean loading){
        settingsContentContainer.setDisable(loading);
    }
}
