package dk.syslab.controller.broadcast;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.List;

public class EventEncoder extends MessageToMessageEncoder<Event> {
    private final InetSocketAddress remoteAddress;

    public EventEncoder(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Event event, List<Object> out) throws Exception {
        byte statuscode = (byte)event.getStatuscode();
        byte[] running = intToBytes(event.getRunning());
        byte[] total = intToBytes(event.getTotal());
        byte[] name = event.getName().getBytes(CharsetUtil.UTF_8);
        byte[] address = event.getAddress().getBytes(CharsetUtil.UTF_8);

        // send name+address
        byte[] arr = new byte[address.length + 2 + name.length];
        arr[0] = 1;
        System.arraycopy(name, 0, arr, 1, name.length);
        arr[name.length + 1] = Event.SEPERATOR;
        System.arraycopy(address, 0, arr, name.length + 2, address.length);
        ByteBuf buf = Unpooled.copiedBuffer(arr);
        out.add(new DatagramPacket(buf, remoteAddress));

        // send status+running+total+name
        arr = new byte[name.length + 10];
        arr[0] = 2;
        arr[1] = statuscode;
        System.arraycopy(running, 0, arr, 2, 4);
        System.arraycopy(total, 0, arr, 6, 4);
        System.arraycopy(name, 0, arr, 10, name.length);
        ByteBuf buf2 = Unpooled.copiedBuffer(arr);
        out.add(new DatagramPacket(buf2, remoteAddress));
    }

    private byte[] intToBytes(int val) {
        byte[] arr = new byte[4];
        arr[0] = (byte) ((val & 0xFF000000) >> 24);
        arr[1] = (byte) ((val & 0x00FF0000) >> 16);
        arr[2] = (byte) ((val & 0x0000FF00) >> 8);
        arr[3] = (byte) ((val & 0x000000FF));
        return arr;
    }
}
