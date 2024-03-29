package dk.syslab.controller.broadcast;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventHandler extends SimpleChannelInboundHandler<Event> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final List<EventListener> list = Collections.synchronizedList(new ArrayList<>());

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Event msg) throws Exception {
        synchronized (list) {
            for (EventListener listener : list) {
                listener.EventReceived(msg);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Error in receiving broadcast", cause);
        ctx.close();
    }

    public void addListener(EventListener listener) {
        list.add(listener);
    }

    public void removeListener(EventListener listener) {
        list.remove(listener);
    }
}
