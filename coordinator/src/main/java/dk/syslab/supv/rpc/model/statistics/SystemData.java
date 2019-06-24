package dk.syslab.supv.rpc.model.statistics;

import java.util.ArrayList;
import java.util.List;

public class SystemData {
    private long timestamp;
    private List<Double> user;
    private List<Double> nice;
    private List<Double> system;
    private List<Double> idle;
    private List<Double> load1;
    private List<Double> load2;
    private List<Double> load3;
    private List<Long> memory;
    private List<Long> swap;
    private int length;

    @Deprecated
    public SystemData() {
        timestamp = System.currentTimeMillis();
        user = new ArrayList<>();
        nice = new ArrayList<>();
        system = new ArrayList<>();
        idle = new ArrayList<>();
        load1 = new ArrayList<>();
        load2 = new ArrayList<>();
        load3 = new ArrayList<>();
        memory = new ArrayList<>();
        swap = new ArrayList<>();
        length = 0;
    }

    public SystemData(int length) {
        timestamp = System.currentTimeMillis();
        user = new ArrayList<>(length);
        nice = new ArrayList<>(length);
        system = new ArrayList<>(length);
        idle = new ArrayList<>(length);
        load1 = new ArrayList<>(length);
        load2 = new ArrayList<>(length);
        load3 = new ArrayList<>(length);
        memory = new ArrayList<>(length);
        swap = new ArrayList<>(length);
        this.length = length;
    }

    public void add(long timestamp, double user, double nice, double system, double idle, double load1, double load2, double load3, long memory, long swap) {
        this.timestamp = timestamp;
        this.user.add(user);
        this.nice.add(nice);
        this.system.add(system);
        this.idle.add(idle);
        this.load1.add(load1);
        this.load2.add(load2);
        this.load3.add(load3);
        this.memory.add(memory);
        this.swap.add(swap);
        if (this.user.size() > length) this.user.remove(0);
        if (this.nice.size() > length) this.nice.remove(0);
        if (this.system.size() > length) this.system.remove(0);
        if (this.idle.size() > length) this.idle.remove(0);
        if (this.load1.size() > length) this.load1.remove(0);
        if (this.load2.size() > length) this.load2.remove(0);
        if (this.load3.size() > length) this.load3.remove(0);
        if (this.memory.size() > length) this.memory.remove(0);
        if (this.swap.size() > length) this.swap.remove(0);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<Double> getUser() {
        return user;
    }

    public List<Double> getNice() {
        return nice;
    }

    public List<Double> getSystem() {
        return system;
    }

    public List<Double> getIdle() {
        return idle;
    }

    public List<Double> getLoad1() {
        return load1;
    }

    public List<Double> getLoad2() {
        return load2;
    }

    public List<Double> getLoad3() {
        return load3;
    }

    public List<Long> getMemory() {
        return memory;
    }

    public List<Long> getSwap() {
        return swap;
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

    public void setUser(List<Double> user) {
        this.user = user;
    }

    public void setNice(List<Double> nice) {
        this.nice = nice;
    }

    public void setSystem(List<Double> system) {
        this.system = system;
    }

    public void setIdle(List<Double> idle) {
        this.idle = idle;
    }

    public void setLoad1(List<Double> load1) {
        this.load1 = load1;
    }

    public void setLoad2(List<Double> load2) {
        this.load2 = load2;
    }

    public void setLoad3(List<Double> load3) {
        this.load3 = load3;
    }

    public void setMemory(List<Long> memory) {
        this.memory = memory;
    }

    public void setSwap(List<Long> swap) {
        this.swap = swap;
    }
}
