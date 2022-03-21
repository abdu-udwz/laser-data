package transmitter.ui.stage;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoadableStage<T> extends Stage {

    private T controller;

    public LoadableStage(URL location, ResourceBundle bundle) {
        super();

        addEventHandler(WindowEvent.WINDOW_SHOWING, event -> {
            try {
                FXMLLoader loader = new FXMLLoader(location, bundle);
                Pane mainPane = loader.load();
                this.controller = loader.getController();
                this.setScene(new Scene(mainPane));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> {
            this.setScene(null);
            controller = null;
            System.gc();
        });
    }

    public T getController() {
        return controller;
    }
}
