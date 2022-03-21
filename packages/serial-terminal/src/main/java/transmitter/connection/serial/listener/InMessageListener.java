package transmitter.connection.serial.listener;

import transmitter.message.in.InMessage;

@FunctionalInterface
public interface InMessageListener<T extends InMessage> {

    void handel(T message);
}
