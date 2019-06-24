package dk.syslab.controller.statistics;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SystemStatistics {
    @JsonIgnore
    private final static int HOUR_LENGTH = 60; // 60 data points per process (* 9)
    @JsonIgnore
    private final static int WEEKS_LENGTH = 336; // 1h * 24 * 14 (14 days of data) (336 data points * 9)

    private SystemData weeks;
    private SystemData hour;

    public SystemStatistics() {
        weeks = new SystemData(WEEKS_LENGTH);
        hour = new SystemData(HOUR_LENGTH);
    }

    public void add(long timestamp, double user, double nice, double system, double idle, double load1, double load2, double load3, long memory, long swap) {
        hour.add(timestamp, user, nice, system, idle, load1, load2, load3, memory, swap);
    }

    public void average(long timestamp) {
        double user = 0;
        double nice = 0;
        double system = 0;
        double idle = 0;
        double load1 = 0;
        double load2 = 0;
        double load3 = 0;
        long memory = 0;
        long swap = 0;
        for (double v : hour.getUser()) {
            user += v;
        }
        for (double v : hour.getNice()) {
            nice += v;
        }
        for (double v : hour.getSystem()) {
            system += v;
        }
        for (double v : hour.getIdle()) {
            idle += v;
        }
        for (double v : hour.getLoad1()) {
            load1 += v;
        }
        for (double v : hour.getLoad2()) {
            load2 += v;
        }
        for (double v : hour.getLoad3()) {
            load3 += v;
        }
        for (double v : hour.getMemory()) {
            memory += v;
        }
        for (double v : hour.getSwap()) {
            swap += v;
        }
        user = user / hour.getLength();
        nice = nice / hour.getLength();
        system = system / hour.getLength();
        idle = idle / hour.getLength();
        load1 = load1 / hour.getLength();
        load2 = load2 / hour.getLength();
        load3 = load3 / hour.getLength();
        memory = memory / hour.getLength();
        swap = swap / hour.getLength();
        weeks.add(timestamp, user, nice, system, idle, load1, load2, load3, memory, swap);
    }

    public SystemData getWeeks() {
        return weeks;
    }

    public SystemData getHour() {
        return hour;
    }
}
