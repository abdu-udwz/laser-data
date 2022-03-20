package transmitter.source.ui.controller.main.tab;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.json.JSONException;
import org.json.JSONObject;
import transmitter.source.connection.BAO;
import transmitter.source.connection.data.Packet;
import transmitter.source.connection.data.Packets;
import transmitter.source.connection.receiver.VirtualReceiver;
import transmitter.source.socket.SocketManager;
import transmitter.source.ui.controller.main.CommState;
import transmitter.source.util.Threading;

import java.net.Socket;

public class MessengerController extends MainTabController {

    @FXML private VBox messengerFlow;

    @FXML private HBox progressBox;
    @FXML private Label percentageLabel;
    @FXML private Label timeLabel;
    @FXML private JFXProgressBar progressBar;

    @FXML private HBox messageBox;
    @FXML private JFXTextField messageField;
    @FXML private JFXButton messageSendButton;


    @FXML
    private void initialize() {
        initComponents();
        initListeners();
        updateStateScene(CommState.STANDBY);
    }

    private void initComponents(){
//        visualizeMessage("مرحبا", true);
//        visualizeMessage("مرحبا", false);
//        visualizeMessage("hey how are you? you doing good ", false);
//        visualizeMessage("hey how are you? you doing good ", true);
    }

    private void initListeners() {

        SocketManager.addEventListener(SocketManager.EVENT_SEND_MESSAGE, args -> {
            String text = (String) args[0];
            messageField.setText(text);
            messageSendButton.fire();
        });

        // will change state to TRANSMITTING
        messageSendButton.setOnAction(event -> sendMessage());
        messageField.setOnAction(event -> sendMessage());

        stateProperty().addListener((observable, oldValue, newValue) -> {
            onStateChanged(oldValue, newValue);

        });

    }

    private void onStateChanged(CommState oldValue, CommState newValue) {
        updateStateScene(newValue);

        if (newValue == CommState.RECEIVING){
            initReceivingState();
        }

    }

    private void initReceivingState(){
        VirtualReceiver virtualReceiver = getMainController().getVirtualReceiver();

        virtualReceiver.addListener(packets -> {
            int sizeSum = 0;
            for (Packet packet : packets) {
                sizeSum += packet.getBytes().length;
            }
            String receivedMessage = Packets.toText(packets, virtualReceiver.getProtocol().getTextEncodingCharset());

            System.out.println("=== received " + packets.size() + " packet(s) with a total of " + sizeSum + " byte(s).");
            System.out.println(receivedMessage);

            SocketManager.emit(SocketManager.EVENT_MESSAGE_RECEIVED, receivedMessage);
            visualizeMessage(receivedMessage, false);
        });
    }

    private void sendMessage(){

        String messageText = messageField.getText();
        if (messageText.isEmpty())
            return;

        if (getMainControllerState() != CommState.STANDBY)
            throw new IllegalStateException("Cannot transmit message when board isn't in standby mode, current state=["+ getMainControllerState()+"]");

        Task<Boolean> transmitTask = BAO.transmitText(messageText, getMainController().getProtocol());

        transmitTask.setOnSucceeded(event -> {
            visualizeMessage(messageText, true);
            SocketManager.emit(SocketManager.EVENT_MESSAGE_SENT, messageText);
            messageField.clear();
        });
        transmitTask.setOnFailed(event1 -> {
            event1.getSource().getException().printStackTrace();
        });

        transmitTask.runningProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue)
                setMainControllerState(CommState.TRANSMITTING);
            else
                setMainControllerState(CommState.STANDBY);
        });

        transmitTask.messageProperty().addListener((observable, oldValue, newValue) -> {
            // syntax: sentBytes * totalBytes * remaining seconds
            int term1Index = newValue.indexOf('*');
            int term2Index = newValue.indexOf('&');
            long sentBytes = Long.parseLong(newValue.substring(0, term1Index).trim());
            long totalBytes = Long.parseLong(newValue.substring(term1Index + 1, term2Index).trim());
            double remainingSeconds = Double.parseDouble(newValue.substring(term2Index + 1).trim());
            updateTransmitProgress(sentBytes, totalBytes, remainingSeconds);
        });


        Threading.FIXED_POOL.submit(transmitTask);
    }

    private void updateStateScene(CommState state){
        boolean message;
        boolean progress;

        if (state == CommState.TRANSMITTING){
            message = false;
            progress = true;
        }
        else if (state == CommState.RECEIVING){
            message = false;
            progress = false;
        }
        else{
            message = true;
            progress = false;
        }
        messageBox.setDisable(! message);
        progressBox.setVisible(progress);
        progressBox.setManaged(progress);
    }

    private void visualizeMessage(String msgText, boolean sent){
        HBox container = new HBox(new Label(msgText));
        HBox message = new HBox(container);

        if (sent)
            message.setAlignment(Pos.CENTER_RIGHT);
        else
            message.setAlignment(Pos.CENTER_LEFT);

        int dir = Character.getDirectionality(msgText.charAt(0));
        if (dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC)
            container.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        else
            container.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);

        container.getStyleClass().add("container");
        message.getStyleClass().add("message");
        message.pseudoClassStateChanged(PseudoClass.getPseudoClass("sent"), sent);

        messengerFlow.getChildren().add(message);
    }

    private void updateTransmitProgress(long sentBytes, long totalBytes, double remainingSeconds){
        long percent = Math.round(progressBar.getProgress() * 100);

        String percentText = String.format("%d%% (%d bytes of %d)", percent, sentBytes, totalBytes);
        String remainingTime = String.format("%2.2f sec left", remainingSeconds);
        percentageLabel.setText(percentText);
        timeLabel.setText(remainingTime);

        JSONObject progressJson = new JSONObject();
        try {
            progressJson.put("totalBytes", totalBytes);
            progressJson.put("sentBytes", sentBytes);
            progressJson.put("remainingSeconds", remainingSeconds);
        } catch (JSONException e) {
            System.err.println("========= JSON ERROR IN TRANSMIT ==========");
            e.printStackTrace();
        }
        double progress = sentBytes/((double)totalBytes);
        progressBar.setProgress(progress);
        SocketManager.emit(SocketManager.EVENT_UPDATE_TRANSMIT_PROGRESS, progressJson);
    }

    @FXML
    private void deleteAllMessages(){
        messengerFlow.getChildren().clear();
    }
}
