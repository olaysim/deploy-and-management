package dk.syslab.controller.broadcast;

import dk.syslab.controller.xmlrpc.ProcessInfo;
import dk.syslab.controller.xmlrpc.ProcessState;
import dk.syslab.controller.xmlrpc.XmlRpcService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.apache.xmlrpc.XmlRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EventBroadcaster implements Runnable {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final static int BROADCAST_DELAY = 10; // seconds

    private final EventLoopGroup group;
    private final Bootstrap bootstrap;
    private ScheduledExecutorService executor;
    private Channel channel;
    private Node node;
    private XmlRpcService xmlRpcService;

    public EventBroadcaster(InetSocketAddress broadcastAddress, Node node, XmlRpcService xmlRpcService) {
        this.node = node;
        this.xmlRpcService = xmlRpcService;
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioDatagramChannel.class)
            .option(ChannelOption.SO_BROADCAST, true)
            .handler(new EventEncoder(broadcastAddress));
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void run() {
        if (channel != null && channel.isActive()) {
            node.setStatuscode(xmlRpcService.currentSupervisorInfo().getCode());
            int count = 0;
            int total = 0;
            try {
                for (ProcessInfo processInfo : xmlRpcService.getAllProcessInfo()) {
                    total++;
                    if (processInfo.getState() == ProcessState.RUNNING) {
                        count++;
                    }
                }
            } catch (XmlRpcException ignore) {}
            node.setRunning(count);
            node.setTotal(total);
            Event event = new Event(node);
            channel.writeAndFlush(event);
        } else {
            log.error("Unable to broadcast address, connection is closed!");
        }
    }

    public EventBroadcaster start() {
        try {
            // connect to broadcast address
            channel = bootstrap.bind(0).sync().channel();

            // start periodic broadcast
            executor.scheduleWithFixedDelay(this, 5, BROADCAST_DELAY, TimeUnit.SECONDS);

        } catch (InterruptedException e) {
            log.error("Unable to start broadcaster!, closing broadcaster.", e);
        }
        return this;
    }

    public void stop() {
        group.shutdownGracefully();

        try {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            log.debug("Node broadcaster took too long to shut down, interrupting thread.");
        } finally {
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
        }
    }

    public void updateNode(Node node) {
        this.node = node;
    }
}
