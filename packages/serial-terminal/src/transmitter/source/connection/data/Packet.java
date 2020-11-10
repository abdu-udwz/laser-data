package transmitter.source.connection.data;

import javafx.collections.ObservableList;
import jdk.nashorn.internal.ir.EmptyNode;
import transmitter.source.encode.Determiner;
import transmitter.source.encode.Encoding;

import java.util.Arrays;

public class Packet {

    private final int size;
    private final int index;
    private final int frameSize;

    private final byte[] bytes;

    public final int dataStartIndex;
    public final int dataEndIndex;

    private Packet(byte[] bytes, int index, int frameSize,int dataStartIndex, int dataEndIndex){
        this.size = bytes.length;
        this.bytes = bytes;
        this.index = index;
        this.frameSize = frameSize;

        this.dataStartIndex = dataStartIndex;
        this.dataEndIndex = dataEndIndex;
    }

    public static Packet getTransmitInstance(byte[] determiner, byte[] data, int index, int frameSize){

        // packet consist of:
        // start line: 2x (1 byte)
        // determiner: 2x (unknown)
        // data: 1x (unknown)
        // indexing: 2x (2 byte)
        // finish line: 1x (1 byte)

        byte startLine = (byte) Integer.parseUnsignedInt(Determiner.START_LINE_BINARY, 2);
        byte finishLine = (byte) Integer.parseUnsignedInt(Determiner.FINISH_LINE_BINARY, 2);

        int size = (2 * determiner.length) + 7 + data.length;
        byte[] bytes = new byte[size];

        int currentIndex = 0;
        bytes[currentIndex] = startLine;
        currentIndex++;

        for (int i = 0; i < determiner.length; i++, currentIndex++) {
            bytes[currentIndex] = determiner[i];
        }

        int dataStartIndex = currentIndex;
        int dataEndIndex = currentIndex + data.length;
        for (int i = 0; i < data.length; i++, currentIndex++) {
            bytes[currentIndex] = data[i];
        }

        byte[] indexBinary = Encoding.toByteArray(Encoding.encodeShort((short) index));

        for (int i = 0; i < indexBinary.length; i++, currentIndex++) {
            bytes[currentIndex] = indexBinary[i];
        }

        byte[] frameBinary = Encoding.toByteArray(Encoding.encodeShort((short) frameSize));
        for (int i = 0; i < frameBinary.length; i++, currentIndex++) {
            bytes[currentIndex] = frameBinary[i];
        }

        bytes[currentIndex] = startLine;
        currentIndex++;

        for (int i = 0; i < determiner.length; i++, currentIndex++) {
            bytes[currentIndex] = determiner[i];
        }
        bytes[currentIndex] = finishLine;

        return new Packet(bytes, index, frameSize, dataStartIndex, dataEndIndex);
    }

    public static Packet getReceiverInstance(byte[] binary, int dataStartIndex, int dataEndIndex){

        int indexStart = dataEndIndex;
        int indexEnd = indexStart + 1;
        Short index = Encoding.decodeShort(Arrays.copyOfRange(binary, indexStart, indexEnd + 1));

        int frameSizeStart = indexEnd + 1;
        int frameSizeEnd = frameSizeStart + 1;
        Short frameSize = Encoding.decodeShort(Arrays.copyOfRange(binary, frameSizeStart, frameSizeEnd + 1));

        return new Packet(binary,index, frameSize, dataStartIndex, dataEndIndex);
    }

    public int size(){
        return size;
    }

    public int dataSize(){
        return dataEndIndex -dataStartIndex;
    }

    public int index(){
        return index;
    }

    public int frameSize(){
        return frameSize;
    }

    public byte[] getData(){
        return Arrays.copyOfRange(bytes, dataStartIndex, dataEndIndex);
    }

    public byte[] getBytes(){
        return bytes;
    }

    public ObservableList<String> getBinaryStrings(){
        return Encoding.toBinaryStrings(bytes);
    }
}
