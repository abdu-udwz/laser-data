package transmitter.setting;

import java.util.List;

public interface SettingChangeListener {

    public void Changed(List<SettingKey> changedKeys);
}
