package transmitter.source.encode;

public enum Determiner {

    DETERMINER_0("ر*س", 0),
    DETERMINER_1("hءD", 1),
    DETERMINER_2("إTر", 2),
    DETERMINER_3("?(ط", 3),
    DETERMINER_4("Wاk", 4),
    ;

    public final String string;
    public final int index;

    Determiner(String string, int index) {
        this.string = string;
        this.index = index;
    }

    public static final String START_LINE_BINARY = "11111111";
    public static final String FINISH_LINE_BINARY = "00000000";
}
