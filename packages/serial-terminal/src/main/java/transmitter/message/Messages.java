package transmitter.message;

import transmitter.message.out.OutMessage;

public class Messages {

    public static final OutMessage TRANSMITTER_PIN = new OutMessage(OutMessage.SETUP_MESSAGE, "trans_pin");

    public static final OutMessage RECEIVER_PIN = new OutMessage(OutMessage.SETUP_MESSAGE, "recev_pin");

    public static final OutMessage BIT_DELAY = new OutMessage(OutMessage.SETUP_MESSAGE, "bit_delay");

    public static final OutMessage BOARD_INITIALIZE = new OutMessage(OutMessage.SETUP_MESSAGE, "initialize");

    public static final OutMessage T_TRANSMIT = new OutMessage(OutMessage.TRANSMITTER_MESSAGE, "transmit");

    public static final OutMessage T_STANDBY = new OutMessage(OutMessage.TRANSMITTER_MESSAGE, "standby");

    public static final OutMessage R_RECEIVE = new OutMessage(OutMessage.RECEIVER_MESSAGE, "receive");

    public static final OutMessage R_STANDBY = new OutMessage(OutMessage.RECEIVER_MESSAGE, "standby");

}
