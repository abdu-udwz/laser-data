package transmitter.source.ui.controller.main.tab;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import transmitter.source.util.logging.Logging;
import transmitter.source.util.logging.terminal.TerminalHandler;
import transmitter.source.util.logging.terminal.TerminalLogRecord;

public class TerminalController extends MainTabController {

    @FXML private TextFlow flow;
    @FXML private JFXTextField inputField;
    @FXML private JFXButton submitButton;

    private TerminalHandler terminalHandler = (TerminalHandler) Logging.TERMINAL_LOGGER.getHandlers()[0];

    @FXML
    private void initialize(){
        initComponents();
        initListeners();
    }

    private void initComponents(){

        // remove preview from scene builder
        flow.getChildren().clear();

        if (terminalHandler.getRecords().size() > 0){
            for (TerminalLogRecord record : terminalHandler.getRecords()) {
                addRecord(record);
            }
        }
    }

    private void initListeners(){

        terminalHandler.getRecords().addListener((ListChangeListener<? super TerminalLogRecord>) listener -> {
            while (listener.next()){
                if (listener.wasAdded()){
                    Platform.runLater(() -> {
                        for (TerminalLogRecord logRecord : listener.getAddedSubList()) {
                            addRecord(logRecord);
                        }
                    });
                }
            }
        });
    }

    private void addRecord(TerminalLogRecord logRecord){
        flow.getChildren().addAll(logRecord.getItems());
        flow.getChildren().add(new Text("\n"));
    }

    @FXML
    private void deleteAllRecords(){
        terminalHandler.getRecords().clear();
        flow.getChildren().clear();
    }
}
