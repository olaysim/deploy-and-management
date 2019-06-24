package dk.syslab.controller.statistics;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessStatistics {
    @JsonIgnore
    private final static int HOUR_LENGTH = 60; // 60 data points per process (* 4)
    @JsonIgnore
    private final static int WEEKS_LENGTH = 336; // 1h * 24 * 14 (14 days of data) (336 data points * 4)

    private HashMap<String, ProcessData> weeks;
    private HashMap<String, ProcessData> hour;

    public ProcessStatistics() {
        weeks = new HashMap<>();
        hour = new HashMap<>();
    }

    public void add(long timestamp, String name, double cpu, double memory, long vsz, long rss) {
        if (!hour.containsKey(name)) {
            hour.put(name, new ProcessData(HOUR_LENGTH));
        }
        hour.get(name).add(timestamp, cpu, memory, vsz, rss);
    }

    public void average(long timestamp) {
        for (Map.Entry<String, ProcessData> entry : hour.entrySet()) {
            if (!weeks.containsKey(entry.getKey())) {
                weeks.put(entry.getKey(), new ProcessData(WEEKS_LENGTH));
            }
            double cpu = 0;
            double memory = 0;
            long vsz = 0;
            long rss = 0;
            for (double v : entry.getValue().getCpu()) {
                cpu += v;
            }
            for (double v : entry.getValue().getMemory()) {
                memory += v;
            }
            for (double v : entry.getValue().getVsz()) {
                vsz += v;
            }
            for (double v : entry.getValue().getRss()) {
                rss += v;
            }
            cpu = cpu / entry.getValue().getLength();
            memory = memory / entry.getValue().getLength();
            vsz = vsz / entry.getValue().getLength();
            rss = rss / entry.getValue().getLength();
            weeks.get(entry.getKey()).add(timestamp, cpu, memory, vsz, rss);
        }
    }

    public void removeOldEntries(List<String> active) {
        List<String> remove = new ArrayList<>();
        for (Map.Entry<String, ProcessData> entry : hour.entrySet()) {
            if (!active.contains(entry.getKey())) {
                remove.add(entry.getKey());
            }
        }
        for (String item : remove) {
            if (hour.containsKey(item)) hour.remove(item);
            if (weeks.containsKey(item)) weeks.remove(item);
        }
    }

    public HashMap<String, ProcessData> getWeeks() {
        return weeks;
    }

    public HashMap<String, ProcessData> getHour() {
        return hour;
    }
}
