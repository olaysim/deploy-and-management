package dk.syslab.supv.rpc;

import dk.syslab.controller.rpc.protobuf.BroadcastRpcGrpc;
import dk.syslab.controller.rpc.protobuf.Messages;
import dk.syslab.supv.rpc.model.NodeList;
import dk.syslab.supv.rpc.model.Node;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("Duplicates")
@Service
public class BroadcastRpcService implements Runnable {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    RpcChannelService channelService;

    private ReentrantLock lock;
    private NodeList cache;
    private ScheduledExecutorService scheduledExecutorService;
    private String host;

    public BroadcastRpcService(Environment env, RpcChannelService channelService) {
        this.lock = new ReentrantLock(true);
        this.channelService = channelService;
        cache = new NodeList();
        host = env.getRequiredProperty("default.node");
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleWithFixedDelay(this, 0, 10, TimeUnit.SECONDS);
    }

    public Node getSelf(String host) {
        ManagedChannel channel = channelService.getChannel(host);
        BroadcastRpcGrpc.BroadcastRpcBlockingStub stub = BroadcastRpcGrpc.newBlockingStub(channel);
        Messages.Node result = stub.getSelf(Messages.Token.newBuilder().build());
        Node node = new Node();
        node.setName(result.getName());
        node.setAddress(result.getAddress());
        node.setStatuscode(result.getStatuscode());
        node.setRunning(result.getRunning());
        node.setTotal(result.getTotal());
        node.setTimestamp(result.getTimestamp());
        return node;
    }

    public SortedMap<String, Node> getNodeMap(String host) {
        ManagedChannel channel = channelService.getChannel(host);
        BroadcastRpcGrpc.BroadcastRpcBlockingStub stub = BroadcastRpcGrpc.newBlockingStub(channel);
        Messages.NodeMap result = stub.getNodeMap(Messages.Token.newBuilder().build());
        SortedMap<String, Node> map = new TreeMap<>(Comparator.naturalOrder());
        for (Map.Entry<String, Messages.Node> entry : result.getNodeMapMap().entrySet()) {
            Node node = new Node();
            node.setName(entry.getValue().getName());
            node.setAddress(entry.getValue().getAddress());
            node.setStatuscode(entry.getValue().getStatuscode());
            node.setRunning(entry.getValue().getRunning());
            node.setTotal(entry.getValue().getTotal());
            node.setTimestamp(entry.getValue().getTimestamp());
            map.put(entry.getKey(), node);
        }
        return map;
    }

    public SortedSet<Node> getSortedNodes(String host) {
        ManagedChannel channel = channelService.getChannel(host);
        BroadcastRpcGrpc.BroadcastRpcBlockingStub stub = BroadcastRpcGrpc.newBlockingStub(channel);
        Messages.SortedNodes result = stub.getSortedNodes(Messages.Token.newBuilder().build());
        SortedSet<Node> list = new TreeSet<>(Comparator.comparing(Node::getName));
        for (Messages.Node n : result.getNodeList()) {
            Node node = new Node();
            node.setName(n.getName());
            node.setAddress(n.getAddress());
            node.setStatuscode(n.getStatuscode());
            node.setRunning(n.getRunning());
            node.setTotal(n.getTotal());
            node.setTimestamp(n.getTimestamp());
            list.add(node);
        }
        return list;
    }

    public Map<String, Integer> getNodeStatistics(String host) {
        ManagedChannel channel = channelService.getChannel(host);
        BroadcastRpcGrpc.BroadcastRpcBlockingStub stub = BroadcastRpcGrpc.newBlockingStub(channel);
        Messages.NodeStatistics result = stub.getNodeStatistics(Messages.Token.newBuilder().build());
        Map<String, Integer> map = new HashMap<>();
        map.put("supervisors-running", result.getSupervisorsRunning());
        map.put("supervisors-total", result.getSupervisorsTotal());
        map.put("processes-running", result.getProcessesRunning());
        map.put("processes-total", result.getProcessesTotal());
        return map;
    }

    public NodeList getNodeList(String host) {
        ManagedChannel channel = channelService.getChannel(host);
        BroadcastRpcGrpc.BroadcastRpcBlockingStub stub = BroadcastRpcGrpc.newBlockingStub(channel);
        Messages.NodeListResult result = stub.getNodeList(Messages.Token.newBuilder().build());
        NodeList list = new NodeList();

        list.setNodes(result.getNodeList());

        SortedMap<String, String> map = new TreeMap<>(Comparator.naturalOrder());
        for (Map.Entry<String, String> entry : result.getAddressMap().entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        list.setAddress(map);

        Map<String, String> self = new HashMap<>();
        self.put("name", result.getSelf().getName());
        self.put("address", result.getSelf().getAddress());

        Map<String, Integer> stats = new HashMap<>();
        stats.put("supervisors-running", result.getStatistics().getSupervisorsRunning());
        stats.put("supervisors-total", result.getStatistics().getSupervisorsTotal());
        stats.put("processes-running", result.getStatistics().getProcessesRunning());
        stats.put("processes-total", result.getStatistics().getProcessesTotal());
        list.setStatistics(stats);

        return list;
    }

    public String lookUpAddress(String node) {
        try {
            lock.lock();
            if (node.equalsIgnoreCase("localhost")) return "127.0.0.1";
            if (node.equalsIgnoreCase("R267F662")) return "127.0.0.1";
            return cache.getAddress().get(node);
        } catch (Exception ignore) {}
        finally {
            lock.unlock();
        }
        return null;
    }

    @Override
    public void run() {
        try {
            lock.lock();
            cache = getNodeList(host);
            lock.unlock();
        } catch (Exception ignore) {}
        finally {
            lock.unlock();
        }
    }

    public String getDefaultNode() {
        return host;
    }
}
