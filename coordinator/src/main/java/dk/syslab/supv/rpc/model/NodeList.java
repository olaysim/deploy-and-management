package dk.syslab.supv.rpc.model;

import java.util.*;

public class NodeList {

    private List<String> nodes;
    private SortedMap<String, String> address;
    private Map<String, String> self;
    private Map<String, Integer> statistics;

    public NodeList() {
        this.nodes = new ArrayList<>();
        this.address = new TreeMap<>(Comparator.naturalOrder());
        this.self = new HashMap<>();
        this.statistics = new HashMap<>();
    }

    public NodeList(SortedSet<Node> set, Map<String, Node> map, Node self, Map<String, Integer> nodeStatistics) {
        this.nodes = new ArrayList<>();
        for (Node node : set) {
            nodes.add(node.getName());
        }

        this.address = new TreeMap<>(Comparator.naturalOrder());
        for (String node : nodes) {
            address.put(node, map.get(node).getAddress());
        }

        this.self = new HashMap<>();
        addSelf(self);

        this.statistics = nodeStatistics;
    }

    public void addSelf(Node n) {
        self.put("name", n.getName());
        self.put("address", n.getAddress());
    }

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public SortedMap<String, String> getAddress() {
        return address;
    }

    public void setAddress(SortedMap<String, String> address) {
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
