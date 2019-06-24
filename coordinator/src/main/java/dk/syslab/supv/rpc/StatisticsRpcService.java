package dk.syslab.supv.rpc;

import dk.syslab.controller.rpc.protobuf.Messages;
import dk.syslab.controller.rpc.protobuf.StatisticsRpcGrpc;
import dk.syslab.controller.rpc.protobuf.XmlRpcGrpc;
import dk.syslab.supv.rpc.model.statistics.*;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@SuppressWarnings("Duplicates")
@Service
public class StatisticsRpcService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RpcChannelService channelService;

    public ProcessStatistics getProcessStatistics(String host, String name) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        StatisticsRpcGrpc.StatisticsRpcBlockingStub stub = StatisticsRpcGrpc.newBlockingStub(channel);
        Messages.ProcessStatisticsResult result = stub.getProcessStatistics(Messages.NameRequest.newBuilder().setName(name).build());
        if (result.getSuccess()) {
            ProcessStatistics stats = new ProcessStatistics();
            ProcessData hour = new ProcessData(result.getHour().getLength());
            ProcessData weeks = new ProcessData(result.getWeeks().getLength());
            stats.addHour(name, hour);
            stats.addWeeks(name, weeks);

            hour.setTimestamp(result.getHour().getTimestamp());
            hour.setCpu(result.getHour().getCpuList());
            hour.setMemory(result.getHour().getMemoryList());
            hour.setVsz(result.getHour().getVszList());
            hour.setRss(result.getHour().getRssList());
            hour.setLength(result.getHour().getLength());

            weeks.setTimestamp(result.getWeeks().getTimestamp());
            weeks.setCpu(result.getWeeks().getCpuList());
            weeks.setMemory(result.getWeeks().getMemoryList());
            weeks.setVsz(result.getWeeks().getVszList());
            weeks.setRss(result.getWeeks().getRssList());
            weeks.setLength(result.getWeeks().getLength());

            return stats;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public SystemStatistics getSystemStatistics(String host) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        StatisticsRpcGrpc.StatisticsRpcBlockingStub stub = StatisticsRpcGrpc.newBlockingStub(channel);
        Messages.SystemStatisticsResult result = stub.getSystemStatistics(Messages.Token.newBuilder().build());
        if (result.getSuccess()) {
            SystemStatistics stats = new SystemStatistics();
            SystemData hour = new SystemData(result.getHour().getLength());
            SystemData weeks = new SystemData(result.getWeeks().getLength());
            stats.setHour(hour);
            stats.setWeeks(weeks);

            hour.setTimestamp(result.getHour().getTimestamp());
            hour.setUser(result.getHour().getUserList());
            hour.setNice(result.getHour().getNiceList());
            hour.setSystem(result.getHour().getSystemList());
            hour.setIdle(result.getHour().getIdleList());
            hour.setLoad1(result.getHour().getLoad1List());
            hour.setLoad2(result.getHour().getLoad2List());
            hour.setLoad3(result.getHour().getLoad3List());
            hour.setMemory(result.getHour().getMemoryList());
            hour.setSwap(result.getHour().getSwapList());
            hour.setLength(result.getHour().getLength());

            weeks.setTimestamp(result.getWeeks().getTimestamp());
            weeks.setUser(result.getWeeks().getUserList());
            weeks.setNice(result.getWeeks().getNiceList());
            weeks.setSystem(result.getWeeks().getSystemList());
            weeks.setIdle(result.getWeeks().getIdleList());
            weeks.setLoad1(result.getWeeks().getLoad1List());
            weeks.setLoad2(result.getWeeks().getLoad2List());
            weeks.setLoad3(result.getWeeks().getLoad3List());
            weeks.setMemory(result.getWeeks().getMemoryList());
            weeks.setSwap(result.getWeeks().getSwapList());
            weeks.setLength(result.getWeeks().getLength());

            return stats;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public SystemInformation getSystemInformation(String host) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        StatisticsRpcGrpc.StatisticsRpcBlockingStub stub = StatisticsRpcGrpc.newBlockingStub(channel);
        Messages.SystemInformationResult result = stub.getSystemInformation(Messages.Token.newBuilder().build());
        if (result.getSuccess()) {
            SystemInformation info = new SystemInformation();
            info.setOs(result.getOs());
            info.setManufacturer(result.getManufacturer());
            info.setBaseboardManufacturer(result.getBaseboardManufacturer());
            info.setBaseboardModel(result.getBaseboardModel());
            info.setCpu(result.getCpu());
            info.setLogicalCpu(result.getLogicalCpu());
            info.setMemory(result.getMemory());
            info.setMemoryStr(result.getMemoryStr());
            info.setSwap(result.getSwap());
            info.setSwapStr(result.getSwapStr());
            return info;
        } else {
            throw new IOException(result.getMessage());
        }
    }
}
