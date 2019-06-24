package dk.syslab.supv.dto.supervisor;

public class TailLog {
    private String log;
    private int offset;
    private boolean overflow;

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public boolean isOverflow() {
        return overflow;
    }

    public void setOverflow(boolean overflow) {
        this.overflow = overflow;
    }
}
