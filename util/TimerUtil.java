package ai.venice.trestage.util;

public class TimerUtil {
    private long last;

    public void reset() { last = System.currentTimeMillis(); }
    public boolean passed(long ms) { return System.currentTimeMillis() - last >= ms; }
    public long elapsed() { return System.currentTimeMillis() - last; }
}
