package dk.syslab.supv.rpc;

import dk.syslab.controller.rpc.protobuf.Messages;
import dk.syslab.controller.rpc.protobuf.XmlRpcGrpc;
import dk.syslab.supv.rpc.model.xmlrpc.ProcessInfo;
import dk.syslab.supv.rpc.model.xmlrpc.ProcessStatus;
import dk.syslab.supv.rpc.model.xmlrpc.SupervisorInfo;
import dk.syslab.supv.rpc.model.xmlrpc.TailLog;
import dk.syslab.supv.web.api.model.Log;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("Duplicates")
@Service
public class XmlRpcService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RpcChannelService channelService;

    public boolean clearMainLog(String host, String token) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
        Messages.Result result = stub.clearLog(Messages.Token.newBuilder().setToken(token).build());
        if (result.getSuccess()) {
            return true;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public boolean shutdown(String host, String token) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
        Messages.Result result = stub.shutdown(Messages.Token.newBuilder().setToken(token).build());
        if (result.getSuccess()) {
            return true;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public boolean restart(String host, String token) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
        Messages.Result result = stub.restart(Messages.Token.newBuilder().setToken(token).build());
        if (result.getSuccess()) {
            return true;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public Log readMainLog(String host, int offset, int length) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
        Messages.LogResult result = stub.readMainLog(Messages.LogRequest.newBuilder()
            .setOffset(offset)
            .setLength(length)
            .build());
        if (result.getSuccess()) {
            return new Log(result.getLog());
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public Map<String, List<String>> reloadConfig(String host, String token) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
        Messages.ReloadResult result = stub.reloadConfig(Messages.Token.newBuilder().setToken(token).build());
        if (result.getSuccess()) {
            Map<String, List<String>> map = new HashMap<>();
            map.put("added", result.getAddedList());
            map.put("changed", result.getChangedList());
            map.put("removed", result.getRemovedList());
            return map;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public boolean startProcess(String host, String token, String name, boolean wait) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
        Messages.Result result = stub.startProcess(Messages.NameRequest.newBuilder().setName(name).setToken(token).setWait(wait).build());
        if (result.getSuccess()) {
            return true;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public boolean stopProcess(String host, String token, String name, boolean wait) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
        Messages.Result result = stub.stopProcess(Messages.NameRequest.newBuilder().setName(name).setToken(token).setWait(wait).build());
        if (result.getSuccess()) {
            return true;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public List<ProcessStatus> startProcessGroup(String host, String token, String name, boolean wait) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
        Messages.ProcessStatusses result = stub.startProcessGroup(Messages.NameRequest.newBuilder().setName(name).setToken(token).setWait(wait).build());
        if (result.getSuccess()) {
            List<ProcessStatus> list = new ArrayList<>();
            for (Messages.ProcessStatus status : result.getProcessStatusList()) {
                ProcessStatus processStatus = new ProcessStatus();
                processStatus.setName(status.getName());
                processStatus.setGroup(status.getGroup());
                processStatus.setDescription(status.getDescription());
                processStatus.setStatus(status.getStatus());
                list.add(processStatus);
            }
            return list;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public List<ProcessStatus> stopProcessGroup(String host, String token, String name, boolean wait) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
        Messages.ProcessStatusses result = stub.stopProcessGroup(Messages.NameRequest.newBuilder().setName(name).setToken(token).setWait(wait).build());
        if (result.getSuccess()) {
            List<ProcessStatus> list = new ArrayList<>();
            for (Messages.ProcessStatus status : result.getProcessStatusList()) {
                ProcessStatus processStatus = new ProcessStatus();
                processStatus.setName(status.getName());
                processStatus.setGroup(status.getGroup());
                processStatus.setDescription(status.getDescription());
                processStatus.setStatus(status.getStatus());
                list.add(processStatus);
            }
            return list;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public boolean signalProcess(String host, String token, String name, String signal) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
        Messages.Result result = stub.signalProcess(Messages.NameDataRequest.newBuilder().setName(name).setData(signal).setToken(token).build());
        if (result.getSuccess()) {
            return true;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public List<ProcessStatus> signalProcessGroup(String host, String token, String name, String signal) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
        Messages.ProcessStatusses result = stub.signalProcessGroup(Messages.NameDataRequest.newBuilder().setName(name).setData(signal).setToken(token).build());
        if (result.getSuccess()) {
            List<ProcessStatus> list = new ArrayList<>();
            for (Messages.ProcessStatus status : result.getProcessStatusList()) {
                ProcessStatus processStatus = new ProcessStatus();
                processStatus.setName(status.getName());
                processStatus.setGroup(status.getGroup());
                processStatus.setDescription(status.getDescription());
                processStatus.setStatus(status.getStatus());
                list.add(processStatus);
            }
            return list;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public List<ProcessStatus> signalAllProcesses(String host, String token, String signal) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
        Messages.ProcessStatusses result = stub.signalAllProcesses(Messages.NameDataRequest.newBuilder().setData(signal).setToken(token).build());
        if (result.getSuccess()) {
            List<ProcessStatus> list = new ArrayList<>();
            for (Messages.ProcessStatus status : result.getProcessStatusList()) {
                ProcessStatus processStatus = new ProcessStatus();
                processStatus.setName(status.getName());
                processStatus.setGroup(status.getGroup());
                processStatus.setDescription(status.getDescription());
                processStatus.setStatus(status.getStatus());
                list.add(processStatus);
            }
            return list;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public boolean update(String host, String token) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
        Messages.Result result = stub.update(Messages.Token.newBuilder().setToken(token).build());
        if (result.getSuccess()) {
            return true;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public boolean sendProcessStdin(String host, String token, String name, String message) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
        Messages.Result result = stub.sendProcessStdin(Messages.NameDataRequest.newBuilder().setName(name).setData(message).setToken(token).build());
        if (result.getSuccess()) {
            return true;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public boolean sendRemoteCommEvent(String host, String token, String type, String message) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
        Messages.Result result = stub.sendRemoteCommEvent(Messages.NameDataRequest.newBuilder().setData(message).setType(type).setToken(token).build());
        if (result.getSuccess()) {
            return true;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public SupervisorInfo getSupervisorInfo(String host) {
        ManagedChannel channel = channelService.getChannel(host);
        XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
        Messages.SupervisorInfo result = stub.getSupervisorInfo(Messages.Token.newBuilder().build());
        if (result.getSuccess()) {
            SupervisorInfo info = new SupervisorInfo();
            info.setApiVersion(result.getApiVersion());
            info.setPackageVersion(result.getPackageVersion());
            info.setIdentifier(result.getIdentifier());
            info.setState(result.getState());
            info.setPid(result.getPid());
            return info;
        } else {
            return null;
        }
    }

    public ProcessInfo getProcessInfo(String host, String name) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
        Messages.ProcessInfo result = stub.getProcessInfo(Messages.NameRequest.newBuilder().setName(name).build());
        if (result.getSuccess()) {
            ProcessInfo info = new ProcessInfo();
            info.setName(result.getName());
            info.setGroup(result.getGroup());
            info.setDescription(result.getDescription());
            info.setStart(result.getStart());
            info.setStop(result.getStop());
            info.setNow(result.getNow());
            info.setStatename(result.getStatename());
            info.setState(result.getState());
            info.setSpawnerr(result.getSpawnerr());
            info.setExitstatus(result.getExitstatus());
            info.setLogfile(result.getLogfile());
            info.setStdOutLogfile(result.getStdOutLogfile());
            info.setStdErrLogfile(result.getStdErrLogfile());
            info.setPid(result.getPid());
            return info;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public List<ProcessInfo> getAllProcessInfo(String host) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
        Messages.ProcessInfos result = stub.getAllProcessInfo(Messages.Token.newBuilder().build());
        if (result.getSuccess()) {
            List<ProcessInfo> infos = new ArrayList<>();
            for (Messages.ProcessInfo res : result.getProcessInfoList()) {
                ProcessInfo info = new ProcessInfo();
                info.setName(res.getName());
                info.setGroup(res.getGroup());
                info.setDescription(res.getDescription());
                info.setStart(res.getStart());
                info.setStop(res.getStop());
                info.setNow(res.getNow());
                info.setStatename(res.getStatename());
                info.setState(res.getState());
                info.setSpawnerr(res.getSpawnerr());
                info.setExitstatus(res.getExitstatus());
                info.setLogfile(res.getLogfile());
                info.setStdOutLogfile(res.getStdOutLogfile());
                info.setStdErrLogfile(res.getStdErrLogfile());
                info.setPid(res.getPid());
                infos.add(info);
            }
            return infos;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public Log readProcessStdoutLog(String host, String name, int offset, int length) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
        Messages.LogResult result = stub.readLog(Messages.LogRequest.newBuilder()
            .setName(name)
            .setOffset(offset)
            .setLength(length)
            .build());
        if (result.getSuccess()) {
            return new Log(result.getLog());
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public Log readProcessStderrLog(String host, String name, int offset, int length) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
        Messages.LogResult result = stub.readErrorLog(Messages.LogRequest.newBuilder()
            .setName(name)
            .setOffset(offset)
            .setLength(length)
            .build());
        if (result.getSuccess()) {
            return new Log(result.getLog());
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public TailLog tailProcessStdoutLog(String host, String name, int offset, int length) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
        Messages.TailLogResult result = stub.tailLog(Messages.LogRequest.newBuilder()
            .setName(name)
            .setOffset(offset)
            .setLength(length)
            .build());
        if (result.getSuccess()) {
            TailLog tailLog = new TailLog();
            tailLog.setLog(result.getLog());
            tailLog.setOffset(result.getOffset());
            tailLog.setOverflow(result.getOverflow());
            return tailLog;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public TailLog tailProcessStderrLog(String host, String name, int offset, int length) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
        Messages.TailLogResult result = stub.tailErrorLog(Messages.LogRequest.newBuilder()
            .setName(name)
            .setOffset(offset)
            .setLength(length)
            .build());
        if (result.getSuccess()) {
            TailLog tailLog = new TailLog();
            tailLog.setLog(result.getLog());
            tailLog.setOffset(result.getOffset());
            tailLog.setOverflow(result.getOverflow());
            return tailLog;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public boolean clearProcessLogs(String host, String token, String name) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
        Messages.Result result = stub.clearProcesslogs(Messages.NameRequest.newBuilder().setName(name).setToken(token).build());
        if (result.getSuccess()) {
            return true;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public List<ProcessStatus> clearAllProcessLogs(String host, String token) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
        Messages.ProcessStatusses result = stub.clearAllProcesslogs(Messages.Token.newBuilder().setToken(token).build());
        if (result.getSuccess()) {
            List<ProcessStatus> list = new ArrayList<>();
            for (Messages.ProcessStatus status : result.getProcessStatusList()) {
                ProcessStatus processStatus = new ProcessStatus();
                processStatus.setName(status.getName());
                processStatus.setGroup(status.getGroup());
                processStatus.setDescription(status.getDescription());
                processStatus.setStatus(status.getStatus());
                list.add(processStatus);
            }
            return list;
        } else {
            throw new IOException(result.getMessage());
        }
    }
}
