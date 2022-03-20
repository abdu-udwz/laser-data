package transmitter.source.connection;

import com.fazecast.jSerialComm.SerialPort;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import transmitter.source.connection.data.Packet;
import transmitter.source.connection.data.Packets;
import transmitter.source.connection.protocol.LaserProtocol;
import transmitter.source.connection.serial.listener.InMessageListener;
import transmitter.source.connection.serial.listener.InMessageListenerType;
import transmitter.source.connection.serial.Serial;
import transmitter.source.message.Messages;
import transmitter.source.message.in.InfoMessage;
import transmitter.source.setting.SettingKey;
import transmitter.source.setting.Settings;
import transmitter.source.util.logging.Logging;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

public class BAO {


    public static final String PROTOCOL_PREFIX = "alp://";
    public static final String EVENT_PREFIX = PROTOCOL_PREFIX + "cevnt/";

    // ########### logging ###########
    private static final String TRANSMITTER_TERMINAL = "Transmitter";
    private static final String RECEIVER_TERMINAL = "Receiver";
    private static final String SETUP_TERMINAL = "Setup";

    // ########## connection #########
    private static final ObjectProperty<Serial> SERIAL = new SimpleObjectProperty<>(null);

    public static boolean connect(SerialPort serialPort, BaudRate baudRate) {

        Serial serial = new Serial(serialPort.getSystemPortName(), baudRate);
        serial.open();
        setSerial(serial);
        return serial.isOpen();
    }

