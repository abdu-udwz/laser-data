package transmitter.ui.controller.main.tab;

import transmitter.util.Res;

import java.net.URL;

public enum MainTab {
    MESSENGER(Res.Fxml.MESSENGER_WINDOW, "Messenger"),
    PAINT(Res.Fxml.PAINT_WINDOW, "Paint"),
    TERMINAL(Res.Fxml.TERMINAL_WINDOW, "Terminal");

    public final URL fxmlUrl;
    public final String displayString;

    MainTab(Res.Fxml fxml, String displayString) {
        this.fxmlUrl = fxml.getUrl();
        this.displayString = displayString;
    }

}
