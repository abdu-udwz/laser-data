package transmitter.source.util.logging.terminal;

import transmitter.source.util.logging.Logging;

import java.util.logging.Level;

public interface TerminalLoggable {

    String getTerminalHead();

    default void logTerminal(Level level, String message){
        Logging.logTerminal(getTerminalHead(), level, message);
    }
}
