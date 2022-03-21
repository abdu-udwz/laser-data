package transmitter.message.out;

import transmitter.message.SerialMessage;
import transmitter.connection.BAO;

public class OutMessage extends SerialMessage {

  public static final String CUSTOM_EVENT = BAO.PROTOCOL_PREFIX + "cust/";

  public static final String SETUP_MESSAGE = "setp/";
  public static final String TRANSMITTER_MESSAGE = "tran/";
  public static final String RECEIVER_MESSAGE = "rece/";

  public OutMessage(String type, String command) {
    super(CUSTOM_EVENT + type + command);
  }

  public String message() {
    return this.message("");
  }

  public String message(Object paramter) {
    return this.getRawMessage() + "*" + paramter + "&";
  }
}
