package dk.syslab.controller.broadcast;

import dk.syslab.controller.xmlrpc.SupervisorState;
import dk.syslab.controller.xmlrpc.XmlRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class BroadcastService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final static int BROADCAST_PORT = 58585;
    private final static int NODE_TIMEOUT = 20000000; // seconds

    private EventBroadcaster broadcaster;
    private EventMonitor monitor;
    private ConcurrentMap<String, Node> nodes;
    private Node self;
    private ScheduledExecutorService scheduledExecutorService;

    public BroadcastService(XmlRpcService xmlRpcService) {
        nodes = new ConcurrentHashMap<>();
        self = new Node();

        EventListener listener = new EventListener() {
            @Override
            public void EventReceived(Event event) {
                if (event.isAddress()) {
//                    log.info("Got broadcast address - name: " + event.getName() + ", address: " + event.getAddress() + ", broadcast address: " + event.getBroadcastAddress());
                    Node node = nodes.get(event.getName());
                    if (node == null) {
                        nodes.put(event.getName(), event.getNode());
                    } else {
                        if (!node.getAddress().equalsIgnoreCase(event.getAddress())) {
                            node.setAddress(event.getAddress());
                        }
                    }
                } else {
//                    log.info("Got broadcast statistics - name: " + event.getName() + ", status: " + event.getStatuscode() + ", running: " + event.getRunning() + ", total: " + event.getTotal());
                    Node node = nodes.get(event.getName());
                    if (node != null) {
                        node.setRunning(event.getRunning());
                        node.setTotal(event.getTotal());
                    }
                }

            }
        };

        try {
            // get ip address of this node
            final DatagramSocket socket = new DatagramSocket();
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            InetAddress addr = socket.getLocalAddress();
            String hostName = InetAddress.getLocalHost().getHostName();
            if (hostName == null) {
                hostName = addr.getHostName();
            }
            self.setName(hostName);
            self.setAddress(addr.getHostAddress());
            socket.close();

            broadcaster = new EventBroadcaster(new InetSocketAddress("255.255.255.255", BROADCAST_PORT), self, xmlRpcService);
            monitor = new EventMonitor(new InetSocketAddress(BROADCAST_PORT));

            monitor.start();
            monitor.addEventListener(listener);
            broadcaster.start();

        } catch (UnknownHostException | SocketException e) {
            log.error("Unable to guess preferred IP address, will not broadcast!", e);
        } catch (InterruptedException e) {
            log.error("Broadcast monitor failed", e);
        }

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleWithFixedDelay(this::removeOldNodes, 10, 5, TimeUnit.SECONDS);
    }

    public ConcurrentMap<String, Node> getNodeMap() {
        return nodes;
    }

    public SortedSet<Node> getSortedNodes() {
        SortedSet<Node> list = new TreeSet<>(Comparator.comparing(Node::getName));
        list.addAll(nodes.values());
        return list;
    }

    public Node getSelf() {
        return self;
    }

    public Node getNode(String name) {
        return nodes.get(name);
    }

    public Map<String, Integer> getNodeStatistics() {
        int running = 0;
        int supRunning = 0;
        int procTotal = 0;
        for (Map.Entry<String, Node> entry : nodes.entrySet()) {
            if (entry.getValue().getStatuscode() == SupervisorState.RUNNING) {
                supRunning++;
            }
            running += entry.getValue().getRunning();
            procTotal += entry.getValue().getTotal();
        }
        Map<String, Integer> stats = new HashMap<>(2);
        stats.put("supervisors-running", supRunning);
        stats.put("supervisors-total", nodes.size());
        stats.put("processes-running", running);
        stats.put("processes-total", procTotal);
        return stats;
    }

    public void removeOldNodes() {
        long now = System.currentTimeMillis();
        List<String> remove = new ArrayList<>();
        for (Map.Entry<String, Node> entry : nodes.entrySet()) {
            long age = now - entry.getValue().getTimestamp();
            if (age > NODE_TIMEOUT) {
                System.out.println("removing " + entry.getKey());
                remove.add(entry.getKey());
            }
        }
        for (String key : remove) {
            nodes.remove(key);
        }
    }
}
