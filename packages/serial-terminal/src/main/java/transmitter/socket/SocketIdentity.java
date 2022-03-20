package transmitter.source.socket;

public enum SocketIdentity {

    ALPHA("Alpha"),
    BETA("Beta");

    public final String displayString;
    SocketIdentity(String displayString) {
        this.displayString = displayString;
    }
}
