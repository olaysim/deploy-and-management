package dk.syslab.controller.broadcast;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class EventMonitor {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final EventLoopGroup group;
    private final Bootstrap bootstrap;
    private Channel channel;

    public EventMonitor(InetSocketAddress address) {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioDatagramChannel.class)
            .option(ChannelOption.SO_BROADCAST, true)
            .handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ChannelPipeline pl = ch.pipeline();
                    pl.addLast(new EventDecoder());
                    pl.addLast(new EventHandler());
                }
            })
            .localAddress(address);
    }

    public void start() throws InterruptedException {
        channel = bootstrap.bind().sync().channel();
    }

    public void stop() {
        group.shutdownGracefully();
    }

    public void addEventListener(EventListener listener) {
        channel.pipeline().get(EventHandler.class).addListener(listener);
    }

    public void removeEventListener(EventListener listener) {
        channel.pipeline().get(EventHandler.class).removeListener(listener);
    }
}
