package transmitter.source.message.in;

public class LightReadingMessage extends InMessage {

    private final long timestamp;
    private final int light;

    public LightReadingMessage(String message) {
        super(message);

        int timeStartIndex = message.lastIndexOf('/') + 1;
        int timeTerminator = message.indexOf('*');
        int lightTerminator = message.indexOf('&');

        timestamp = Long.valueOf(message.substring(timeStartIndex, timeTerminator));
        light = Integer.valueOf(message.substring(timeTerminator + 1, lightTerminator));
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getLight() {
        return light;
    }
}
