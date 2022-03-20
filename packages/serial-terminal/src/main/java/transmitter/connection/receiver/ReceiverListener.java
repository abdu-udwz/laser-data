package transmitter.source.connection.receiver;

import transmitter.source.connection.data.Packet;

import java.util.List;

@FunctionalInterface
public interface ReceiverListener {

    void listen(List<Packet> packets);
}
