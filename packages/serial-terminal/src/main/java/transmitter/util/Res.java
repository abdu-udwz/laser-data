package transmitter.util;

import com.sun.istack.internal.NotNull;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class Res {

    private final static String RESOURCES = "/transmitter/resources/";
    private final static String STYLE_SHEET = RESOURCES + "stylesheet/";
    private final static String PROPERTIES = RESOURCES + "properties/";
    private final static String FXML = RESOURCES + "fxml/";

    private final static String IMAGE = RESOURCES + "image/";
    private final static String ICONS = IMAGE + "icons/";

    private final static String DEVELOPER = FXML + "developer/";
    private final static String DIALOGS = FXML + "dialogs/";

    final static String LANGUAGE_PATH = "transmitter.resources.string.strings";
    final static String DEFAULT_PROPERTIES = "transmitter.resources.properties.default";

    public enum Image {
        CRESTYANO(IMAGE + "players_photos/Crestyano.jpg");

        final String url;

        Image(String url) {
            this.url = url;
        }

        @NotNull
        public javafx.scene.image.Image getImage() {
            return new javafx.scene.image.Image(getClass().getResourceAsStream(url));
        }
    }

    public enum Fxml {
        MAIN_WINDOW(FXML + "MainWindow.fxml"),
        MESSENGER_WINDOW(FXML + "MessengerWindow.fxml"),
        PAINT_WINDOW(FXML + "PaintController.fxml"),
        TERMINAL_WINDOW(FXML + "TerminalWindow.fxml"),
        SETTINGS_WINDOW(FXML + "SettingsWindow.fxml"),
        CONNECT_DIALOG_LAYOUT(FXML + "ConnectionAlertLayout.fxml"),

        LIGHT_TIME_CHART(DEVELOPER + "LightTimeChart.fxml"),
        INFO_ALERT_DETAILS_LAYOUT(DIALOGS + "InfoAlertDetailsLayout.fxml"),
        CUSTOM_ALERT_LAYOUT(DIALOGS + "CustomAlertLayout.fxml");

        private final String url;

        Fxml(String url) {
            this.url = url;
        }

        @NotNull
        public URL getUrl() {

            return getClass().getResource(url);
        }

        @Override
        public String toString() {
            return url;
        }
    }

    public enum Stylesheet {
        TEMPLATES(STYLE_SHEET + "template.css"),
        THEME(STYLE_SHEET + "theme.css"),
        DEFAULT_CUSTOM_ALERT_STYLE(STYLE_SHEET + "default-custom-alert-style.css");

        private final String url;

        Stylesheet(String url) {
            this.url = url;
        }

        @NotNull
        public String getUrl() {
            return getClass().getResource(url).toExternalForm();
        }
    }

    public static ResourceBundle getBundle(String language) {
        return ResourceBundle.getBundle(LANGUAGE_PATH, Locale.forLanguageTag(language));
    }
}
