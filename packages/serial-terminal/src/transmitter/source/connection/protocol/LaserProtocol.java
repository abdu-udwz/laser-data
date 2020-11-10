package transmitter.source.connection.protocol;

import transmitter.source.encode.Determiner;
import transmitter.source.setting.SettingKey;
import transmitter.source.setting.Settings;

import java.nio.charset.Charset;

public class LaserProtocol {
     // this class is immutable and can be used with different threads and objects at the same time;

    // trans & receiver
    private final int bitDelay;
    private final Determiner packetDeterminer;
    private final Charset textEncodingCharset;

    // transmitter specific only
    private final int packetSize;

    // receiver specific only
    private final int highLightValue;
    private final int byteCorrection;

    public static LaserProtocol getFromSettings(){
        return new LaserProtocol(Settings.getInteger(SettingKey.BIT_DELAY),
                                Determiner.valueOf(Settings.getString(SettingKey.PACKET_DETERMINER)),
                                Charset.forName(Settings.getString(SettingKey.TEXT_ENCODING_CHARSET)),
                                Settings.getInteger(SettingKey.PACKET_SIZE),
                                Settings.getInteger(SettingKey.LIGHT_HIGH_VALUE),
                                Settings.getInteger(SettingKey.BYTE_CORRECTION)
        );
    }

    private LaserProtocol(int bitDelay,
                          Determiner packetDeterminer,
                          Charset textEncodingCharset,
                          int packetSize,
                          int highLightValue, int byteCorrection){

        this.bitDelay = bitDelay;

        this.packetDeterminer = packetDeterminer;
        this.textEncodingCharset = textEncodingCharset;
        this.packetSize = packetSize;

        this.highLightValue = highLightValue;
        this.byteCorrection = byteCorrection;

    }

    public int getBitDelay() {
        return bitDelay;
    }

    public Determiner getPacketDeterminer() {
        return packetDeterminer;
    }

    public Charset getTextEncodingCharset() {
        return textEncodingCharset;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public int getByteCorrection() {
        return byteCorrection;
    }

    public int getHighLightValue() {
        return highLightValue;
    }
}
