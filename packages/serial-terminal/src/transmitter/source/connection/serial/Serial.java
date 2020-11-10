package transmitter.source.connection.serial;

import com.fazecast.jSerialComm.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import transmitter.source.connection.BAO;
import transmitter.source.connection.BaudRate;
import transmitter.source.connection.serial.listener.InMessageListener;
import transmitter.source.connection.serial.listener.InMessageListenerType;
import transmitter.source.message.in.InMessage;
import transmitter.source.message.in.InfoMessage;
import transmitter.source.message.in.LightReadingMessage;
import transmitter.source.message.in.error.ErrorMessage;
import transmitter.source.util.Threading;

public class Serial{

    private final SerialPort serialPort;

    private final ObservableList<InMessageListener<LightReadingMessage>> lightReadListeners =
                FXCollections.observableArrayList();

    private final ObservableList<InMessageListener<InfoMessage>> infoListeners =
                FXCollections.observableArrayList();

    private final ObservableList<InMessageListener<ErrorMessage>> errorListeners =
                FXCollections.observableArrayList();

    private final BooleanProperty open = new SimpleBooleanProperty(false);

    public Serial(String serialPortName, BaudRate baudRate) {

        this.serialPort = SerialPort.getCommPort(serialPortName);
        serialPort.setBaudRate(baudRate.val);

        initListeners();
    }

    private void initListeners(){

        openProperty().addListener((observable, oldValue, open) -> {

            if (open){
                serialPort.openPort();

                serialPort.addDataListener(new SerialPortMessageListener() {

                    @Override
                    public byte[] getMessageDelimiter() {
                        return "&\n".getBytes();
                    }

                    @Override
                    public boolean delimiterIndicatesEndOfMessage() {
                        return true;
                    }

                    @Override
                    public int getListeningEvents() {
                        return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
                    }

                    @Override
                    public void serialEvent(SerialPortEvent event) {
                        onDataReceived(event.getReceivedData());
                    }
                });
            }
            else {
                serialPort.removeDataListener();
                serialPort.closePort();
            }
        });
    }

    private void onDataReceived(byte[] data){

        Runnable dataProcess = new Runnable() {

            StringBuilder messageBuilder = new StringBuilder();
//
            @Override
            public void run() {
                String receivedString = new String(data);

                messageBuilder.append(receivedString);
                String[] messages = messageBuilder.toString().split("\n");

                for (String message : messages) {

                    int messageLength = message.length();

                    if (messageLength > BAO.EVENT_PREFIX.length() && message.endsWith("&")){
                        callInMessageListeners(message);
                        messageBuilder.delete(0, messageLength + 1);
                    }
                    else if (messageLength <= BAO.EVENT_PREFIX.length() && message.endsWith("&")){

                        System.out.println("...clearing noise in buffer ["+messageBuilder+"]");
                        messageBuilder.delete(0, messageLength + 1);
                        serialPort.clearBreak();
                    }

                }
            }
        };

        Threading.SERIAL_EVENTS_POOL.submit(dataProcess);
    }

    private void callInMessageListeners(final String message){

        InMessageListenerType toCallType = listenerTypeForMessage(message);
        String eventMessage = message.substring(BAO.EVENT_PREFIX.length());

        if (toCallType == InMessageListenerType.BOARD_ERROR) {
            System.out.println(message);
        }

        if (toCallType == InMessageListenerType.BOARD_ERROR){
            for (InMessageListener<ErrorMessage> listener : errorListeners) {
                listener.handel(new ErrorMessage(eventMessage));
            }
        }
        else if (toCallType == InMessageListenerType.BOARD_INFO){
            for (InMessageListener<InfoMessage> listener : infoListeners) {
                listener.handel(new InfoMessage(eventMessage));
            }
        }
        else if (toCallType == InMessageListenerType.LIGHT_READ){
//            System.out.println(message);
//            System.out.println("[Serial.java] " + message);
            for (InMessageListener<LightReadingMessage> listener : lightReadListeners) {
                listener.handel(new LightReadingMessage(eventMessage));
            }
        }
        else {
            System.err.println("Couldn't determine listener for message: " + message);
        }
    }

