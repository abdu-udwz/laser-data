package transmitter.util;

import javafx.stage.Modality;
import transmitter.ui.controller.main.MainController;
import transmitter.ui.controller.SettingsController;
import transmitter.ui.stage.LoadableStage;

public class Windows {

    public static final LoadableStage<MainController> MAIN_WINDOW = new LoadableStage<>(Res.Fxml.MAIN_WINDOW.getUrl(),
            Utils.getBundle());

    public static final LoadableStage<SettingsController> SETTINGS_WINDOW = new LoadableStage<>(
            Res.Fxml.SETTINGS_WINDOW.getUrl(), Utils.getBundle());

    static {
        SETTINGS_WINDOW.initModality(Modality.APPLICATION_MODAL);
        SETTINGS_WINDOW.setResizable(false);
        SETTINGS_WINDOW.setTitle("Settings");

        MAIN_WINDOW.setTitle("Laser Data");
    }
}
