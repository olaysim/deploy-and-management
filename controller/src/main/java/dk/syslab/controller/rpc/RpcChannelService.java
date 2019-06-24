package dk.syslab.controller.rpc;

import dk.syslab.controller.Configuration;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RpcChannelService implements Runnable {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private int port;
    private final Map<String, Connection> connections;
    private ScheduledExecutorService scheduledExecutorService;

    public RpcChannelService(Configuration configuration) {
        port = Integer.parseInt(configuration.getRequiredProperty("grpc.server"));
        connections = new HashMap<>();
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleWithFixedDelay(this, 10, 5, TimeUnit.MINUTES);
    }

    public ManagedChannel getChannel(String host) {
        synchronized (connections) {
            Connection connection = connections.get(host);
            if (connection != null && connection.getChannel() != null && (connection.getChannel().isTerminated() || connection.getChannel().isShutdown())) {
                try {
                    connection.getChannel().shutdownNow();
                } catch (Exception ignore) {}
                connections.remove(host);
                connection = null;
            }
            if (connection != null && connection.getChannel() != null) {
                connection.setTimestamp(System.currentTimeMillis());
                return connection.getChannel();
            } else {
                ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
                Connection conn = new Connection(channel);
                connections.put(host, conn);
                return channel;
            }
        }
    }

    @Override
    public void run() {
        synchronized (connections) {
            List<String> remove = new ArrayList<>();
            for (Map.Entry<String, Connection> entry : connections.entrySet()) {
                if (System.currentTimeMillis() - entry.getValue().getTimestamp() > 300000) { // force close connections not used for 5 minutes
                    remove.add(entry.getKey());
                }
            }
            for (String host : remove) {
                Connection conn = connections.remove(host);
                if (conn.getChannel() != null && !conn.getChannel().isTerminated() && !conn.getChannel().isShutdown()) {
                    try {
                        conn.getChannel().shutdownNow();
                    } catch (Exception ignore) {}
                }
            }
        }
    }

    public static class Connection {
        private ManagedChannel channel;
        private long timestamp;

        public Connection() {
            this.timestamp = System.currentTimeMillis();
        }

        public Connection(ManagedChannel channel) {
            this();
            this.channel = channel;
        }

        public ManagedChannel getChannel() {
            return channel;
        }

        public void setChannel(ManagedChannel channel) {
            this.channel = channel;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