    public static Task<Boolean> disconnect(){
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                resetBoard().run();
                serial().close();
                return true;
            }
        };
    }

    /* ######################################################################### */

    public static Task<Boolean> changeReceiverState(boolean turnOn){
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {

                CountDownLatch latch = new CountDownLatch(1);

                InMessageListener<InfoMessage> locker = message -> {
                    if ( (turnOn && message.ofType(InfoMessage.RECEIVER_ON)) ||
                            (! turnOn && message.ofType(InfoMessage.RECEIVER_STANDBY)) ) {
                        latch.countDown();

                    }
                };

                serial().addInMessageListener(InMessageListenerType.BOARD_INFO, locker);

                String message = Messages.R_RECEIVE.message();
                if (! turnOn)
                    message = Messages.R_STANDBY.message();

                boolean succeed = false;

                logReceiver(Level.INFO, String.format("Attempting to change receiver state to %s", turnOn? "On" : "Off"));
                for (int i = 0; i < 8; i++) { // try a few couple times
                    if (succeed)
                        break;

                    logReceiver(Level.INFO, String.format("...Attempt %d.", i+1));

                    serial().sendMessage(message);
                    succeed = latch.await(1500, TimeUnit.MILLISECONDS);
                }

                if (! succeed)
                    throw new TimeoutException("Board receiver state couldn't be changed to " + turnOn);
                else{
                    logReceiver(Level.FINEST, "Board sent receiver reply. Receiver stated changed successfully");
                }

                serial().removeInMessageListener(InMessageListenerType.BOARD_INFO, locker);

                return true;
            }
        };
    }

    public static Task<Boolean> transmitText(String text, LaserProtocol protocol){
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {

                List<Packet> packets = Packets.forText(text, protocol.getTextEncodingCharset());
                int byteDuration = Byte.SIZE * protocol.getBitDelay();
                Task<Boolean> transmitTask = transmitPackets(packets, byteDuration);
                transmitTask.messageProperty().addListener((observable, oldValue, newValue) -> {
                    updateMessage(transmitTask.getMessage());
                });

                transmitTask.run();

                return transmitTask.get();
            }
        };
    }

    public static Task<Boolean> transmitPackets(List<Packet> srcPackets, int byteDuration){
        return new Task<Boolean>() {

            private final List<Packet> packets = srcPackets;
            private final long packetInterval = byteDuration * 3;
            private final int totalBytesCount = Packets.countSize(packets);
            private final long totalTransmitTime = (totalBytesCount * byteDuration) + (srcPackets.size() * packetInterval);
            private long remainingTime = totalTransmitTime;

            @Override
            protected Boolean call() throws Exception {
                String logMessage = String.format("Transmitting %d packet(s).%n" +
                                                "\tEstimated time: %d ms.%n" +
                                                "\tBetween-packet interval: %d ms.",
                                                packets.size(),
                                                totalTransmitTime,
                                                packetInterval);

                logTransmitter(Level.INFO, logMessage);

                final Object lock = new Object();
                InMessageListener<InfoMessage> listener = message -> {
                    if (message.ofType(InfoMessage.SEND_NEXT_BINARY)) {
                        synchronized (lock) {
                            lock.notify();
                        }
                    }
                };

                serial().addInMessageListener(InMessageListenerType.BOARD_INFO, listener);
                int byteCount = 0;
                synchronized (lock) {

                    for (int i = 0; i < packets.size(); i++) {
                        Packet packet = packets.get(i);
                        for (String binaryString : packet.getBinaryStrings()) {

                            if (isCancelled()) {
                                throw new InterruptedException("Packet at index " + packet.index() + " was interrupted while transmitting.");
                            }

                            String transmitterMessage = Messages.T_TRANSMIT.message(binaryString);
                            serial().sendMessage(transmitterMessage);

                            lock.wait();

                            byteCount++;
                            remainingTime -= byteDuration;
                            updateTaskInfo(byteCount);
                        }
                        serial().sendMessage(Messages.T_TRANSMIT.message("end"));

                        if (i != packets.size() -1){
                            // wait for the noise after the packet..
                            Thread.sleep(packetInterval);
                        }
                        remainingTime -= packetInterval;
                    }
                }

                // next transmit task will create its own listeners
                serial().removeInMessageListener(InMessageListenerType.BOARD_INFO, listener);

                logMessage = String.format("Successfully transmitted a total size of %d bytes.",byteCount );
                logTransmitter(Level.FINEST, logMessage);
                return true;
            }

            private void updateTaskInfo(int doneCount){
                double remainingSeconds = ((double)remainingTime) / 1000;
                String message = String.format("%d * %d & %f", doneCount, totalBytesCount, remainingSeconds);
                updateMessage(message);
            }

        };
    }

    public static Task<Boolean> setupBoard(){
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {

                logSetup(Level.INFO, "Configuring on-board settings...");
                Object SETUP_LOCK = new Object();

                int transPin = Settings.getInteger(SettingKey.TRANSMITTER_PIN);
                logSetup(Level.INFO, String.format("Setting transmitter pin to %d.", transPin));
                String transMessage = Messages.TRANSMITTER_PIN.message(transPin);

                int receiverPin = Settings.getInteger(SettingKey.RECEIVER_PIN);
                logSetup(Level.INFO, String.format("Setting receiver pin to %dA.", receiverPin));
                String receMessage = Messages.RECEIVER_PIN.message(receiverPin);

                int bitDelay = Settings.getInteger(SettingKey.BIT_DELAY);
                logSetup(Level.INFO, String.format("Setting bit delay %d ms.", bitDelay));

                String bitDelayMessage = Messages.BIT_DELAY.message(bitDelay);

                String boardInitializedMessage = Messages.BOARD_INITIALIZE.message(true);

                synchronized (SETUP_LOCK){

                    InMessageListener<InfoMessage> setupListener = message -> {
                        // use synchronized block. on another thread
                        synchronized (SETUP_LOCK) {
                            if (message.ofType(InfoMessage.TRANSMITTER_PIN)) {
                                SETUP_LOCK.notify();
                            } else if (message.ofType(InfoMessage.RECEIVER_PIN)) {
                                SETUP_LOCK.notify();
                            } else if (message.ofType(InfoMessage.BIT_DELAY)) {
                                SETUP_LOCK.notify();
                            } else if (message.ofType(InfoMessage.INITIALIZED)) {
                                SETUP_LOCK.notify();
                            }
                        }
                    };

                    serial().addInMessageListener(InMessageListenerType.BOARD_INFO, setupListener);
                    checkCanceled();

                    serial().sendMessage(transMessage);
                    SETUP_LOCK.wait();

                    checkCanceled();
                    serial().sendMessage(receMessage);
                    SETUP_LOCK.wait();

                    checkCanceled();
                    serial().sendMessage(bitDelayMessage);
                    SETUP_LOCK.wait();

                    checkCanceled();
                    serial().sendMessage(boardInitializedMessage);
                    SETUP_LOCK.wait();

                    logSetup(Level.FINEST, "Successfully configured on-board settings.");
                    serial().removeInMessageListener(InMessageListenerType.BOARD_INFO, setupListener);

                }
                return true;
            }

            private void checkCanceled() throws InterruptedException {

                if (isCancelled())
                    throw new InterruptedException("Setup task was canceled");
            }
        };
    }

    public static Task<Boolean> resetBoard(){
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {

                logSetup(Level.INFO, "Resetting Board");
                Object lock = new Object();

                InMessageListener<InfoMessage> resetListener = message -> {

                    synchronized (lock){
                        if (message.ofType(InfoMessage.TRANSMITTER_STANDBY) ||
                                message.ofType(InfoMessage.RECEIVER_STANDBY) ||
                                    message.ofType(InfoMessage.DEINITIALIZED))
                            lock.notify();
                    }
                };

                serial().addInMessageListener(InMessageListenerType.BOARD_INFO, resetListener);

                String transStandby = Messages.T_STANDBY.message();
                String receStandby = Messages.R_STANDBY.message();
                String deinitialize = Messages.BOARD_INITIALIZE.message(false);

                synchronized (lock) {
                    serial().sendMessage(transStandby);
                    lock.wait();
                    serial().sendMessage(receStandby);
                    lock.wait();
                    serial().sendMessage(deinitialize);

                }
                serial().removeInMessageListener(InMessageListenerType.BOARD_INFO, resetListener);
                return true;
            }
        };
    }

    public static Serial serial() {
        if (SERIAL.get() == null){
            throw new IllegalStateException("There's no connection to board, connect first");
        }

        return SERIAL.get();
    }

    public static ObjectProperty<Serial> serialProperty() {
        return SERIAL;
    }

    public static void setSerial(Serial serial) {
        BAO.SERIAL.set(serial);
    }


    private static void logTransmitter(Level level, String message){
        Logging.logTerminal(TRANSMITTER_TERMINAL, level, message);
    }

    private static void logReceiver(Level level, String message) {
        Logging.logTerminal(RECEIVER_TERMINAL, level, message);
    }

    private static void logSetup(Level level, String message) {
        Logging.logTerminal(SETUP_TERMINAL, level, message);
    }
}
