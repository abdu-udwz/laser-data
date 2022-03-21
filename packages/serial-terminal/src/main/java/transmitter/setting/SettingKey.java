package transmitter.setting;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import static transmitter.util.Utils.DEFAULT_PROPERTIES;

public enum SettingKey {

    BAUD_RATE("serial.baudrate", String.class),
    TRANSMITTER_PIN("serial.transmitter.pin", Integer.class),
    RECEIVER_PIN("serial.receiver.pin", Integer.class),

    SOCKET_IDENTITY("socket.identity", String.class),
    SOCKET_HOST("socket.host", String.class),

    BIT_DELAY("protocol.bit.delay", Integer.class),
    PACKET_DETERMINER("protocol.determiner", String.class),
    TEXT_ENCODING_CHARSET("protocol.text.encoding", String.class),

    PACKET_SIZE("send.packet.size", Integer.class),
    PACKET_INTERVAL("send.packet.interval", Long.class),

    LIGHT_HIGH_VALUE("receiver.light.high.value", Integer.class),
    BYTE_CORRECTION("receiver.byte.correction", Integer.class),
    ;

    public final String key;
    public final Object defaultValue;
    final Class<?> type;

    SettingKey(String defaultValueKey, Class<?> type) {

        String defaultInString = DEFAULT_PROPERTIES.getString(defaultValueKey);

        if (type == Integer.class)
            defaultValue = Integer.valueOf(defaultInString);
        else if (type == Long.class)
            defaultValue = Long.valueOf(defaultInString);
        else if (type == Float.class)
            defaultValue = Float.valueOf(defaultInString);
        else if (type == Double.class)
            defaultValue = Double.valueOf(defaultInString);
        else if (type == Boolean.class)
            defaultValue = Boolean.valueOf(defaultInString);
        else if (type == String.class)
            defaultValue = defaultInString;
        else
            throw new IllegalStateException("Unknown setting type " + type);

        this.key = name();
        this.type = type;
    }

    public static ObservableList<SettingKey> protcolKeys() {
        return FXCollections.observableArrayList(SettingKey.BIT_DELAY,
                SettingKey.PACKET_DETERMINER,
                SettingKey.TEXT_ENCODING_CHARSET,
                SettingKey.PACKET_SIZE,
                SettingKey.PACKET_INTERVAL,
                SettingKey.LIGHT_HIGH_VALUE,
                SettingKey.BYTE_CORRECTION);
    }
}
