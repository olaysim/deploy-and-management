package dk.syslab.controller.broadcast;

import java.util.Objects;

public class Node {
    private String name;
    private String address;
    private int statuscode;
    private int running;
    private int total;
    private long timestamp;

    public Node() {
        timestamp = System.currentTimeMillis();
    }

    public Node(String name, String address, int statuscode, int running, int total) {
        this.name = name;
        this.address = address;
        this.statuscode = statuscode;
        this.running = running;
        this.total = total;
        timestamp = System.currentTimeMillis();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatuscode() {
        return statuscode;
    }

    public void setStatuscode(int statuscode) {
        this.statuscode = statuscode;
    }

    public int getRunning() {
        return running;
    }

    public void setRunning(int running) {
        this.running = running;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return name + ": " + address + " (" + timestamp + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node node = (Node) o;
        return Objects.equals(name, node.name) &&
            Objects.equals(address, node.address);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, address);
    }
}
