package transmitter.util.logging.terminal;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.text.Text;

import java.util.List;

public class TerminalLogRecord {

    private final ObservableList<Text> items = FXCollections.observableArrayList();

    public TerminalLogRecord(Text... items) {
        getItems().addAll(items);
    }

    public TerminalLogRecord(List<Text> items) {
        getItems().addAll(items);
    }

    public ObservableList<Text> getItems() {
        return this.items;
    }

}
