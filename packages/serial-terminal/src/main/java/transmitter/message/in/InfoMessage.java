package transmitter.message.in;

public class InfoMessage extends InMessage {

    public static final String SETUP_PREFIX = InMessage.INFO_MESSAGE_PREFIX + "setup/";
    public static final String TRANSMITTER_PIN = SETUP_PREFIX + "transmitter_pin";
    public static final String RECEIVER_PIN = SETUP_PREFIX + "receiver_pin";
    public static final String BIT_DELAY = SETUP_PREFIX + "bit_delay";
    public static final String INITIALIZED = SETUP_PREFIX + "initialized";

    public static final String TRANSMITTER_PREFIX = InMessage.INFO_MESSAGE_PREFIX + "trans/";
    public static final String SEND_NEXT_BINARY = TRANSMITTER_PREFIX + "send_binary";

    public static final String RECEIVER_PREFIX = InMessage.INFO_MESSAGE_PREFIX + "rece/";
    public static final String RECEIVER_ON = RECEIVER_PREFIX + "receiver_on";
    public static final String RECEIVER_STANDBY = RECEIVER_PREFIX + "receiver_standby";

    public static final String RESET_PREFIX = InMessage.INFO_MESSAGE_PREFIX + "reset/";
    public static final String TRANSMITTER_STANDBY = RESET_PREFIX + "transmitter_standby";
    public static final String DEINITIALIZED = RESET_PREFIX + "deinitialized";

    public InfoMessage(String message) {
        super(message);
    }

    public boolean ofType(String string) {
        return getRawMessage().startsWith(string);
    }

    public String getValue() {
        String rawMessage = getRawMessage();

        int startIndex = rawMessage.indexOf(PARAMETER_SEPARATOR) + 1;
        int endIndex = rawMessage.indexOf(VALUE_TERMINATOR);

        return rawMessage.substring(startIndex + endIndex);
    }

    public String getParamter() {

        String rawMessage = getRawMessage();

        int startIndex = rawMessage.lastIndexOf('/') + 1;
        int endIndex = rawMessage.indexOf(PARAMETER_SEPARATOR);

        return rawMessage.substring(startIndex + endIndex);
    }
}
