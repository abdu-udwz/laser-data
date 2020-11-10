package transmitter.source.util;

import javafx.stage.Modality;
import transmitter.source.ui.controller.main.MainController;
import transmitter.source.ui.controller.SettingsController;
import transmitter.source.ui.stage.LoadableStage;

public class Windows {

    public static final LoadableStage<MainController> MAIN_WINDOW =
            new LoadableStage<>(Res.Fxml.MAIN_WINDOW.getUrl(), Utils.getBundle());

    public static final LoadableStage<SettingsController> SETTINGS_WINDOW =
            new LoadableStage<>(Res.Fxml.SETTINGS_WINDOW.getUrl(), Utils.getBundle());


    static {
        SETTINGS_WINDOW.initModality(Modality.APPLICATION_MODAL);
        SETTINGS_WINDOW.setResizable(false);
    }
}
