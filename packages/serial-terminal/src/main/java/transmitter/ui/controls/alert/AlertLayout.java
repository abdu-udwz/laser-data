package transmitter.ui.controls.alert;

import org.kordamp.ikonli.javafx.FontIcon;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import transmitter.util.Res;
import transmitter.util.Utils;

import java.io.IOException;

public class AlertLayout extends VBox {

    @FXML
    protected HBox headingPane;
    @FXML
    protected Label headingLabel;
    @FXML
    protected FontIcon graphicView;

    @FXML
    protected StackPane bodyPane;
    @FXML
    protected Label bodyLabel;

    @FXML
    protected VBox footerPane;
    @FXML
    protected HBox buttonsBar;
    @FXML
    protected FlowPane actionsFlow;

    public AlertLayout() {
        super();
        createSceneGraph();

    }

    private void createSceneGraph() {
        FXMLLoader fxmlLoader = new FXMLLoader(Res.Fxml.CUSTOM_ALERT_LAYOUT.getUrl(), Utils.getBundle());
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @FXML
    protected void initialize() {

    }

}
