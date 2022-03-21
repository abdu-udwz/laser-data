package transmitter.util.logging.terminal;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.text.Text;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class TerminalFormatter extends Formatter {

    private static final String PROMPT_PART_1 = "~";
    private static final String PROMPT_PART_2 = "$";

    private static final String STYLE_CLASS_TIMESTAMP = "timestamp";
    private static final String STYLE_CLASS_HEAD = "head";
    private static final String STYLE_CLASS_PROMPT_COLON = "prompt-colon";
    private static final String STYLE_CLASS_PROMPT_1 = "prompt1";
    private static final String STYLE_CLASS_PROMPT_2 = "prompt2";

    private static final String STYLE_CLASS_NORMAL = "normal";
    private static final String STYLE_CLASS_SUCCESS = "success";
    private static final String STYLE_CLASS_WARNING = "warning";
    private static final String STYLE_CLASS_ERROR = "error";

    private SimpleBooleanProperty useTimestamp = new SimpleBooleanProperty(this, "useTimestamp");

    // private final List<Text> prompt = preparePrompt();

    public TerminalFormatter() {
        this(false);
    }

    public TerminalFormatter(boolean useTimestamp) {
        this.useTimestamp.set(useTimestamp);
    }

    @Override
    public String format(LogRecord record) {
        // the format is:
        // ++ timestamp enabled:
        // [timestamp] head: prompt1 prompt2 message
        // e.g. [2019-10-20] Receiver:~$ Got packet from... etc

        // ++ timestamp not enabled:
        // head: prompt1 prompt2 message
        // e.g. Receiver:~$ Got packet from... etc

        Object[] parameters = record.getParameters();
        String headString = (String) parameters[0];

        if (useTimestamp.get()) {
            return String.format("[%s] %s:%s%s %s",
                    prepareTimestamp(record.getMillis()),
                    headString,
                    PROMPT_PART_1,
                    PROMPT_PART_2,
                    record.getMessage());
        } else {
            return String.format("%s:%s%s %s",
                    headString,
                    PROMPT_PART_1,
                    PROMPT_PART_2,
                    record.getMessage());
        }
    }

    public List<Text> formatTerminal(LogRecord record) {

        List<Text> formattedRecord = new ArrayList<>();

        Text timestampText = null;

        if (useTimestamp.get()) {

            String timestampString = prepareTimestamp(record.getMillis());

            timestampText = new Text(String.format("[%s] ", timestampString));
            timestampText.getStyleClass().add(STYLE_CLASS_TIMESTAMP);
        }

        // the parameters of the log record:
        // 0: the string shown before the prompt

        String headString = "Unknown";
        try {
            Object[] parameters = record.getParameters();
            headString = (String) parameters[0];

        } catch (IndexOutOfBoundsException ignored) {
        }

        Text headText = new Text(headString);
        headText.getStyleClass().add(STYLE_CLASS_HEAD);

        String messageString = record.getMessage();
        Text messageText = new Text(messageString);
        messageText.getStyleClass().add(getStyleclassForLevel(record.getLevel()));

        if (timestampText != null)
            formattedRecord.add(timestampText);

        formattedRecord.add(headText);
        formattedRecord.addAll(this.preparePrompt());
        formattedRecord.add(messageText);

        return formattedRecord;
    }

    private List<Text> preparePrompt() {
        Text colon = new Text(":");
        Text part1 = new Text(PROMPT_PART_1);

        Text part2 = new Text(PROMPT_PART_2 + " ");

        colon.getStyleClass().addAll(STYLE_CLASS_PROMPT_COLON);
        part1.getStyleClass().add(STYLE_CLASS_PROMPT_1);
        part2.getStyleClass().add(STYLE_CLASS_PROMPT_2);

        return Arrays.asList(colon, part1, part2);
    }

    private String prepareTimestamp(long millis) {
        Instant instant = Instant.ofEpochMilli(millis);
        LocalDateTime date = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        return DateTimeFormatter.ofPattern("uuuu-MM-dd").format(date);
    }

    private String getStyleclassForLevel(Level level) {

        if (level.equals(Level.INFO) || level.equals(Level.CONFIG))
            return STYLE_CLASS_NORMAL;
        else if (level.equals(Level.WARNING))
            return STYLE_CLASS_WARNING;
        else if (level.equals(Level.SEVERE))
            return STYLE_CLASS_ERROR;
        else if (level.equals(Level.FINEST))
            return STYLE_CLASS_SUCCESS;
        else
            return STYLE_CLASS_NORMAL;

    }

    public boolean isUseTimestamp() {
        return useTimestamp.get();
    }

    public SimpleBooleanProperty useTimestampProperty() {
        return useTimestamp;
    }

    public void setUseTimestamp(boolean useTimestamp) {
        this.useTimestamp.set(useTimestamp);
    }
}
