package dk.syslab.supv.rpc;

import dk.syslab.controller.rpc.protobuf.DistributedMessages;
import dk.syslab.controller.rpc.protobuf.DistributedRpcGrpc;
import dk.syslab.controller.rpc.protobuf.Messages;
import dk.syslab.supv.rpc.model.xmlrpc.ProcessInfo;
import dk.syslab.supv.rpc.model.xmlrpc.ProcessStatus;
import dk.syslab.supv.rpc.model.xmlrpc.TailLog;
import dk.syslab.supv.web.api.model.FineUploaderProgram;
import dk.syslab.supv.web.api.model.distributed.ResultProcessInfo;
import dk.syslab.supv.web.api.model.distributed.ResultProcessStatus;
import dk.syslab.supv.web.api.model.distributed.ResultStatus;
import dk.syslab.supv.web.api.model.distributed.ResultTailLog;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@SuppressWarnings("Duplicates")
@Service
public class DistributedRpcService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    RpcChannelService channelService;
    private Random rand;

    @Autowired
    BroadcastRpcService broadcastRpcService;

    public DistributedRpcService(RpcChannelService channelService) {
        this.channelService = channelService;
        rand = new Random();
    }

    public String selectHostFromNodeList(List<String> nodes) {
        if (nodes == null || nodes.size() <= 0) return  null;
        if (nodes.size() == 1) return broadcastRpcService.lookUpAddress(nodes.get(0));
        int idx = rand.nextInt(nodes.size());
        return broadcastRpcService.lookUpAddress(nodes.get(idx));
    }

    public Map<String, ResultStatus> startPrograms(String token, List<String> nodes, String name, boolean wait) throws IOException {
        ManagedChannel channel = channelService.getChannel(selectHostFromNodeList(nodes));
        DistributedRpcGrpc.DistributedRpcBlockingStub stub = DistributedRpcGrpc.newBlockingStub(channel);
        DistributedMessages.DistributedResultStatus result = stub.distributedStartProgram(DistributedMessages.DistributedNameRequest.newBuilder().setName(name).setToken(token).addAllNodes(nodes).setWait(wait).build());
        if (result.getSuccess()) {
            Map<String, ResultStatus> map = new HashMap<>();
            for (Map.Entry<String, DistributedMessages.ResultStatus> entry : result.getResultsMap().entrySet()) {
                ResultStatus status = new ResultStatus(entry.getValue().getName(), entry.getValue().getSuccess(), entry.getValue().getMessage());
                map.put(entry.getKey(), status);
            }
            return map;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public Map<String, ResultStatus> stopPrograms(String token, List<String> nodes, String name, boolean wait) throws IOException {
        ManagedChannel channel = channelService.getChannel(selectHostFromNodeList(nodes));
        DistributedRpcGrpc.DistributedRpcBlockingStub stub = DistributedRpcGrpc.newBlockingStub(channel);
        DistributedMessages.DistributedResultStatus result = stub.distributedStopProgram(DistributedMessages.DistributedNameRequest.newBuilder().setName(name).setToken(token).addAllNodes(nodes).setWait(wait).build());
        if (result.getSuccess()) {
            Map<String, ResultStatus> map = new HashMap<>();
            for (Map.Entry<String, DistributedMessages.ResultStatus> entry : result.getResultsMap().entrySet()) {
                ResultStatus status = new ResultStatus(entry.getValue().getName(), entry.getValue().getSuccess(), entry.getValue().getMessage());
                map.put(entry.getKey(), status);
            }
            return map;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public SortedMap<String, ResultProcessStatus> startProgramGroups(String token, List<String> nodes, String name, boolean wait) throws IOException {
        ManagedChannel channel = channelService.getChannel(selectHostFromNodeList(nodes));
        DistributedRpcGrpc.DistributedRpcBlockingStub stub = DistributedRpcGrpc.newBlockingStub(channel);
        DistributedMessages.DistributedProcessStatusses result = stub.distributedStartProgramGroup(DistributedMessages.DistributedNameRequest.newBuilder().setName(name).setToken(token).addAllNodes(nodes).setWait(wait).build());
        if (result.getSuccess()) {
            SortedMap<String, ResultProcessStatus> map = new TreeMap<>(Comparator.naturalOrder());
            for (Map.Entry<String, Messages.ProcessStatusses> entry : result.getResultsMap().entrySet()) {
                ResultProcessStatus status = new ResultProcessStatus(entry.getKey(), entry.getValue().getSuccess(), entry.getValue().getMessage());
                List<ProcessStatus> list = new ArrayList<>();
                for (Messages.ProcessStatus processStatus : entry.getValue().getProcessStatusList()) {
                    ProcessStatus s = new ProcessStatus();
                    s.setName(processStatus.getName());
                    s.setGroup(processStatus.getGroup());
                    s.setStatus(processStatus.getStatus());
                    s.setDescription(processStatus.getDescription());
                    list.add(s);
                }
                status.setProcessStatus(list);
                map.put(entry.getKey(), status);
            }
            return map;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public SortedMap<String, ResultProcessStatus> stopProgramGroups(String token, List<String> nodes, String name, boolean wait) throws IOException {
        ManagedChannel channel = channelService.getChannel(selectHostFromNodeList(nodes));
        DistributedRpcGrpc.DistributedRpcBlockingStub stub = DistributedRpcGrpc.newBlockingStub(channel);
        DistributedMessages.DistributedProcessStatusses result = stub.distributedStopProgramGroup(DistributedMessages.DistributedNameRequest.newBuilder().setName(name).setToken(token).addAllNodes(nodes).setWait(wait).build());
        if (result.getSuccess()) {
            SortedMap<String, ResultProcessStatus> map = new TreeMap<>(Comparator.naturalOrder());
            for (Map.Entry<String, Messages.ProcessStatusses> entry : result.getResultsMap().entrySet()) {
                ResultProcessStatus status = new ResultProcessStatus(entry.getKey(), entry.getValue().getSuccess(), entry.getValue().getMessage());
                List<ProcessStatus> list = new ArrayList<>();
                for (Messages.ProcessStatus processStatus : entry.getValue().getProcessStatusList()) {
                    ProcessStatus s = new ProcessStatus();
                    s.setName(processStatus.getName());
                    s.setGroup(processStatus.getGroup());
                    s.setStatus(processStatus.getStatus());
                    s.setDescription(processStatus.getDescription());
                    list.add(s);
                }
                status.setProcessStatus(list);
                map.put(entry.getKey(), status);
            }
            return map;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public Map<String, ResultProcessInfo> getProcessInfo(List<String> nodes, String name) throws IOException {
        ManagedChannel channel = channelService.getChannel(selectHostFromNodeList(nodes));
        DistributedRpcGrpc.DistributedRpcBlockingStub stub = DistributedRpcGrpc.newBlockingStub(channel);
        DistributedMessages.DistributedProcessInfo result = stub.distributedGetProgramInfo(DistributedMessages.DistributedNameRequest.newBuilder().setName(name).addAllNodes(nodes).build());
        if (result.getSuccess()) {
            Map<String, ResultProcessInfo> map = new HashMap<>();
            for (Map.Entry<String, Messages.ProcessInfo> entry : result.getResultsMap().entrySet()) {
                ProcessInfo info = new ProcessInfo();
                info.setName(entry.getValue().getName());
                info.setGroup(entry.getValue().getGroup());
                info.setDescription(entry.getValue().getDescription());
                info.setStart(entry.getValue().getStart());
                info.setStop(entry.getValue().getStop());
                info.setNow(entry.getValue().getNow());
                info.setStatename(entry.getValue().getStatename());
                info.setState(entry.getValue().getState());
                info.setSpawnerr(entry.getValue().getSpawnerr());
                info.setExitstatus(entry.getValue().getExitstatus());
                info.setLogfile(entry.getValue().getLogfile());
                info.setStdOutLogfile(entry.getValue().getStdOutLogfile());
                info.setStdErrLogfile(entry.getValue().getStdErrLogfile());
                info.setPid(entry.getValue().getPid());
                ResultProcessInfo rsinfo = new ResultProcessInfo(entry.getKey(), result.getSuccess(), result.getMessage());
                map.put(entry.getKey(), rsinfo);
            }
            return map;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public Map<String, ResultStatus> uploadProgram(String host, String token, List<String> nodes, FineUploaderProgram program) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        DistributedRpcGrpc.DistributedRpcBlockingStub stub = DistributedRpcGrpc.newBlockingStub(channel);

        DistributedMessages.DistributedProgram.Builder builder = DistributedMessages.DistributedProgram.newBuilder()
            .setToken(token)
            .setName(program.getName())
            .setProgramUuid(program.getTransaction())
            .addAllNodes(nodes);
        if (program.getCommand() != null) builder.setCommand(program.getCommand());
        if (program.getPriority() != null) builder.setPriority(program.getPriority());
        if (program.getAutostart() != null) builder.setAutostart(program.getAutostart());
        if (program.getAutorestart() != null) builder.setAutorestart(program.getAutorestart());
        if (program.getStartsecs() != null) builder.setStartsecs(program.getStartsecs());
        if (program.getStartretries() != null) builder.setStartretries(program.getStartretries());
        if (program.getExitcodes() != null) builder.setExitcodes(program.getExitcodes());
        if (program.getStopwaitsecs() != null) builder.setStopwaitsecs(program.getStopwaitsecs());
        if (program.getEnvironment() != null) builder.setEnvironment(program.getEnvironment());
        if (program.getPaths() != null) builder.putAllPaths(program.getPaths());
        if (program.getTransforms() != null) builder.putAllTransforms(program.getTransforms());
        if (program.getUuidFilenames() != null) builder.putAllUuidFilenames(program.getUuidFilenames());
        if (program.getUuidPaths() != null) builder.putAllUuidPaths(program.getUuidPaths());

        DistributedMessages.DistributedResultStatus result = stub.distributedUploadProgram(builder.build());
        if (result.getSuccess()) {
            Map<String, ResultStatus> map = new HashMap<>();
            for (Map.Entry<String, DistributedMessages.ResultStatus> entry : result.getResultsMap().entrySet()) {
                ResultStatus status = new ResultStatus(entry.getValue().getName(), entry.getValue().getSuccess(), entry.getValue().getMessage());
                map.put(entry.getKey(), status);
            }
            return map;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public Map<String, ResultStatus> deleteProgram(String token, List<String> nodes, String name) throws IOException {
        ManagedChannel channel = channelService.getChannel(selectHostFromNodeList(nodes));
        DistributedRpcGrpc.DistributedRpcBlockingStub stub = DistributedRpcGrpc.newBlockingStub(channel);
        DistributedMessages.DistributedResultStatus result = stub.distributedDeleteProgram(DistributedMessages.DistributedNameRequest.newBuilder().setName(name).setToken(token).addAllNodes(nodes).build());
        if (result.getSuccess()) {
            Map<String, ResultStatus> map = new HashMap<>();
            for (Map.Entry<String, DistributedMessages.ResultStatus> entry : result.getResultsMap().entrySet()) {
                ResultStatus status = new ResultStatus(entry.getValue().getName(), entry.getValue().getSuccess(), entry.getValue().getMessage());
                map.put(entry.getKey(), status);
            }
            return map;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public Map<String, ResultStatus> signalProgram(String token, List<String> nodes, String name, String signal) throws IOException {
        ManagedChannel channel = channelService.getChannel(selectHostFromNodeList(nodes));
        DistributedRpcGrpc.DistributedRpcBlockingStub stub = DistributedRpcGrpc.newBlockingStub(channel);
        DistributedMessages.DistributedResultStatus result = stub.distributedSignalProgram(DistributedMessages.DistributedNameDataRequest.newBuilder().setName(name).setToken(token).addAllNodes(nodes).setData(signal).build());
        if (result.getSuccess()) {
            Map<String, ResultStatus> map = new HashMap<>();
            for (Map.Entry<String, DistributedMessages.ResultStatus> entry : result.getResultsMap().entrySet()) {
                ResultStatus status = new ResultStatus(entry.getValue().getName(), entry.getValue().getSuccess(), entry.getValue().getMessage());
                map.put(entry.getKey(), status);
            }
            return map;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public Map<String, ResultStatus> sendMessage(String token, List<String> nodes, String name, String message) throws IOException {
        ManagedChannel channel = channelService.getChannel(selectHostFromNodeList(nodes));
        DistributedRpcGrpc.DistributedRpcBlockingStub stub = DistributedRpcGrpc.newBlockingStub(channel);
        DistributedMessages.DistributedResultStatus result = stub.distributedSendMessage(DistributedMessages.DistributedNameDataRequest.newBuilder().setName(name).setToken(token).addAllNodes(nodes).setData(message).build());
        if (result.getSuccess()) {
            Map<String, ResultStatus> map = new HashMap<>();
            for (Map.Entry<String, DistributedMessages.ResultStatus> entry : result.getResultsMap().entrySet()) {
                ResultStatus status = new ResultStatus(entry.getValue().getName(), entry.getValue().getSuccess(), entry.getValue().getMessage());
                map.put(entry.getKey(), status);
            }
            return map;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public Map<String, ResultStatus> sendRemoteCommEvent(String token, List<String> nodes, String type, String message) throws IOException {
        ManagedChannel channel = channelService.getChannel(selectHostFromNodeList(nodes));
        DistributedRpcGrpc.DistributedRpcBlockingStub stub = DistributedRpcGrpc.newBlockingStub(channel);
        DistributedMessages.DistributedResultStatus result = stub.distributedSendCommEvent(DistributedMessages.DistributedNameDataRequest.newBuilder().setToken(token).addAllNodes(nodes).setType(type).setData(message).build());
        if (result.getSuccess()) {
            Map<String, ResultStatus> map = new HashMap<>();
            for (Map.Entry<String, DistributedMessages.ResultStatus> entry : result.getResultsMap().entrySet()) {
                ResultStatus status = new ResultStatus(entry.getValue().getName(), entry.getValue().getSuccess(), entry.getValue().getMessage());
                map.put(entry.getKey(), status);
            }
            return map;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public Map<String, ResultStatus> restartSupervisors(String token, List<String> nodes) throws IOException {
        ManagedChannel channel = channelService.getChannel(selectHostFromNodeList(nodes));
        DistributedRpcGrpc.DistributedRpcBlockingStub stub = DistributedRpcGrpc.newBlockingStub(channel);
        DistributedMessages.DistributedResultStatus result = stub.distributedRestartSupervisor(DistributedMessages.DistributedToken.newBuilder().setToken(token).addAllNodes(nodes).build());
        if (result.getSuccess()) {
            Map<String, ResultStatus> map = new HashMap<>();
            for (Map.Entry<String, DistributedMessages.ResultStatus> entry : result.getResultsMap().entrySet()) {
                ResultStatus status = new ResultStatus(entry.getValue().getName(), entry.getValue().getSuccess(), entry.getValue().getMessage());
                map.put(entry.getKey(), status);
            }
            return map;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public Map<String, ResultStatus> clearProcessLogs(String token, List<String> nodes, String name) throws IOException {
        ManagedChannel channel = channelService.getChannel(selectHostFromNodeList(nodes));
        DistributedRpcGrpc.DistributedRpcBlockingStub stub = DistributedRpcGrpc.newBlockingStub(channel);
        DistributedMessages.DistributedResultStatus result = stub.distributedClearProcessLogs(DistributedMessages.DistributedNameRequest.newBuilder().setName(name).setToken(token).addAllNodes(nodes).build());
        if (result.getSuccess()) {
            Map<String, ResultStatus> map = new HashMap<>();
            for (Map.Entry<String, DistributedMessages.ResultStatus> entry : result.getResultsMap().entrySet()) {
                ResultStatus status = new ResultStatus(entry.getValue().getName(), entry.getValue().getSuccess(), entry.getValue().getMessage());
                map.put(entry.getKey(), status);
            }
            return map;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public Map<String, ResultStatus> update(String token, List<String> nodes) throws IOException {
        ManagedChannel channel = channelService.getChannel(selectHostFromNodeList(nodes));
        DistributedRpcGrpc.DistributedRpcBlockingStub stub = DistributedRpcGrpc.newBlockingStub(channel);
        DistributedMessages.DistributedResultStatus result = stub.distributedUpdate(DistributedMessages.DistributedToken.newBuilder().setToken(token).addAllNodes(nodes).build());
        if (result.getSuccess()) {
            Map<String, ResultStatus> map = new HashMap<>();
            for (Map.Entry<String, DistributedMessages.ResultStatus> entry : result.getResultsMap().entrySet()) {
                ResultStatus status = new ResultStatus(entry.getValue().getName(), entry.getValue().getSuccess(), entry.getValue().getMessage());
                map.put(entry.getKey(), status);
            }
            return map;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public Map<String, ResultTailLog> tailProcessStdoutLog(List<String> nodes, String name, int offset, int length) throws IOException {
        ManagedChannel channel = channelService.getChannel(selectHostFromNodeList(nodes));
        DistributedRpcGrpc.DistributedRpcBlockingStub stub = DistributedRpcGrpc.newBlockingStub(channel);
        DistributedMessages.DistributedTailLogResult result = stub.distributedTailLog(DistributedMessages.DistributedLogRequest.newBuilder().setName(name).addAllNodes(nodes).setOffset(offset).setLength(length).build());
        if (result.getSuccess()) {
            Map<String, ResultTailLog> map = new HashMap<>();
            for (Map.Entry<String, Messages.TailLogResult> entry : result.getResultsMap().entrySet()) {
                ResultTailLog resultTailLog = new ResultTailLog(entry.getKey(), entry.getValue().getSuccess(), entry.getValue().getMessage());
                TailLog tailLog = new TailLog();
                tailLog.setLog(entry.getValue().getLog());
                tailLog.setOffset(entry.getValue().getOffset());
                tailLog.setOverflow(entry.getValue().getOverflow());
                resultTailLog.setLog(tailLog);
                map.put(entry.getKey(), resultTailLog);
            }
            return map;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public Map<String, ResultTailLog> tailProcessStderrLog(List<String> nodes, String name, int offset, int length) throws IOException {
        ManagedChannel channel = channelService.getChannel(selectHostFromNodeList(nodes));
        DistributedRpcGrpc.DistributedRpcBlockingStub stub = DistributedRpcGrpc.newBlockingStub(channel);
        DistributedMessages.DistributedTailLogResult result = stub.distributedTailErrorLog(DistributedMessages.DistributedLogRequest.newBuilder().setName(name).addAllNodes(nodes).setOffset(offset).setLength(length).build());
        if (result.getSuccess()) {
            Map<String, ResultTailLog> map = new HashMap<>();
            for (Map.Entry<String, Messages.TailLogResult> entry : result.getResultsMap().entrySet()) {
                ResultTailLog resultTailLog = new ResultTailLog(entry.getKey(), entry.getValue().getSuccess(), entry.getValue().getMessage());
                TailLog tailLog = new TailLog();
                tailLog.setLog(entry.getValue().getLog());
                tailLog.setOffset(entry.getValue().getOffset());
                tailLog.setOverflow(entry.getValue().getOverflow());
                resultTailLog.setLog(tailLog);
                map.put(entry.getKey(), resultTailLog);
            }
            return map;
        } else {
            throw new IOException(result.getMessage());
        }
    }
}
