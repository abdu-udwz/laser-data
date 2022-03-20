package transmitter.source.connection.serial.listener;

import transmitter.source.connection.BAO;
import transmitter.source.message.in.InMessage;

public enum InMessageListenerType {

    LIGHT_READ(InMessage.LIGHT_READ_MESSAGE_PREFIX),
    BOARD_INFO (InMessage.INFO_MESSAGE_PREFIX),
    BOARD_ERROR (InMessage.ERROR_MESSAGE_PREFIX),
    UNKNOWN(null),;

    private final String pattern;

    InMessageListenerType(String pattern) {
        this.pattern = BAO.EVENT_PREFIX + pattern;
    }

    public String getPattern() {
        return pattern;
    }
}
