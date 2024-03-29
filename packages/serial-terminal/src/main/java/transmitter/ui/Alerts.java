package transmitter.ui;

import javafx.stage.Stage;
import javafx.stage.StageStyle;
import transmitter.ui.controls.alert.AlertAction;
import transmitter.ui.controls.alert.InfoAlert;
import transmitter.ui.controls.alert.InfoAlertType;

import java.util.Optional;

public abstract class Alerts {

    private Alerts() {
    }

    public static Optional<AlertAction> infoAlert(Stage stage, String heading, String body,
            AlertAction... actions) {
        InfoAlert alert = prepAlert(stage, InfoAlertType.INFO, heading, body, actions);
        return alert.showAndWait();
    }

    public static Optional<AlertAction> warningAlert(Stage stage, String heading, String body,
            AlertAction... actions) {
        InfoAlert alert = prepAlert(stage, InfoAlertType.WARNING, heading, body, actions);
        return alert.showAndWait();
    }

    public static Optional<AlertAction> errorAlert(Stage stage, String heading, String body,
            Throwable throwable, AlertAction... actions) {
        InfoAlert alert = prepAlert(stage, InfoAlertType.ERROR, heading, body, actions);

        if (throwable != null) {
            alert.setUseDetails(true);
            alert.visualizeStackTrace(throwable);
        }
        return alert.showAndWait();
    }

    private static InfoAlert prepAlert(Stage stage, InfoAlertType type, String heading, String body,
            AlertAction... actions) {

        InfoAlert alert = new InfoAlert(stage, type, actions);
        alert.setHeadingText(heading);
        alert.setBodyText(body);

        if (stage == null) {
            alert.initStyle(StageStyle.TRANSPARENT);

            alert.getLayout().heightProperty().addListener((observable, oldValue, newValue) -> {
                // FIXME: causes too much load on cpu,
                // but may not be that problem used only once or twice..
                alert.setHeight(newValue.doubleValue());

                // bad look, very recourse consumer..
                // alert.getLayout().getScene().getWindow().sizeToScene();
            });
        }

        return alert;
    }
}
