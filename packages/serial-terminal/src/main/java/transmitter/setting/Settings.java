package transmitter.source.setting;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Settings {

    private static final String SETTINGS_NODE_NAME = "LASER_DATA";
    private static final String SETTINGS_NODE_PATH = "WALK/" + SETTINGS_NODE_NAME;

    private final static Preferences prefs = Preferences.userRoot().node(SETTINGS_NODE_PATH);

    private final static ObservableList<SettingChangeListener> changeListeners = FXCollections.observableArrayList();

    public static int getInteger(SettingKey key){
        return prefs.getInt(key.key, (Integer) key.defaultValue);
    }

    public static void putInteger(SettingKey key, int val){
        prefs.putInt(key.key, val);
    }

    public static String getString(SettingKey key){
        return prefs.get(key.key, (String) key.defaultValue);
    }

    public static void putString(SettingKey key, String val){
        prefs.put(key.key, val);
    }

    public static long getLong(SettingKey key){
        return prefs.getLong(key.key, (long) key.defaultValue);
    }

    public static void putLong(SettingKey key, long val){
        prefs.putLong(key.key, val);
    }


    public static void flush() throws BackingStoreException {
        prefs.flush();
    }

    public static void invokeChangeListeners(List<SettingKey> changedKeys){
        Platform.runLater(() -> {
            for (SettingChangeListener changeListener : changeListeners) {
                changeListener.Changed(changedKeys);
            }
        });
    }
    public static void setSettingsFromMap(Map<SettingKey, Object> settingsMap) throws BackingStoreException {

        settingsMap.forEach((key, val) -> {
            if (key.type == Integer.class)
                putInteger(key, (Integer) val);
//            else if (key.type == Long.class)
//                putLong()
            else if (key.type == String.class)
                putString(key, (String) val);
            else
                throw new IllegalStateException("Unknown setting type " + key.type + " for key " + key);
        });

        flush();
    }

    public static Map<SettingKey, Object> getSettingsMap(){

        ObservableMap<SettingKey, Object> settingsMap = FXCollections.observableHashMap();
        for (SettingKey key : SettingKey.values()) {
            Object value;

            if (key.type == String.class)
                value = getString(key);
            else if (key.type == Integer.class)
                value = getInteger(key);
            else if (key.type == Long.class)
                value = getLong(key);
            else
                throw new IllegalStateException("Unknown setting type " + key.type + " for key "+ key);

            settingsMap.put(key, value);
        }

        return settingsMap;
    }

    public static void addChangeListener(SettingChangeListener listener){
        changeListeners.add(listener);
    }
}
