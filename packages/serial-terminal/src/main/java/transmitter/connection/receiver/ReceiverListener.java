package transmitter.connection.receiver;

import transmitter.connection.data.Packet;

import java.util.List;

@FunctionalInterface
public interface ReceiverListener {

    void listen(List<Packet> packets);
}
