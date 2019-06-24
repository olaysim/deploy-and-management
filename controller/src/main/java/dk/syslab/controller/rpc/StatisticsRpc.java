package dk.syslab.controller.rpc;

import dk.syslab.controller.rpc.protobuf.Messages;
import dk.syslab.controller.rpc.protobuf.StatisticsRpcGrpc;
import dk.syslab.controller.statistics.ProcessData;
import dk.syslab.controller.statistics.StatisticsService;
import dk.syslab.controller.statistics.SystemData;
import dk.syslab.controller.statistics.SystemInformation;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticsRpc extends StatisticsRpcGrpc.StatisticsRpcImplBase {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private StatisticsService statisticsService;

    public StatisticsRpc(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @Override
    public void getProcessStatistics(Messages.NameRequest request, StreamObserver<Messages.ProcessStatisticsResult> responseObserver) {
        try {
            ProcessData hour = statisticsService.getProcessStatistics().getHour().get(request.getName());
            ProcessData weeks = statisticsService.getProcessStatistics().getWeeks().get(request.getName());
            if (weeks != null && hour != null) {
                Messages.ProcessStatisticsResult.ProcessData processDataHour = Messages.ProcessStatisticsResult.ProcessData.newBuilder()
                    .setTimestamp(hour.getTimestamp())
                    .addAllCpu(hour.getCpu())
                    .addAllMemory(hour.getMemory())
                    .addAllVsz(hour.getVsz())
                    .addAllRss(hour.getRss())
                    .setLength(hour.getLength())
                    .build();
                Messages.ProcessStatisticsResult.ProcessData processDataWeeks = Messages.ProcessStatisticsResult.ProcessData.newBuilder()
                    .setTimestamp(weeks.getTimestamp())
                    .addAllCpu(weeks.getCpu())
                    .addAllMemory(weeks.getMemory())
                    .addAllVsz(weeks.getVsz())
                    .addAllRss(weeks.getRss())
                    .setLength(weeks.getLength())
                    .build();
                responseObserver.onNext(Messages.ProcessStatisticsResult.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK)
                    .setHour(processDataHour)
                    .setWeeks(processDataWeeks)
                    .build());
            } else {
                responseObserver.onNext(Messages.ProcessStatisticsResult.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage("No statistics for " + request.getName()).build());
            }
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getSystemStatistics(Messages.Token request, StreamObserver<Messages.SystemStatisticsResult> responseObserver) {
        try {
            SystemData hour = statisticsService.getSystemStatistics().getHour();
            SystemData weeks = statisticsService.getSystemStatistics().getWeeks();
            if (weeks != null && hour != null) {
                Messages.SystemStatisticsResult.SystemData systemDataHour = Messages.SystemStatisticsResult.SystemData.newBuilder()
                    .setTimestamp(hour.getTimestamp())
                    .addAllUser(hour.getUser())
                    .addAllNice(hour.getNice())
                    .addAllSystem(hour.getSystem())
                    .addAllIdle(hour.getIdle())
                    .addAllLoad1(hour.getLoad1())
                    .addAllLoad2(hour.getLoad2())
                    .addAllLoad3(hour.getLoad3())
                    .addAllMemory(hour.getMemory())
                    .addAllSwap(hour.getSwap())
                    .setLength(hour.getLength())
                    .build();
                Messages.SystemStatisticsResult.SystemData systemDataWeeks = Messages.SystemStatisticsResult.SystemData.newBuilder()
                    .setTimestamp(weeks.getTimestamp())
                    .addAllUser(weeks.getUser())
                    .addAllNice(weeks.getNice())
                    .addAllSystem(weeks.getSystem())
                    .addAllIdle(weeks.getIdle())
                    .addAllLoad1(weeks.getLoad1())
                    .addAllLoad2(weeks.getLoad2())
                    .addAllLoad3(weeks.getLoad3())
                    .addAllMemory(weeks.getMemory())
                    .addAllSwap(weeks.getSwap())
                    .setLength(weeks.getLength())
                    .build();
                responseObserver.onNext(Messages.SystemStatisticsResult.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK)
                    .setHour(systemDataHour)
                    .setWeeks(systemDataWeeks)
                    .build());
            } else {
                responseObserver.onNext(Messages.SystemStatisticsResult.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage("No system statistics").build());
            }
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getSystemInformation(Messages.Token request, StreamObserver<Messages.SystemInformationResult> responseObserver) {
        try {
            SystemInformation info = statisticsService.getSystemInformation();
            if (info != null) {
                responseObserver.onNext(Messages.SystemInformationResult.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK)
                    .setOs(info.getOs())
                    .setManufacturer(info.getManufacturer())
                    .setBaseboardManufacturer(info.getBaseboardManufacturer())
                    .setBaseboardModel(info.getBaseboardModel())
                    .setCpu(info.getCpu())
                    .setLogicalCpu(info.getLogicalCpu())
                    .setMemory(info.getMemory())
                    .setMemoryStr(info.getMemoryStr())
                    .setSwap(info.getSwap())
                    .setSwapStr(info.getSwapStr())
                    .build());
            } else {
                responseObserver.onNext(Messages.SystemInformationResult.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage("No system information").build());
            }
        } finally {
            responseObserver.onCompleted();
        }
    }
}
