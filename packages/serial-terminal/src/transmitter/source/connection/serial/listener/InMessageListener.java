package transmitter.source.connection.serial.listener;

import transmitter.source.message.in.InMessage;

@FunctionalInterface
public interface InMessageListener<T extends InMessage> {

    void handel(T message);
}
