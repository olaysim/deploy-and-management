package dk.syslab.supv.dto.distributed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeList {

    private List<String> nodes;
    private Map<String, String> address;
    private Map<String, String> self;
    private Map<String, Integer> statistics;

    public NodeList() {
        this.nodes = new ArrayList<>();
        this.address = new HashMap<>();
        this.self = new HashMap<>();
        this.statistics = new HashMap<>();
    }

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public Map<String, String> getAddress() {
        return address;
    }

    public void setAddress(Map<String, String> address) {
        this.address = address;
    }

    public Map<String, String> getSelf() {
        return self;
    }

    public void setSelf(Map<String, String> self) {
        this.self = self;
    }

    public Map<String, Integer> getStatistics() {
        return statistics;
    }

    public void setStatistics(Map<String, Integer> statistics) {
        this.statistics = statistics;
    }
}
