package transmitter.source.message;

public abstract class SerialMessage {

    protected static final String PARAMETER_SEPARATOR = "*";
    protected static final String VALUE_TERMINATOR = "&";

    private final String message;

    public SerialMessage(String message){
        this.message = message;
    }

    public String getRawMessage() {
        return message;
    }
}
