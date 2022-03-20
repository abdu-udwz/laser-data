package transmitter.source.connection.data;

import javafx.collections.ObservableList;
import transmitter.source.encode.Determiner;
import transmitter.source.encode.Encoding;
import transmitter.source.setting.SettingKey;
import transmitter.source.setting.Settings;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public final class Packets {


    private Packets(){

    }

    public static String toText(List<Packet> packets, Charset charset){

        List<Byte> totalBytes = new ArrayList<>();
        for (Packet packet : packets) {
            totalBytes.addAll(Encoding.toByteList(packet.getData()));
        }
        return Encoding.decodeString(totalBytes, charset);
    }

    public static List<Packet> forText(String text, Charset charset){

        ArrayList<Packet> packets = new ArrayList<>();

        Determiner determiner = Determiner.valueOf(Settings.getString(SettingKey.PACKET_DETERMINER));
        ObservableList<Byte> determinerBinary = Encoding.encodeString(determiner.string, charset);
        ObservableList<Byte> dataBinary = Encoding.encodeString(text, charset);

        // special bytes are
        //  - start line        (x2)
        //  - determiner binary (x2)
        //  - finish line
        //  - indexing: 4 bytes

        int specialBytesSize = 7 + (2 * determinerBinary.size());
        int definedPacketSize = Settings.getInteger(SettingKey.PACKET_SIZE);
        System.out.println("=== Defined packet size: " + definedPacketSize + " byte(s) ===");

        int totalDataSize = dataBinary.size();
        System.out.println("=== data size: " + totalDataSize + " ===");
        int packetCapacity = definedPacketSize - specialBytesSize;
        int frameSize = (int) Math.ceil((double)totalDataSize / packetCapacity);
        System.out.println("=== Packet capacity: " + packetCapacity + " ===");

        for (int i = 0; i < frameSize; i++) {

            int currentDataSize = dataBinary.size();
            List<Byte> packetData;
            int dataEndIndex;

            if (packetCapacity < currentDataSize) {
                dataEndIndex = packetCapacity;
            }
            else
                dataEndIndex = currentDataSize;

            packetData = new ArrayList<>(dataBinary.subList(0, dataEndIndex));
            dataBinary.remove(0, dataEndIndex);


            Packet packet = Packet.getTransmitInstance(Encoding.toByteArray(determinerBinary),
                                                       Encoding.toByteArray(packetData),
                                                       i, frameSize);
            packets.add(packet);

        }
        return packets;
    }


    public static int countSize(List<Packet> packets){
        int count = 0;
        for (Packet packet : packets) {
            count += packet.size();
        }
        return count;
    }

}
