package dk.syslab.controller.rpc;

import dk.syslab.controller.rpc.protobuf.Messages;
import dk.syslab.controller.rpc.protobuf.XmlRpcGrpc;
import dk.syslab.controller.validation.ValidationException;
import dk.syslab.controller.validation.ValidationService;
import dk.syslab.controller.xmlrpc.*;
import io.grpc.stub.StreamObserver;
import io.jsonwebtoken.Claims;
import org.apache.xmlrpc.XmlRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@SuppressWarnings("Duplicates")
public class XmlRpc extends XmlRpcGrpc.XmlRpcImplBase {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private XmlRpcService xmlRpcService;
    private ValidationService validationService;

    public XmlRpc(ValidationService validationService, XmlRpcService xmlRpcService) {
        this.validationService = validationService;
        this.xmlRpcService = xmlRpcService;
    }

    @Override
    public void clearLog(Messages.Token request, StreamObserver<Messages.Result> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                if (validationService.isAdmin(claims)) {
                    xmlRpcService.clearLog();
                    responseObserver.onNext(Messages.Result.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK).build());
                } else {
                    responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("You need ADMINISTRATIVE privileges to perform this command!").build());
                }
            } else {
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not authenticated").build());
            }
        } catch (XmlRpcException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } catch (ValidationException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void shutdown(Messages.Token request, StreamObserver<Messages.Result> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                if (validationService.isAdmin(claims)) {
                    xmlRpcService.shutdown();
                    responseObserver.onNext(Messages.Result.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK).build());
                } else {
                    responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("You need ADMINISTRATIVE privileges to perform this command!").build());
                }
            } else {
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not authenticated").build());
            }
        } catch (XmlRpcException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } catch (ValidationException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void restart(Messages.Token request, StreamObserver<Messages.Result> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                if (validationService.isAdmin(claims)) {
                    xmlRpcService.restart();
                    responseObserver.onNext(Messages.Result.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK).build());
                } else {
                    responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("You need ADMINISTRATIVE privileges to perform this command!").build());
                }
            } else {
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not authenticated").build());
            }
        } catch (XmlRpcException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } catch (ValidationException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void reloadConfig(Messages.Token request, StreamObserver<Messages.ReloadResult> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                if (validationService.isAdmin(claims)) {
                    Map<String, List<String>> res = xmlRpcService.reloadConfig();
                    responseObserver.onNext(Messages.ReloadResult.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK)
                        .addAllAdded(res.get("added"))
                        .addAllChanged(res.get("changed"))
                        .addAllRemoved(res.get("removed"))
                        .build());
                } else {
                    responseObserver.onNext(Messages.ReloadResult.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("You need ADMINISTRATIVE privileges to perform this command!").build());
                }
            } else {
                responseObserver.onNext(Messages.ReloadResult.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not authenticated").build());
            }
        } catch (XmlRpcException e) {
            responseObserver.onNext(Messages.ReloadResult.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } catch (ValidationException e) {
            responseObserver.onNext(Messages.ReloadResult.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void readMainLog(Messages.LogRequest request, StreamObserver<Messages.LogResult> responseObserver) {
        try {
            String log = xmlRpcService.readLog(request.getOffset(), request.getLength());
            responseObserver.onNext(Messages.LogResult.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK)
                .setLog(log)
                .build());
        } catch (XmlRpcException e) {
            responseObserver.onNext(Messages.LogResult.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getProcessInfo(Messages.NameRequest request, StreamObserver<Messages.ProcessInfo> responseObserver) {
        try {
            ProcessInfo info = xmlRpcService.getProcessInfo(request.getName());
            responseObserver.onNext(Messages.ProcessInfo.newBuilder().setSuccess(true).setResultCode(Messages.ResultCode.OK)
                .setName(info.getName())
                .setGroup(info.getGroup())
                .setDescription(info.getDescription())
                .setStart(info.getStart())
                .setStop(info.getStop())
                .setNow(info.getNow())
                .setStatename(info.getStatename())
                .setState(info.getState())
                .setSpawnerr(info.getSpawnerr())
                .setExitstatus(info.getExitstatus())
                .setLogfile(info.getLogfile())
                .setStdOutLogfile(info.getStdOutLogfile())
                .setStdErrLogfile(info.getStdErrLogfile())
                .setPid(info.getPid())
                .build());
        } catch (XmlRpcException e) {
            responseObserver.onNext(Messages.ProcessInfo.newBuilder().setSuccess(false).setResultCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getAllProcessInfo(Messages.Token request, StreamObserver<Messages.ProcessInfos> responseObserver) {
        try {
            List<ProcessInfo> list = xmlRpcService.getAllProcessInfo();
            Messages.ProcessInfos.Builder builder = Messages.ProcessInfos.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK);
            for (ProcessInfo info : list) {
                Messages.ProcessInfo processInfo = Messages.ProcessInfo.newBuilder()
                    .setName(info.getName())
                    .setGroup(info.getGroup())
                    .setDescription(info.getDescription())
                    .setStart(info.getStart())
                    .setStop(info.getStop())
                    .setNow(info.getNow())
                    .setStatename(info.getStatename())
                    .setState(info.getState())
                    .setSpawnerr(info.getSpawnerr())
                    .setExitstatus(info.getExitstatus())
                    .setLogfile(info.getLogfile())
                    .setStdOutLogfile(info.getStdOutLogfile())
                    .setStdErrLogfile(info.getStdErrLogfile())
                    .setPid(info.getPid())
                    .build();
                builder.addProcessInfo(processInfo);
            }
            responseObserver.onNext(builder.build());
        } catch (XmlRpcException e) {
            responseObserver.onNext(Messages.ProcessInfos.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void startProcess(Messages.NameRequest request, StreamObserver<Messages.Result> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                xmlRpcService.startProcess(request.getName(), request.getWait());
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK).build());
            } else {
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not authenticated").build());
            }
        } catch (XmlRpcException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } catch (ValidationException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void startAllProcesses(Messages.NameRequest request, StreamObserver<Messages.ProcessStatusses> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                List<ProcessStatus> statuses = xmlRpcService.startAllProcesses(request.getWait());
                Messages.ProcessStatusses.Builder builder = Messages.ProcessStatusses.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK);
                for (ProcessStatus status : statuses) {
                    Messages.ProcessStatus processStatus = Messages.ProcessStatus.newBuilder()
                        .setName(status.getName())
                        .setGroup(status.getGroup())
                        .setDescription(status.getDescription())
                        .setStatus(status.getStatus())
                        .build();
                    builder.addProcessStatus(processStatus);
                }
                responseObserver.onNext(builder.build());
            } else {
                responseObserver.onNext(Messages.ProcessStatusses.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not authenticated").build());
            }
        } catch (XmlRpcException e) {
            responseObserver.onNext(Messages.ProcessStatusses.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } catch (ValidationException e) {
            responseObserver.onNext(Messages.ProcessStatusses.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void startProcessGroup(Messages.NameRequest request, StreamObserver<Messages.ProcessStatusses> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                List<ProcessStatus> statuses = xmlRpcService.startProcessGroup(request.getName(), request.getWait());
                Messages.ProcessStatusses.Builder builder = Messages.ProcessStatusses.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK);
                for (ProcessStatus status : statuses) {
                    Messages.ProcessStatus processStatus = Messages.ProcessStatus.newBuilder()
                        .setName(status.getName())
                        .setGroup(status.getGroup())
                        .setDescription(status.getDescription())
                        .setStatus(status.getStatus())
                        .build();
                    builder.addProcessStatus(processStatus);
                }
                responseObserver.onNext(builder.build());
            } else {
                responseObserver.onNext(Messages.ProcessStatusses.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not authenticated").build());
            }
        } catch (XmlRpcException e) {
            responseObserver.onNext(Messages.ProcessStatusses.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } catch (ValidationException e) {
            responseObserver.onNext(Messages.ProcessStatusses.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void stopProcess(Messages.NameRequest request, StreamObserver<Messages.Result> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                xmlRpcService.stopProcess(request.getName(), request.getWait());
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK).build());
            } else {
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not authenticated").build());
            }
        } catch (XmlRpcException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } catch (ValidationException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void stopAllProcesses(Messages.NameRequest request, StreamObserver<Messages.ProcessStatusses> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                List<ProcessStatus> statuses = xmlRpcService.stopAllProcesses(request.getWait());
                Messages.ProcessStatusses.Builder builder = Messages.ProcessStatusses.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK);
                for (ProcessStatus status : statuses) {
                    Messages.ProcessStatus processStatus = Messages.ProcessStatus.newBuilder()
                        .setName(status.getName())
                        .setGroup(status.getGroup())
                        .setDescription(status.getDescription())
                        .setStatus(status.getStatus())
                        .build();
                    builder.addProcessStatus(processStatus);
                }
                responseObserver.onNext(builder.build());
            } else {
                responseObserver.onNext(Messages.ProcessStatusses.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not authenticated").build());
            }
        } catch (XmlRpcException e) {
            responseObserver.onNext(Messages.ProcessStatusses.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } catch (ValidationException e) {
            responseObserver.onNext(Messages.ProcessStatusses.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void stopProcessGroup(Messages.NameRequest request, StreamObserver<Messages.ProcessStatusses> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                List<ProcessStatus> statuses = xmlRpcService.stopProcessGroup(request.getName(), request.getWait());
                Messages.ProcessStatusses.Builder builder = Messages.ProcessStatusses.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK);
                for (ProcessStatus status : statuses) {
                    Messages.ProcessStatus processStatus = Messages.ProcessStatus.newBuilder()
                        .setName(status.getName())
                        .setGroup(status.getGroup())
                        .setDescription(status.getDescription())
                        .setStatus(status.getStatus())
                        .build();
                    builder.addProcessStatus(processStatus);
                }
                responseObserver.onNext(builder.build());
            } else {
                responseObserver.onNext(Messages.ProcessStatusses.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not authenticated").build());
            }
        } catch (XmlRpcException e) {
            responseObserver.onNext(Messages.ProcessStatusses.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } catch (ValidationException e) {
            responseObserver.onNext(Messages.ProcessStatusses.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void signalProcess(Messages.NameDataRequest request, StreamObserver<Messages.Result> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                xmlRpcService.signalProcess(request.getName(), request.getData());
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK).build());
            } else {
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not authenticated").build());
            }
        } catch (XmlRpcException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } catch (ValidationException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void signalProcessGroup(Messages.NameDataRequest request, StreamObserver<Messages.ProcessStatusses> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                List<ProcessStatus> statuses = xmlRpcService.signalProcessGroup(request.getName(), request.getData());
                Messages.ProcessStatusses.Builder builder = Messages.ProcessStatusses.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK);
                for (ProcessStatus status : statuses) {
                    Messages.ProcessStatus processStatus = Messages.ProcessStatus.newBuilder()
                        .setName(status.getName())
                        .setGroup(status.getGroup())
                        .setDescription(status.getDescription())
                        .setStatus(status.getStatus())
                        .build();
                    builder.addProcessStatus(processStatus);
                }
                responseObserver.onNext(builder.build());
            } else {
                responseObserver.onNext(Messages.ProcessStatusses.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not authenticated").build());
            }
        } catch (XmlRpcException e) {
            responseObserver.onNext(Messages.ProcessStatusses.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } catch (ValidationException e) {
            responseObserver.onNext(Messages.ProcessStatusses.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void signalAllProcesses(Messages.NameDataRequest request, StreamObserver<Messages.ProcessStatusses> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                List<ProcessStatus> statuses = xmlRpcService.signalAllProcesses(request.getData());
                Messages.ProcessStatusses.Builder builder = Messages.ProcessStatusses.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK);
                for (ProcessStatus status : statuses) {
                    Messages.ProcessStatus processStatus = Messages.ProcessStatus.newBuilder()
                        .setName(status.getName())
                        .setGroup(status.getGroup())
                        .setDescription(status.getDescription())
                        .setStatus(status.getStatus())
                        .build();
                    builder.addProcessStatus(processStatus);
                }
                responseObserver.onNext(builder.build());
            } else {
                responseObserver.onNext(Messages.ProcessStatusses.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not authenticated").build());
            }
        } catch (XmlRpcException e) {
            responseObserver.onNext(Messages.ProcessStatusses.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } catch (ValidationException e) {
            responseObserver.onNext(Messages.ProcessStatusses.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void sendProcessStdin(Messages.NameDataRequest request, StreamObserver<Messages.Result> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                xmlRpcService.sendProcessStdin(request.getName(), request.getData());
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK).build());
            } else {
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not authenticated").build());
            }
        } catch (XmlRpcException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } catch (ValidationException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void sendRemoteCommEvent(Messages.NameDataRequest request, StreamObserver<Messages.Result> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                xmlRpcService.sendRemoteCommEvent(request.getType(), request.getData());
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK).build());
            } else {
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not authenticated").build());
            }
        } catch (XmlRpcException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } catch (ValidationException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void addProcessGroup(Messages.NameRequest request, StreamObserver<Messages.Result> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                xmlRpcService.addProcessGroup(request.getName());
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK).build());
            } else {
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not authenticated").build());
            }
        } catch (XmlRpcException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } catch (ValidationException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void removeProcessGroup(Messages.NameRequest request, StreamObserver<Messages.Result> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                xmlRpcService.removeProcessGroup(request.getName());
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK).build());
            } else {
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not authenticated").build());
            }
        } catch (XmlRpcException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } catch (ValidationException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void readLog(Messages.LogRequest request, StreamObserver<Messages.LogResult> responseObserver) {
        try {
            String log = xmlRpcService.readProcessStdoutLog(request.getName(), request.getOffset(), request.getLength());
            responseObserver.onNext(Messages.LogResult.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK)
                .setLog(log)
                .build());
        } catch (XmlRpcException e) {
            responseObserver.onNext(Messages.LogResult.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void readErrorLog(Messages.LogRequest request, StreamObserver<Messages.LogResult> responseObserver) {
        try {
            String log = xmlRpcService.readProcessStderrLog(request.getName(), request.getOffset(), request.getLength());
            responseObserver.onNext(Messages.LogResult.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK)
                .setLog(log)
                .build());
        } catch (XmlRpcException e) {
            responseObserver.onNext(Messages.LogResult.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void tailLog(Messages.LogRequest request, StreamObserver<Messages.TailLogResult> responseObserver) {
        try {
            TailLog log = xmlRpcService.tailProcessStdoutLog(request.getName(), request.getOffset(), request.getLength());
            responseObserver.onNext(Messages.TailLogResult.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK)
                .setLog(log.getLog())
                .setOffset(log.getOffset())
                .setOverflow(log.isOverflow())
                .build());
        } catch (XmlRpcException e) {
            responseObserver.onNext(Messages.TailLogResult.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void tailErrorLog(Messages.LogRequest request, StreamObserver<Messages.TailLogResult> responseObserver) {
        try {
            TailLog log = xmlRpcService.tailProcessStderrLog(request.getName(), request.getOffset(), request.getLength());
            responseObserver.onNext(Messages.TailLogResult.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK)
                .setLog(log.getLog())
                .setOffset(log.getOffset())
                .setOverflow(log.isOverflow())
                .build());
        } catch (XmlRpcException e) {
            responseObserver.onNext(Messages.TailLogResult.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void clearProcesslogs(Messages.NameRequest request, StreamObserver<Messages.Result> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                xmlRpcService.clearProcessLogs(request.getName());
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK).build());
            } else {
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not authenticated").build());
            }
        } catch (XmlRpcException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } catch (ValidationException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void clearAllProcesslogs(Messages.Token request, StreamObserver<Messages.ProcessStatusses> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                List<ProcessStatus> statuses = xmlRpcService.clearAllProcessLogs();
                Messages.ProcessStatusses.Builder builder = Messages.ProcessStatusses.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK);
                for (ProcessStatus status : statuses) {
                    Messages.ProcessStatus processStatus = Messages.ProcessStatus.newBuilder()
                        .setName(status.getName())
                        .setGroup(status.getGroup())
                        .setDescription(status.getDescription())
                        .setStatus(status.getStatus())
                        .build();
                    builder.addProcessStatus(processStatus);
                }
                responseObserver.onNext(builder.build());
            } else {
                responseObserver.onNext(Messages.ProcessStatusses.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not authenticated").build());
            }
        } catch (XmlRpcException e) {
            responseObserver.onNext(Messages.ProcessStatusses.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } catch (ValidationException e) {
            responseObserver.onNext(Messages.ProcessStatusses.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void update(Messages.Token request, StreamObserver<Messages.Result> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                xmlRpcService.update();
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK).build());
            } else {
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not authenticated").build());
            }
        } catch (XmlRpcException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } catch (ValidationException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getSupervisorInfo(Messages.Token request, StreamObserver<Messages.SupervisorInfo> responseObserver) {
        try {
            SupervisorInfo info = xmlRpcService.currentSupervisorInfo();
            Messages.SupervisorInfo.Builder builder = Messages.SupervisorInfo.newBuilder().setSuccess(true).setResultCode(Messages.ResultCode.OK)
                .setApiVersion(info.getApiVersion())
                .setPackageVersion(info.getPackageVersion())
                .setIdentifier(info.getIdentifier())
                .setState(info.getState())
                .setCode(info.getCode())
                .setPid(info.getPid());
            responseObserver.onNext(builder.build());
        } finally {
            responseObserver.onCompleted();
        }
    }
}
