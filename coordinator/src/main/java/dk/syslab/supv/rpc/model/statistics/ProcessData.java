package dk.syslab.supv.rpc.model.statistics;

import java.util.ArrayList;
import java.util.List;

public class ProcessData {
    private long timestamp;
    private List<Double> cpu;
    private List<Double> memory;
    private List<Long> vsz;
    private List<Long> rss;
    private int length;

    @Deprecated
    public ProcessData() {
        timestamp = System.currentTimeMillis();
        cpu = new ArrayList<>();
        memory = new ArrayList<>();
        vsz = new ArrayList<>();
        rss = new ArrayList<>();
        length = 0;
    }

    public ProcessData(int length) {
        timestamp = System.currentTimeMillis();
        cpu = new ArrayList<>(length);
        memory = new ArrayList<>(length);
        vsz = new ArrayList<>(length);
        rss = new ArrayList<>(length);
        this.length = length;
    }

    public void add(long timestamp, double cpu, double memory, long vsz, long rss) {
        this.timestamp = timestamp;
        this.cpu.add(cpu);
        this.memory.add(memory);
        this.vsz.add(vsz);
        this.rss.add(rss);
        if (this.cpu.size() > length) this.cpu.remove(0);
        if (this.memory.size() > length) this.memory.remove(0);
        if (this.vsz.size() > length) this.vsz.remove(0);
        if (this.rss.size() > length) this.rss.remove(0);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<Double> getCpu() {
        return cpu;
    }

    public List<Double> getMemory() {
        return memory;
    }

    public List<Long> getVsz() {
        return vsz;
    }

    public List<Long> getRss() {
        return rss;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setCpu(List<Double> cpu) {
        this.cpu = cpu;
    }

    public void setMemory(List<Double> memory) {
        this.memory = memory;
    }

    public void setVsz(List<Long> vsz) {
        this.vsz = vsz;
    }

    public void setRss(List<Long> rss) {
        this.rss = rss;
    }
}
