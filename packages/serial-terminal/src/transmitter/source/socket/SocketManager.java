package transmitter.source.socket;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import transmitter.source.util.logging.Logging;

import java.net.URISyntaxException;
import java.util.concurrent.Callable;
import java.util.logging.Level;

public abstract class SocketManager {

    public static final String EVENT_SET_STATE = "STATE_set";

    public static final String EVENT_SEND_MESSAGE = "TRANSMIT_sendMessage";
    public static final String EVENT_MESSAGE_SENT = "TRANSMIT_messageSent";
    public static final String EVENT_UPDATE_TRANSMIT_PROGRESS = "TRANSMIT_updateProgress";

    public static final String EVENT_TOGGLE_RECEIVER = "RECEIVE_toggle";
    public static final String EVENT_RECEIVER_TOGGLE_ERROR = "RECEIVE_toggleError";
    public static final String EVENT_MESSAGE_RECEIVED = "RECEIVE_messageReceived";

    private static Socket socket;
    private static final BooleanProperty connected = new SimpleBooleanProperty(false);

    public static void connect(SocketIdentity identity, String host) throws URISyntaxException {
        socket = IO.socket(host);

        socket.on(Socket.EVENT_CONNECT, args -> {
            logTerminal(Level.INFO, String.format("Connected successfully to [%s] as %s", host, identity.name()));
            Platform.runLater(() -> setConnected(true));
            socket.emit("signIn", identity.name(), "TRANSCEIVER");
        });

        socket.open();
    }

    public static void disconnect() {
        if (socket == null || !isConnected()) {
            return;
        }
        socket.disconnect();
        socket.close();
        socket = null;
        Platform.runLater(() -> setConnected(false));
        logTerminal(Level.INFO, "Disconnected.");
    }

    public static void emit(String event, Object... args) {
        socket.emit(event, args);
    }

    public static void addEventListener(String event, Emitter.Listener listener){
        if (socket == null){
            throw new IllegalStateException("Socket is not initialized. Cannot register event listener");
        }
        socket.on(event, listener);
    }

    private static void logTerminal(Level level, String message) {
        Logging.logTerminal("Socket", level, message);
    }

    public static void setOnDisconnected(Runnable listener){
        socket.on(Socket.EVENT_DISCONNECT, args -> listener.run());
    }

    // ==================================================== //
    //                  setters && getters                  //
    // ==================================================== //
    public static boolean isConnected() {
        return connected.get();
    }

    private static void setConnected(boolean connected) {
        SocketManager.connected.set(connected);
    }

    public static ReadOnlyBooleanProperty connectedProperty() {
        return connected;
    }
}
