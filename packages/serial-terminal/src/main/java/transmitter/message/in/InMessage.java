package transmitter.message.in;

import transmitter.message.SerialMessage;

public abstract class InMessage extends SerialMessage {

    public static final String LIGHT_READ_MESSAGE_PREFIX = "light_reading/";
    public static final String INFO_MESSAGE_PREFIX = "info/";
    public static final String ERROR_MESSAGE_PREFIX = "error/";

    public InMessage(String message) {
        super(message);
    }

}
