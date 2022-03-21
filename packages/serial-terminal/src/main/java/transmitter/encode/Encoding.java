package transmitter.encode;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Encoding {

    public static ObservableList<Byte> encodeString(final String string, Charset charset) {
        return toByteList(string.getBytes(charset));
    }

    public static ObservableList<Byte> encodeShort(final Short num) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putShort(num);
        return toByteList(buffer.array());
    }

    public static ObservableList<String> toBinaryStrings(List<Byte> byteList) {
        return toBinaryStrings(toByteArray(byteList));
    }

    public static ObservableList<String> toBinaryStrings(byte[] byteArray) {
        ObservableList<String> binaryStrings = FXCollections.observableArrayList();

        for (Byte b : byteArray) {
            String origBitString = Integer.toUnsignedString(b, 2);
            StringBuilder bitStringBuilder = new StringBuilder(origBitString);

            // fill remaining bits with 0s..
            int byteSize = Byte.SIZE;
            if (bitStringBuilder.length() < byteSize) {

                int diff = 8 - bitStringBuilder.length();
                for (int i = 0; i < diff; i++) {
                    bitStringBuilder.insert(0, '0');
                }
            } else if (bitStringBuilder.length() > byteSize) {
                int diff = bitStringBuilder.length() - byteSize;
                bitStringBuilder.delete(0, diff);
            }

            binaryStrings.add(bitStringBuilder.toString());
        }

        return binaryStrings;
    }

    public static List<Byte> listFromBinaryStrings(List<String> binaryStrings) {
        ArrayList<Byte> bytes = new ArrayList<>();

        for (String binaryString : binaryStrings) {
            int parsed = Integer.parseUnsignedInt(binaryString, 2);
            bytes.add((byte) parsed);
        }

        return bytes;
    }

    public static byte[] toByteArray(List<Byte> list) {

        byte[] array = new byte[list.size()];

        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public static ObservableList<Byte> toByteList(byte[] array) {
        ObservableList<Byte> list = FXCollections.observableArrayList();
        for (byte b : array) {
            list.add(b);
        }
        return list;
    }

    public static String decodeString(final List<Byte> bytes, Charset charset) {
        return new String(toByteArray(bytes), charset);
    }

    public static Short decodeShort(byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        return buffer.getShort();
    }

}
