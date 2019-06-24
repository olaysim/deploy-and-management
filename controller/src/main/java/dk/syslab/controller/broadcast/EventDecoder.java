package dk.syslab.controller.broadcast;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class EventDecoder extends MessageToMessageDecoder<DatagramPacket> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
        try {
            ByteBuf buf = msg.content();
            int type = (int)buf.readSlice(1).readByte();
            switch (type) {
                case 1:
                    int idx = buf.indexOf(0, buf.readableBytes(), Event.SEPERATOR);
                    String name = buf.readSlice(idx-1).toString(CharsetUtil.UTF_8);
                    buf.readSlice(1);
                    String address = buf.readSlice(buf.readableBytes()).toString(CharsetUtil.UTF_8);
                    Event event = new Event(name, address, msg.sender().getAddress().getHostAddress());
                    out.add(event);
                    break;
                case 2:
                    int statuscode = (int)buf.readSlice(1).readByte();
                    int running = buf.readSlice(4).readInt();
                    int total = buf.readSlice(4).readInt();
                    name = buf.readSlice(buf.readableBytes()).toString(CharsetUtil.UTF_8);
                    event = new Event(name, statuscode, running, total);
                    out.add(event);
                    break;
            }
        } catch (Exception ignored) {
            // if the message can't be parsed, throw it away
//            log.debug("Received broadcast message which could not be parsed.", ignore);
        }
    }

//    private int bytesToInt(byte[] bytes) {
//        int ret = 0;
//        for (int i=0; i < 4; i++) {
//            ret <<= 8;
//            ret |= (int)bytes[i] & 0xFF;
//        }
//        return ret;
//    }
}
