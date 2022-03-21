package transmitter.util.logging.terminal;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.text.Text;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class TerminalHandler extends Handler {

    private static final int MAX_RECORD_COUNT = 500;
    private final ObservableList<TerminalLogRecord> records = FXCollections.observableArrayList();

    private final TerminalFormatter formatter = new TerminalFormatter();

    public TerminalHandler() {
        this(new TerminalFormatter());
    }

    public TerminalHandler(TerminalFormatter formatter) {
        super();
        this.setFormatter(formatter);

        this.records.addListener((ListChangeListener<? super TerminalLogRecord>) c -> {
            if (records.size() > MAX_RECORD_COUNT) {
                int toRemoveCount = MAX_RECORD_COUNT - records.size();
                records.remove(0, toRemoveCount - 1);
            }
        });
    }

    @Override
    public void publish(LogRecord record) {
        if (record == null)
            return;

        List<Text> texts = formatter.formatTerminal(record);
        getRecords().add(new TerminalLogRecord(texts));
        flush();
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }

    public ObservableList<TerminalLogRecord> getRecords() {
        return records;
    }
}
