package transmitter.connection.receiver;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import transmitter.connection.data.Packet;
import transmitter.connection.protocol.LaserProtocol;
import transmitter.encode.Determiner;
import transmitter.encode.Encoding;
import transmitter.message.in.LightReadingMessage;
import transmitter.util.Threading;
import transmitter.util.logging.terminal.TerminalLoggable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class VirtualReceiver implements TerminalLoggable {

    private static final boolean PRINT_BIT_READING_DETAILS = false;

    private static final String TERMINAL_HEAD = "V Receiver";

    private final ObservableList<Byte> binary = FXCollections.observableArrayList();

    private final ObjectProperty<LightReadingMessage> lightReading = new SimpleObjectProperty<LightReadingMessage>(
            null);
    private final StringProperty bitBuffer = new SimpleStringProperty(null);
    private final BooleanProperty receiving = new SimpleBooleanProperty(false);
    private final BooleanProperty headerFound = new SimpleBooleanProperty(false);

    private final LaserProtocol protocol;
    private final ObservableList<Byte> determinerBinary = FXCollections.observableArrayList();

    private final ObservableList<Packet> receivedPackets = FXCollections.observableArrayList();
    private final ObservableList<ReceiverListener> listeners = FXCollections.observableArrayList();
    private final ObjectProperty<ReceiverState> state = new SimpleObjectProperty<>(ReceiverState.STANDBY);
    private final BooleanProperty lossFlag = new SimpleBooleanProperty(false);

    public VirtualReceiver(LaserProtocol protocol) {
        this.protocol = protocol;
        determinerBinary.add((byte) Integer.parseUnsignedInt(Determiner.START_LINE_BINARY, 2));
        ObservableList<Byte> determinerBytes = Encoding.encodeString(protocol.getPacketDeterminer().string,
                protocol.getTextEncodingCharset());
        determinerBinary.addAll(determinerBytes);

        System.out.println("============ determiner binary ============");
        for (String s : Encoding.toBinaryStrings(determinerBinary)) {
            System.out.println(s);
        }
        System.out.println("===========================================");
        initListeners();
    }

    private void initListeners() {

        lightReading.addListener((observable, oldValue, newValue) -> {
            onLightReadingChanged(oldValue, newValue);
        });

    }

    private long byteStartTime = 0;

    private void onLightReadingChanged(LightReadingMessage prevReading, LightReadingMessage curReading) {

        if (curReading == null)
            return;

        if (byteStartTime == 0) {

            if (detectSignal(curReading.getLight())) {
                setState(ReceiverState.RECEIVING_NOHEADER);
                // gonna check if the header is found after detecting the signal
                long byteDelay = Byte.SIZE * getProtocol().getBitDelay();
                long headerTimeOut = (determinerBinary.size() * byteDelay) + byteDelay;

                Threading.SCHEDULED_POOL.schedule(() -> {
                    // when there's a noise or
                    // some kind of non-sense -> reset
                    if (!isHeaderFound() && binary.size() > 0) {

                        logTerminal(Level.WARNING, "Found no header, resting..");

                        if (getReceivedPackets().size() > 0) {
                            // data loss occurred
                            Platform.runLater(() -> {
                                setLossFlag(true);
                                setLossFlag(false);
                            });
                        }

                        reset();
                    }
                }, headerTimeOut, TimeUnit.MILLISECONDS);

                byteStartTime = curReading.getTimestamp() - 1;
                setBitBuffer("");
                logTerminal(Level.INFO,
                        String.format("Detected signal at: %d value: %d.", byteStartTime, curReading.getLight()));
            }
        }

        if (byteStartTime != 0) {
            String buffer = getBitBuffer();

            if (buffer.length() == Byte.SIZE) {

                String bitBuffer = getBitBuffer();
                System.out.println(bitBuffer);

                try {
                    binary.add((byte) Integer.parseUnsignedInt(getBitBuffer(), 2));
                } catch (NumberFormatException ex) {
                }

                byteStartTime += Byte.SIZE * getProtocol().getBitDelay();
                setBitBuffer("");

                if (bitBuffer.equals(Determiner.FINISH_LINE_BINARY) && checkFooter()) {

                    setState(ReceiverState.STANDBY);
                    List<Byte> packetBinary = new ArrayList<>(binary);
                    packetBinary.remove(packetBinary.size() - 1); // remove the finish line byte

                    int dataStartIndex = determinerBinary.size();
                    int dataEndIndex = binary.size() - determinerBinary.size() - 5;

                    Packet packet = Packet.getReceiverInstance(Encoding.toByteArray(packetBinary), dataStartIndex,
                            dataEndIndex);
                    receivedPackets.add(packet);

                    logTerminal(Level.FINEST, String.format("===[Virtual Receiver] received packet [%d from %d].%n",
                            packet.index() + 1, (packet.frameSize())));
                    if (packet.index() == packet.frameSize() - 1) {
                        for (ReceiverListener listener : listeners) {
                            Platform.runLater(() -> {
                                listener.listen(new ArrayList<>(receivedPackets));
                                receivedPackets.clear();
                            });
                        }
                    }

                    reset();
                }

                if (!isHeaderFound()) {
                    int binaryStringsSize = binary.size();
                    int determinerSize = determinerBinary.size();

                    if (binaryStringsSize == determinerSize) {
                        boolean isDeterminer = isPacketDeterminer(binary);
                        setState(ReceiverState.RECEIVING);
                        setHeaderFound(isDeterminer);

                        logTerminal(Level.INFO, "Header found? " + isHeaderFound());
                    } else if (binaryStringsSize > determinerSize) {
                        byteStartTime = 0;
                        binary.clear();
                    }
                }
            }

            updateBinaryBuffer(prevReading);
        }
    }

    private int bitOrder = 1;
    private ObservableList<LightReadingMessage> bitReadings = FXCollections.observableArrayList();

    private void updateBinaryBuffer(LightReadingMessage previousReading) {
        // this method is in a loop-like invocation

        int bufferLength = getBitBuffer().length();
        long currentBitStartTime = byteStartTime + (bufferLength * getProtocol().getBitDelay());

        LightReadingMessage currentReading = getLightReading();
        long currentTimestamp = currentReading.getTimestamp();

        if (currentTimestamp >= currentBitStartTime &&
                currentTimestamp <= currentBitStartTime + getProtocol().getBitDelay()) {

            if (previousReading != null &&
                    currentTimestamp == previousReading.getTimestamp())
                return;

            bitReadings.add(currentReading);
        }

        else if (currentTimestamp > currentBitStartTime) {

            boolean currentState = false;

            try {
                currentState = isHighBit(bitReadings);
            } catch (IllegalArgumentException ex) {
                if (isHeaderFound())
                    throw ex;
                else
                    reset();
            }
            appendBitToBuffer(currentState);

            bitReadings.clear();

            bitReadings.add(currentReading);
        }
    }

    private boolean previousBitState = true;

    private boolean isHighBit(final List<LightReadingMessage> bitReadings) {

        int byteCorrection = protocol.getByteCorrection();

        if (bitReadings.size() < 2) {
            throw new IllegalArgumentException(
                    "Too few bit light readings, got " + bitReadings.size() + " expected at least " + 2);
        }

        // cannot modify passed-by-reference list
        int startIndex = byteCorrection;
        if (byteCorrection > 0)
            startIndex = byteCorrection - 1;

        int endIndex = bitReadings.size() - byteCorrection;

        List<LightReadingMessage> reliableReadings = bitReadings.subList(startIndex, endIndex);

        int startLight = reliableReadings.get(0).getLight();
        int endLight = reliableReadings.get(reliableReadings.size() - 1).getLight();

        // increase 6
        // decrease 20
        int reasonableIncrease = 20;
        int reasonableDecrease = 6;

        boolean increase = (endLight - startLight) > 0;
        boolean decrease = (endLight - startLight) < 0;
        int absDiff = Math.abs(endLight - startLight);

        boolean state;

        if (increase) {

            if (endLight >= protocol.getHighLightValue()) {
                state = true;
            } else {
                state = previousBitState;
            }
        } else if (decrease) {
            if (absDiff >= reasonableDecrease)
                state = false;

            else { // slight decrease
                state = (endLight >= protocol.getHighLightValue());
            }
        } else { // in case start == end
            state = (endLight >= protocol.getHighLightValue());
        }

        this.previousBitState = state;

        if (PRINT_BIT_READING_DETAILS) {
            if (bitReadings.size() >= 1) {
                String formattedBitDetails = String.format(
                        "[%02d]current: %03d - %03d (%03d) [%b] \t finished at: %07d%n",
                        bitOrder,
                        startLight,
                        endLight,
                        absDiff,
                        state,
                        getLightReading().getTimestamp());

                logTerminal(Level.INFO, formattedBitDetails);
            }
        }

        return state;
    }

    private void appendBitToBuffer(boolean state) {
        char append = state ? '1' : '0';
        setBitBuffer(getBitBuffer() + append);
        bitOrder++;
    }

    private int lowestLightDetected = 0;

    private boolean detectSignal(int light) {

        if (light >= protocol.getHighLightValue() - 90
                && (light - lowestLightDetected) >= 11)
            return true;

        lowestLightDetected = light;
        return false;
    }

    private boolean checkFooter() {

        int bytesSize = binary.size();
        int determinerSize = determinerBinary.size();

        // at least the received binary must be double
        // the size of the header in order to find the footer...
        if (bytesSize < (determinerSize * 2))
            return false;

        if (bytesSize >= (determinerSize * 2)) {

            int footerStartIndex = (bytesSize - (determinerSize + 1));
            int footerEndIndex = bytesSize - 1; // 1 for the finish line
            List<Byte> possibleFooter = binary.subList(footerStartIndex, footerEndIndex);

            return isPacketDeterminer(possibleFooter);
        }

        return false;
    }

    private boolean isPacketDeterminer(final List<Byte> binaryStrings) {
        return binaryStrings.equals(determinerBinary);
    }

    public void reset() {

        setState(ReceiverState.STANDBY);
        setBitBuffer("");
        setHeaderFound(false);

        lowestLightDetected = 0;

        byteStartTime = 0;
        binary.clear();

        previousBitState = true;
        bitReadings.clear();
        bitOrder = 1;

    }

    /*
     * ******************************************** *
     * setters & getters *
     *
     * ********************************************
     */

    public boolean isReceiving() {
        return receiving.get();
    }

    public BooleanProperty receivingProperty() {
        return receiving;
    }

    public void setReceiving(boolean receiving) {
        this.receiving.set(receiving);
    }

    public String getBitBuffer() {
        return bitBuffer.get();
    }

    public StringProperty bitBufferProperty() {
        return bitBuffer;
    }

    private void setBitBuffer(String byteBuffer) {
        this.bitBuffer.set(byteBuffer);
    }

    public LightReadingMessage getLightReading() {
        return lightReading.get();
    }

    public ObjectProperty<LightReadingMessage> lightReadingProperty() {
        return lightReading;
    }

    public void setLightReading(LightReadingMessage lightReading) {
        this.lightReading.set(lightReading);
    }

    public boolean isHeaderFound() {
        return headerFound.get();
    }

    public void setHeaderFound(boolean headerFound) {
        this.headerFound.set(headerFound);
    }

    public LaserProtocol getProtocol() {
        return protocol;
    }

    public ObservableList<Packet> getReceivedPackets() {
        return this.receivedPackets;
    }

    public ObservableList<ReceiverListener> getListeners() {
        return this.listeners;
    }

    public void addListener(ReceiverListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(ReceiverListener listener) {
        this.listeners.remove(listener);
    }

    public ReceiverState getState() {
        return state.get();
    }

    public ReadOnlyObjectProperty<ReceiverState> stateProperty() {
        return state;
    }

    private void setState(ReceiverState state) {
        this.state.set(state);
    }

    public boolean getLossFlag() {
        return lossFlag.get();
    }

    public ReadOnlyBooleanProperty lossFlagProperty() {
        return lossFlag;
    }

    private void setLossFlag(boolean lossFlag) {
        this.lossFlag.set(lossFlag);
    }

    @Override
    public String getTerminalHead() {
        return TERMINAL_HEAD;
    }
}