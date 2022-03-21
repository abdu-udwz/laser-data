package transmitter.util.logging;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import transmitter.util.logging.terminal.TerminalFormatter;
import transmitter.util.logging.terminal.TerminalHandler;

import java.util.logging.*;

public class Logging {

    private Logging() {
    }

    public static final Logger TERMINAL_LOGGER = Logger.getLogger(Logging.class.getName());

    static {

        TerminalFormatter terminalFormatter = new TerminalFormatter();

        TerminalHandler terminalHandler = new TerminalHandler(terminalFormatter);
        TERMINAL_LOGGER.addHandler(terminalHandler);
        TERMINAL_LOGGER.setLevel(Level.ALL);

        // TERMINAL_LOGGER.setUseParentHandlers(false);
    }

    public static void logTerminal(String terminalHead, Level level, String message) {
        LogRecord record = new LogRecord(level, message);
        record.setParameters(new Object[] { terminalHead });

        TERMINAL_LOGGER.log(record);
    }

}