    public void sendMessage(String message) throws SerialPortTimeoutException {
        checkPortOpen();

        byte[] bytes = message.getBytes();
        serialPort.writeBytes(bytes, bytes.length);
    }

    private int prevStart = 0;
    private int prevEnd = 0;
    private void transmitterTimeBugTest(InMessageListenerType toCallType, String eventMessage){

        if (toCallType == InMessageListenerType.BOARD_INFO){

            boolean isStart = false;

            if (eventMessage.contains("st_") || eventMessage.contains("et_")){
                isStart = eventMessage.contains("st_");

                int startIndex = eventMessage.indexOf('*') + 1;
                int endIndex = eventMessage.indexOf('&');

                int timestamp = Integer.parseInt(eventMessage.substring(startIndex, endIndex));

                if (isStart) {

                    if (prevStart != 0) {

                        if (prevEnd != 0){
                            System.out.println("time since last byte end: " + (timestamp - prevEnd));
                        }
                    }
                    prevStart = timestamp;
                }
                else{

                    if (prevStart != 0){
                        System.out.println("time since i started: " + (timestamp - prevStart));
                    }

                    prevEnd = timestamp;
                }
            }

        }
    }

    /* ********************************************************** *
     *                                                            *
     *                            Utils                           *
     *                                                            *
     * ********************************************************** */

    private InMessageListenerType listenerTypeForMessage(String message){

        for (InMessageListenerType value : InMessageListenerType.values()) {
            if (message.contains(value.getPattern()))
                return value;
        }

        return InMessageListenerType.UNKNOWN;
    }

    private void checkPortOpen(){
        if (! serialPort.isOpen())
            throw new IllegalStateException("Serial port is not open, cannot write/read.");
    }


    public void addInMessageListener(InMessageListenerType listenerType,
                                     InMessageListener<? extends InMessage> listener){
        if (listenerType == InMessageListenerType.BOARD_ERROR)
            errorListeners.add((InMessageListener<ErrorMessage>) listener);

        else if (listenerType == InMessageListenerType.BOARD_INFO)
            infoListeners.add((InMessageListener<InfoMessage>) listener);

        else if (listenerType == InMessageListenerType.LIGHT_READ)
            lightReadListeners.add((InMessageListener<LightReadingMessage>) listener);
        else
            throw new IllegalStateException("Unknown listener type, cant interpret listener.");

    }

    public void removeInMessageListener(InMessageListenerType listenerType,
                                        InMessageListener<? extends InMessage> listener){
        if (listenerType == InMessageListenerType.BOARD_ERROR)
            errorListeners.removeIf(listener1 -> listener1 == listener);

        else if (listenerType == InMessageListenerType.BOARD_INFO){
            infoListeners.removeIf(listener1 -> listener1 == listener);
        }

        else if (listenerType == InMessageListenerType.LIGHT_READ)
            lightReadListeners.removeIf(listener1 -> listener1 == listener);


    }

    /* ********************************************************** *
     *                  setter & getters                          *
     * ********************************************************** */

    public boolean isOpen() {
        return serialPort.isOpen();
    }

    public BooleanProperty openProperty() {
        return open;
    }

    public void open() {
        this.open.set(true);
    }

    public void close() {
        open.set(false);
    }

    public int getBaudRate() {
        return serialPort.getBaudRate();
    }

    public int getReadTimeout() {
        return serialPort.getReadTimeout();
    }

    public int getWriteTimeout() {
        return serialPort.getWriteTimeout();
    }

    public String getSystemPortName() {
        return serialPort.getSystemPortName();
    }

    public String getDescriptivePortName() {
        return serialPort.getDescriptivePortName();
    }

    public String getPortDescription() {
        return serialPort.getPortDescription();
    }

    public boolean setRTS() {
        return serialPort.setRTS();
    }

    public boolean clearRTS() {
        return serialPort.clearRTS();
    }

    public SerialPort getNativeSerial(){
        return this.serialPort;
    }
}
