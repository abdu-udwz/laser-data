package transmitter.connection;

public enum BaudRate {

    R_110(110),
    R_300(300),
    R_600(600),
    R_1200(1200),
    R_4800(4800),
    R_9600(9600),
    R_14400(14400),
    R_19200(19200),
    R_38400(38400),
    R_57600(57600),
    R_115200(115200),
    R_128000(128000),
    R_256000(256000),
    R_500000(500_000),
    R_1000000(1_000_000),
    R_2000000(2_000_000);

    public final int val;

    BaudRate(int val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return String.valueOf(val);
    }
}
