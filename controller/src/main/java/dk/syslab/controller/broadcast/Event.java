package dk.syslab.controller.broadcast;

public class Event {
    public static final byte SEPERATOR = (byte) '|';

    private String name;
    private String address;
    private String broadcastAddress;
    private int statuscode;
    private int running;
    private int total;

    public Event() {}

    public Event(String name, String address, String broadcastAddress) {
        this.name = name;
        this.address = address;
        this.broadcastAddress = broadcastAddress;
    }

    public Event(String name, int statuscode, int running, int total) {
        this.name = name;
        this.statuscode = statuscode;
        this.running = running;
        this.total = total;
    }

    public Event(Node node) {
        this.name = node.getName();
        this.address = node.getAddress();
        this.statuscode = node.getStatuscode();
        this.running = node.getRunning();
        this.total = node.getTotal();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBroadcastAddress() {
        return broadcastAddress;
    }

    public void setBroadcastAddress(String broadcastAddress) {
        this.broadcastAddress = broadcastAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Node getNode() {
        return new Node(name, address, statuscode, running, total);
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

    public boolean isAddress() {
        return address != null;
    }

    public boolean isStatistics() {
        return address == null;
    }

    @Override
    public String toString() {
        return name + ": " + address + "(" + statuscode + ", " + running + "/" + total + ")";
    }
}
