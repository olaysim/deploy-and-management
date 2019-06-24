package dk.syslab.controller.rpc;

import com.google.protobuf.ByteString;
import dk.syslab.controller.broadcast.BroadcastService;
import dk.syslab.controller.broadcast.Node;
import dk.syslab.controller.rpc.model.AsyncSuccess;
import dk.syslab.controller.rpc.model.FileUuidBytes;
import dk.syslab.controller.rpc.protobuf.*;
import dk.syslab.controller.storage.FileService;
import dk.syslab.controller.validation.ValidationException;
import dk.syslab.controller.validation.ValidationService;
import dk.syslab.controller.xmlrpc.ProcessInfo;
import dk.syslab.controller.xmlrpc.ProcessStatus;
import dk.syslab.controller.xmlrpc.TailLog;
import dk.syslab.controller.xmlrpc.XmlRpcService;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.jsonwebtoken.Claims;
import org.apache.xmlrpc.XmlRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@SuppressWarnings("Duplicates")
public class DistributedRpc extends DistributedRpcGrpc.DistributedRpcImplBase {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final static int TIME_OUT = 20;

    private ValidationService validationService;
    private FileService fileService;
    private BroadcastService broadcastService;
    private RpcChannelService channelService;
    private XmlRpcService xmlRpcService;
    private ExecutorService executorService;

    public DistributedRpc(ValidationService validationService, FileService fileService, BroadcastService broadcastService, RpcChannelService channelService, XmlRpcService xmlRpcService) {
        this.validationService = validationService;
        this.fileService = fileService;
        this.broadcastService = broadcastService;
        this.channelService = channelService;
        this.xmlRpcService = xmlRpcService;
        this.executorService = Executors.newCachedThreadPool();
    }

    private List<Node> getQueryList(List<String> nodes) {
        // check if "all" was given, in which case get all nodes from broadcast service
        // otherwise get the nodes that are known in the broadcast service from the list
        List<Node> queryList = new ArrayList<>();
        if (nodes.contains("all")) {
            // query all known nodes, get list from broadcastservice
            queryList.addAll(broadcastService.getSortedNodes());
        } else {
            // only get nodes from list
            for (String node : nodes) {
                Node n = broadcastService.getNode(node);
                if (n != null) queryList.add(n);
            }
        }
        return queryList;
    }

    @Override
    public void distributedStartProgram(DistributedMessages.DistributedNameRequest request, StreamObserver<DistributedMessages.DistributedResultStatus> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                ExecutorCompletionService<DistributedMessages.ResultStatus> completionService = new ExecutorCompletionService<>(executorService);
                Map<String, DistributedMessages.ResultStatus> results = new HashMap<>();
                List<Node> queryList = getQueryList(request.getNodesList());

                // query nodes in parallel
                List<Future<DistributedMessages.ResultStatus>> ful = new ArrayList<>();
                for (Node node : queryList) {
                    if (!node.getName().equalsIgnoreCase(broadcastService.getSelf().getName())) { // don't query self
                        Future<DistributedMessages.ResultStatus> fu = completionService.submit(new Callable<DistributedMessages.ResultStatus>() {
                            @Override
                            public DistributedMessages.ResultStatus call() throws Exception {
                                ManagedChannel channel = channelService.getChannel(node.getAddress());
                                XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
                                Messages.Result result = stub.startProcess(Messages.NameRequest.newBuilder().setName(request.getName()).setToken(request.getToken()).setWait(request.getWait()).build());
                                return DistributedMessages.ResultStatus.newBuilder().setName(node.getName()).setSuccess(result.getSuccess()).setCode(result.getCode()).setMessage(result.getMessage()).build();
                            }
                        });
                        ful.add(fu);
                    }
                }

                // collect results
                for (int i = 0; i < ful.size(); i++) {
                    try {
                        Future<DistributedMessages.ResultStatus> f = completionService.poll(TIME_OUT, TimeUnit.SECONDS);
                        if (f != null) {
                            DistributedMessages.ResultStatus r = f.get();
                            results.put(r.getName(), r);
                        }
                    } catch (InterruptedException e) {
                        log.debug("completionservice timed out", e);
                    } catch (ExecutionException e) {
                        log.error("Unable to get result from completionservice", e);
                    }
                }

                // clean up
                for (Future<DistributedMessages.ResultStatus> future : ful) {
                    future.cancel(true);
                }

                // run on self
                String self = broadcastService.getSelf().getName();
                if (request.getNodesList().contains(self) || request.getNodesList().contains("all")) {
                    try {
                        xmlRpcService.startProcess(request.getName(), request.getWait());
                        results.put(self, DistributedMessages.ResultStatus.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK).build());
                    } catch (XmlRpcException e) {
                        results.put(self, DistributedMessages.ResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
                    }
                }

                // build response
                responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder()
                    .setSuccess(true)
                    .setCode(Messages.ResultCode.OK)
                    .putAllResults(results)
                    .build());

            } else {
                responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not Authenticated").build());
            }
        }
//        catch (IOException e) {
//            responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
//        }
        catch (ValidationException e) {
            responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void distributedStopProgram(DistributedMessages.DistributedNameRequest request, StreamObserver<DistributedMessages.DistributedResultStatus> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                ExecutorCompletionService<DistributedMessages.ResultStatus> completionService = new ExecutorCompletionService<>(executorService);
                Map<String, DistributedMessages.ResultStatus> results = new HashMap<>();
                List<Node> queryList = getQueryList(request.getNodesList());

                // query nodes in parallel
                List<Future<DistributedMessages.ResultStatus>> ful = new ArrayList<>();
                for (Node node : queryList) {
                    if (!node.getName().equalsIgnoreCase(broadcastService.getSelf().getName())) { // don't query self
                        Future<DistributedMessages.ResultStatus> fu = completionService.submit(new Callable<DistributedMessages.ResultStatus>() {
                            @Override
                            public DistributedMessages.ResultStatus call() throws Exception {
                                ManagedChannel channel = channelService.getChannel(node.getAddress());
                                XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
                                Messages.Result result = stub.stopProcess(Messages.NameRequest.newBuilder().setName(request.getName()).setToken(request.getToken()).setWait(request.getWait()).build());
                                return DistributedMessages.ResultStatus.newBuilder().setName(node.getName()).setSuccess(result.getSuccess()).setCode(result.getCode()).setMessage(result.getMessage()).build();
                            }
                        });
                        ful.add(fu);
                    }
                }

                // collect results
                for (int i = 0; i < ful.size(); i++) {
                    try {
                        Future<DistributedMessages.ResultStatus> f = completionService.poll(TIME_OUT, TimeUnit.SECONDS);
                        if (f != null) {
                            DistributedMessages.ResultStatus r = f.get();
                            results.put(r.getName(), r);
                        }
                    } catch (InterruptedException e) {
                        log.debug("completionservice timed out", e);
                    } catch (ExecutionException e) {
                        log.error("Unable to get result from completionservice", e);
                    }
                }

                // clean up
                for (Future<DistributedMessages.ResultStatus> future : ful) {
                    future.cancel(true);
                }

                // run on self
                String self = broadcastService.getSelf().getName();
                if (request.getNodesList().contains(self) || request.getNodesList().contains("all")) {
                    try {
                        xmlRpcService.stopProcess(request.getName(), request.getWait());
                        results.put(self, DistributedMessages.ResultStatus.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK).build());
                    } catch (XmlRpcException e) {
                        results.put(self, DistributedMessages.ResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
                    }
                }

                // build response
                responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder()
                    .setSuccess(true)
                    .setCode(Messages.ResultCode.OK)
                    .putAllResults(results)
                    .build());

            } else {
                responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not Authenticated").build());
            }
        } catch (ValidationException e) {
            responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void distributedStartProgramGroup(DistributedMessages.DistributedNameRequest request, StreamObserver<DistributedMessages.DistributedProcessStatusses> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                ExecutorCompletionService<Messages.ProcessStatusses> completionService = new ExecutorCompletionService<>(executorService);
                Map<String, Messages.ProcessStatusses> results = new HashMap<>();
                List<Node> queryList = getQueryList(request.getNodesList());

                // query nodes in parallel
                List<Future<Messages.ProcessStatusses>> ful = new ArrayList<>();
                for (Node node : queryList) {
                    if (!node.getName().equalsIgnoreCase(broadcastService.getSelf().getName())) { // don't query self
                        Future<Messages.ProcessStatusses> fu = completionService.submit(new Callable<Messages.ProcessStatusses>() {
                            @Override
                            public Messages.ProcessStatusses call() throws Exception {
                                ManagedChannel channel = channelService.getChannel(node.getAddress());
                                XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
                                Messages.ProcessStatusses result = stub.startProcessGroup(Messages.NameRequest.newBuilder().setName(request.getName()).setToken(request.getToken()).setWait(request.getWait()).build());
                                return Messages.ProcessStatusses.newBuilder().mergeFrom(result).setName(node.getName()).build();
                            }
                        });
                        ful.add(fu);
                    }
                }

                // collect results
                for (int i = 0; i < ful.size(); i++) {
                    try {
                        Future<Messages.ProcessStatusses> f = completionService.poll(TIME_OUT, TimeUnit.SECONDS);
                        if (f != null) {
                            Messages.ProcessStatusses r = f.get();
                            results.put(r.getName(), r);
                        }
                    } catch (InterruptedException e) {
                        log.debug("completionservice timed out", e);
                    } catch (ExecutionException e) {
                        log.error("Unable to get result from completionservice", e);
                    }
                }

                // clean up
                for (Future<Messages.ProcessStatusses> future : ful) {
                    future.cancel(true);
                }

                // run on self
                String self = broadcastService.getSelf().getName();
                if (request.getNodesList().contains(self) || request.getNodesList().contains("all")) {
                    try {
                        List<ProcessStatus> processStatuses = xmlRpcService.startProcessGroup(request.getName(), request.getWait());
                        List<Messages.ProcessStatus> list = new ArrayList<>();
                        for (ProcessStatus status : processStatuses) {
                            Messages.ProcessStatus processStatus = Messages.ProcessStatus.newBuilder()
                                .setName(status.getName())
                                .setGroup(status.getGroup())
                                .setDescription(status.getDescription())
                                .setStatus(status.getStatus())
                                .build();
                            list.add(processStatus);
                        }
                        results.put(self, Messages.ProcessStatusses.newBuilder()
                            .setSuccess(true)
                            .setCode(Messages.ResultCode.OK)
                            .addAllProcessStatus(list)
                            .build());
                    } catch (XmlRpcException e) {
                        results.put(self, Messages.ProcessStatusses.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
                    }
                }

                // build response
                responseObserver.onNext(DistributedMessages.DistributedProcessStatusses.newBuilder()
                    .setSuccess(true)
                    .setCode(Messages.ResultCode.OK)
                    .putAllResults(results)
                    .build());

            } else {
                responseObserver.onNext(DistributedMessages.DistributedProcessStatusses.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not Authenticated").build());
            }
        } catch (ValidationException e) {
            responseObserver.onNext(DistributedMessages.DistributedProcessStatusses.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void distributedStopProgramGroup(DistributedMessages.DistributedNameRequest request, StreamObserver<DistributedMessages.DistributedProcessStatusses> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                ExecutorCompletionService<Messages.ProcessStatusses> completionService = new ExecutorCompletionService<>(executorService);
                Map<String, Messages.ProcessStatusses> results = new HashMap<>();
                List<Node> queryList = getQueryList(request.getNodesList());

                // query nodes in parallel
                List<Future<Messages.ProcessStatusses>> ful = new ArrayList<>();
                for (Node node : queryList) {
                    if (!node.getName().equalsIgnoreCase(broadcastService.getSelf().getName())) { // don't query self
                        Future<Messages.ProcessStatusses> fu = completionService.submit(new Callable<Messages.ProcessStatusses>() {
                            @Override
                            public Messages.ProcessStatusses call() throws Exception {
                                ManagedChannel channel = channelService.getChannel(node.getAddress());
                                XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
                                Messages.ProcessStatusses result = stub.stopProcessGroup(Messages.NameRequest.newBuilder().setName(request.getName()).setToken(request.getToken()).setWait(request.getWait()).build());
                                return Messages.ProcessStatusses.newBuilder().mergeFrom(result).setName(node.getName()).build();
                            }
                        });
                        ful.add(fu);
                    }
                }

                // collect results
                for (int i = 0; i < ful.size(); i++) {
                    try {
                        Future<Messages.ProcessStatusses> f = completionService.poll(TIME_OUT, TimeUnit.SECONDS);
                        if (f != null) {
                            Messages.ProcessStatusses r = f.get();
                            results.put(r.getName(), r);
                        }
                    } catch (InterruptedException e) {
                        log.debug("completionservice timed out", e);
                    } catch (ExecutionException e) {
                        log.error("Unable to get result from completionservice", e);
                    }
                }

                // clean up
                for (Future<Messages.ProcessStatusses> future : ful) {
                    future.cancel(true);
                }

                // run on self
                String self = broadcastService.getSelf().getName();
                if (request.getNodesList().contains(self) || request.getNodesList().contains("all")) {
                    try {
                        List<ProcessStatus> processStatuses = xmlRpcService.stopProcessGroup(request.getName(), request.getWait());
                        List<Messages.ProcessStatus> list = new ArrayList<>();
                        for (ProcessStatus status : processStatuses) {
                            Messages.ProcessStatus processStatus = Messages.ProcessStatus.newBuilder()
                                .setName(status.getName())
                                .setGroup(status.getGroup())
                                .setDescription(status.getDescription())
                                .setStatus(status.getStatus())
                                .build();
                            list.add(processStatus);
                        }
                        results.put(self, Messages.ProcessStatusses.newBuilder()
                            .setSuccess(true)
                            .setCode(Messages.ResultCode.OK)
                            .addAllProcessStatus(list)
                            .build());
                    } catch (XmlRpcException e) {
                        results.put(self, Messages.ProcessStatusses.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
                    }
                }

                // build response
                responseObserver.onNext(DistributedMessages.DistributedProcessStatusses.newBuilder()
                    .setSuccess(true)
                    .setCode(Messages.ResultCode.OK)
                    .putAllResults(results)
                    .build());

            } else {
                responseObserver.onNext(DistributedMessages.DistributedProcessStatusses.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not Authenticated").build());
            }
        } catch (ValidationException e) {
            responseObserver.onNext(DistributedMessages.DistributedProcessStatusses.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void distributedGetProgramInfo(DistributedMessages.DistributedNameRequest request, StreamObserver<DistributedMessages.DistributedProcessInfo> responseObserver) {
        try {
            ExecutorCompletionService<Messages.ProcessInfo> completionService = new ExecutorCompletionService<>(executorService);
            Map<String, Messages.ProcessInfo> results = new HashMap<>();
            List<Node> queryList = getQueryList(request.getNodesList());

            // query nodes in parallel
            List<Future<Messages.ProcessInfo>> ful = new ArrayList<>();
            for (Node node : queryList) {
                if (!node.getName().equalsIgnoreCase(broadcastService.getSelf().getName())) { // don't query self
                    Future<Messages.ProcessInfo> fu = completionService.submit(new Callable<Messages.ProcessInfo>() {
                        @Override
                        public Messages.ProcessInfo call() throws Exception {
                            ManagedChannel channel = channelService.getChannel(node.getAddress());
                            XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
                            Messages.ProcessInfo result = stub.getProcessInfo(Messages.NameRequest.newBuilder().setName(request.getName()).build());
                            return Messages.ProcessInfo.newBuilder().mergeFrom(result).setNodeName(node.getName()).build();
                        }
                    });
                    ful.add(fu);
                }
            }

            // collect results
            for (int i = 0; i < ful.size(); i++) {
                try {
                    Future<Messages.ProcessInfo> f = completionService.poll(TIME_OUT, TimeUnit.SECONDS);
                    if (f != null) {
                        Messages.ProcessInfo r = f.get();
                        results.put(r.getNodeName(), r);
                    }
                } catch (InterruptedException e) {
                    log.debug("completionservice timed out", e);
                } catch (ExecutionException e) {
                    log.error("Unable to get result from completionservice", e);
                }
            }

            // clean up
            for (Future<Messages.ProcessInfo> future : ful) {
                future.cancel(true);
            }

            // run on self
            String self = broadcastService.getSelf().getName();
            if (request.getNodesList().contains(self) || request.getNodesList().contains("all")) {
                try {
                    ProcessInfo info = xmlRpcService.getProcessInfo(request.getName());
                    results.put(self, Messages.ProcessInfo.newBuilder().setSuccess(true).setResultCode(Messages.ResultCode.OK)
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
                    results.put(self, Messages.ProcessInfo.newBuilder().setSuccess(false).setResultCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
                }
            }

            // build response
            responseObserver.onNext(DistributedMessages.DistributedProcessInfo.newBuilder()
                .setSuccess(true)
                .setCode(Messages.ResultCode.OK)
                .putAllResults(results)
                .build());

        } catch (ValidationException e) {
            responseObserver.onNext(DistributedMessages.DistributedProcessInfo.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void distributedUploadProgram(DistributedMessages.DistributedProgram request, StreamObserver<DistributedMessages.DistributedResultStatus> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                ExecutorCompletionService<DistributedMessages.ResultStatus> completionService = new ExecutorCompletionService<>(executorService);
                Map<String, DistributedMessages.ResultStatus> results = new HashMap<>();
                List<Node> queryList = getQueryList(request.getNodesList());

                // query nodes in parallel
                List<Future<DistributedMessages.ResultStatus>> ful = new ArrayList<>();
                for (Node node : queryList) {
                    if (!node.getName().equalsIgnoreCase(broadcastService.getSelf().getName())) { // don't query self
                        Future<DistributedMessages.ResultStatus> fu = completionService.submit(new Callable<DistributedMessages.ResultStatus>() {
                            @Override
                            public DistributedMessages.ResultStatus call() throws Exception {
                                final CountDownLatch finishLatch = new CountDownLatch(1);
                                final AsyncSuccess success = new AsyncSuccess();
                                ManagedChannel channel = channelService.getChannel(node.getAddress());
                                FileRpcGrpc.FileRpcBlockingStub stub = FileRpcGrpc.newBlockingStub(channel);
                                FileRpcGrpc.FileRpcStub async = FileRpcGrpc.newStub(channel);

                                // upload files

                                StreamObserver<Messages.Result> responseObserver = new StreamObserver<Messages.Result>() {
                                    @Override
                                    public void onNext(Messages.Result result) {
                                        success.setSuccess(result.getSuccess());
                                        if (!result.getSuccess()) {
                                            success.setMessage(result.getMessage());
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable t) {
                                        Status status = Status.fromThrowable(t);
                                        log.error("Failed to upload files", status.getDescription());
                                        success.setSuccess(false);
                                        success.setMessage(status.getDescription());
                                        finishLatch.countDown();
                                    }

                                    @Override
                                    public void onCompleted() {
                                        log.debug("Finished file upload");
                                        success.setSuccess(true);
                                        finishLatch.countDown();
                                    }
                                };

                                StreamObserver<Messages.Chunk> requestObserver = async.uploadFiles(responseObserver);
                                try {
                                    List<FileUuidBytes> fileUuidBytes = fileService.generateFileUuidBytesList(request);
                                    for (FileUuidBytes file : fileUuidBytes) {
                                        RandomAccessFile aFile = new RandomAccessFile(file.getFile().toFile(), "r");
                                        FileChannel inChannel = aFile.getChannel();
                                        ByteBuffer buffer = ByteBuffer.allocate(500000);
                                        while (inChannel.read(buffer) > 0) {
                                            buffer.flip();
                                            ByteString bytes = ByteString.copyFrom(buffer);
                                            requestObserver.onNext(Messages.Chunk.newBuilder()
                                                .setToken(request.getToken())
                                                .setProgramUuid(file.getProgramUuid())
                                                .setFileUuid(file.getFileUuid())
                                                .setData(bytes)
                                                .build()
                                            );
                                            buffer.clear();
                                        }
                                        inChannel.close();
                                        aFile.close();
                                    }
                                } catch (RuntimeException e) {
                                    requestObserver.onError(e);
                                    throw e;
                                }
                                requestObserver.onCompleted();

                                try {
                                    finishLatch.await(2, TimeUnit.MINUTES);
                                } catch (InterruptedException ignore) {
                                    System.out.println("what2 " + ignore.getMessage());
                                }

                                if (!success.isSuccess()) {
                                    return DistributedMessages.ResultStatus.newBuilder().setName(node.getName()).setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(success.getMessage()).build();
                                }

                                // upload program

                                Messages.Program.Builder builder = Messages.Program.newBuilder()
                                    .setToken(request.getToken())
                                    .setName(request.getName())
                                    .setProgramUuid(request.getProgramUuid());
                                if (request.getCommand() != null) builder.setCommand(request.getCommand());
                                builder.setPriority(request.getPriority());
                                builder.setAutostart(request.getAutostart());
                                if (request.getAutorestart() != null) builder.setAutorestart(request.getAutorestart());
                                builder.setStartsecs(request.getStartsecs());
                                builder.setStartretries(request.getStartretries());
                                if (request.getExitcodes() != null) builder.setExitcodes(request.getExitcodes());
                                builder.setStopwaitsecs(request.getStopwaitsecs());
                                if (request.getEnvironment() != null) builder.setEnvironment(request.getEnvironment());
                                if (request.getPathsMap() != null) builder.putAllPaths(request.getPathsMap());
                                if (request.getTransformsMap() != null) builder.putAllTransforms(request.getTransformsMap());
                                if (request.getUuidFilenamesMap() != null) builder.putAllUuidFilenames(request.getUuidFilenamesMap());
                                if (request.getUuidPathsMap() != null) builder.putAllUuidPaths(request.getUuidPathsMap());
                                Messages.Result result = stub.uploadProgram(builder.build());
                                return DistributedMessages.ResultStatus.newBuilder().setName(node.getName()).setSuccess(result.getSuccess()).setCode(result.getCode()).setMessage(result.getMessage()).build();
                            }
                        });
                        ful.add(fu);
                    }
                }

                // collect results
                for (int i = 0; i < ful.size(); i++) {
                    try {
                        Future<DistributedMessages.ResultStatus> f = completionService.poll(TIME_OUT, TimeUnit.SECONDS);
                        if (f != null) {
                            DistributedMessages.ResultStatus r = f.get();
                            results.put(r.getName(), r);
                        }
                    } catch (InterruptedException e) {
                        log.debug("completionservice timed out", e);
                    } catch (ExecutionException e) {
                        log.error("Unable to get result from completionservice", e);
                    }
                }

                // clean up
                for (Future<DistributedMessages.ResultStatus> future : ful) {
                    future.cancel(true);
                }

                // run on self
                String self = broadcastService.getSelf().getName();
                if (request.getNodesList().contains(self) || request.getNodesList().contains("all")) {
                    try {
                        Messages.Program.Builder builder = Messages.Program.newBuilder()
                            .setToken(request.getToken())
                            .setName(request.getName())
                            .setProgramUuid(request.getProgramUuid());
                        if (request.getCommand() != null) builder.setCommand(request.getCommand());
                        builder.setPriority(request.getPriority());
                        builder.setAutostart(request.getAutostart());
                        if (request.getAutorestart() != null) builder.setAutorestart(request.getAutorestart());
                        builder.setStartsecs(request.getStartsecs());
                        builder.setStartretries(request.getStartretries());
                        if (request.getExitcodes() != null) builder.setExitcodes(request.getExitcodes());
                        builder.setStopwaitsecs(request.getStopwaitsecs());
                        if (request.getEnvironment() != null) builder.setEnvironment(request.getEnvironment());
                        if (request.getPathsMap() != null) builder.putAllPaths(request.getPathsMap());
                        if (request.getTransformsMap() != null) builder.putAllTransforms(request.getTransformsMap());
                        if (request.getUuidFilenamesMap() != null) builder.putAllUuidFilenames(request.getUuidFilenamesMap());
                        if (request.getUuidPathsMap() != null) builder.putAllUuidPaths(request.getUuidPathsMap());

                        Messages.Program program = builder.build();
                        fileService.storeConfiguration(program);
                        fileService.storeProgram(program);
                        results.put(self, DistributedMessages.ResultStatus.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK).build());
                    } catch (IOException e) {
                        results.put(self, DistributedMessages.ResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
                    }
                }

                // build response
                responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder()
                    .setSuccess(true)
                    .setCode(Messages.ResultCode.OK)
                    .putAllResults(results)
                    .build());

            } else {
                responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not Authenticated").build());
            }
        } catch (ValidationException e) {
            responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void distributedDeleteProgram(DistributedMessages.DistributedNameRequest request, StreamObserver<DistributedMessages.DistributedResultStatus> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                ExecutorCompletionService<DistributedMessages.ResultStatus> completionService = new ExecutorCompletionService<>(executorService);
                Map<String, DistributedMessages.ResultStatus> results = new HashMap<>();
                List<Node> queryList = getQueryList(request.getNodesList());

                // query nodes in parallel
                List<Future<DistributedMessages.ResultStatus>> ful = new ArrayList<>();
                for (Node node : queryList) {
                    if (!node.getName().equalsIgnoreCase(broadcastService.getSelf().getName())) { // don't query self
                        Future<DistributedMessages.ResultStatus> fu = completionService.submit(new Callable<DistributedMessages.ResultStatus>() {
                            @Override
                            public DistributedMessages.ResultStatus call() throws Exception {
                                ManagedChannel channel = channelService.getChannel(node.getAddress());
                                FileRpcGrpc.FileRpcBlockingStub stub = FileRpcGrpc.newBlockingStub(channel);
                                Messages.Result result = stub.deleteProgram(Messages.NameRequest.newBuilder().setName(request.getName()).setToken(request.getToken()).build());
                                return DistributedMessages.ResultStatus.newBuilder().setName(node.getName()).setSuccess(result.getSuccess()).setCode(result.getCode()).setMessage(result.getMessage()).build();
                            }
                        });
                        ful.add(fu);
                    }
                }

                // collect results
                for (int i = 0; i < ful.size(); i++) {
                    try {
                        Future<DistributedMessages.ResultStatus> f = completionService.poll(TIME_OUT, TimeUnit.SECONDS);
                        if (f != null) {
                            DistributedMessages.ResultStatus r = f.get();
                            results.put(r.getName(), r);
                        }
                    } catch (InterruptedException e) {
                        log.debug("completionservice timed out", e);
                    } catch (ExecutionException e) {
                        log.error("Unable to get result from completionservice", e);
                    }
                }

                // clean up
                for (Future<DistributedMessages.ResultStatus> future : ful) {
                    future.cancel(true);
                }

                // run on self
                String self = broadcastService.getSelf().getName();
                if (request.getNodesList().contains(self) || request.getNodesList().contains("all")) {
                    try {
                        fileService.deleteProgram(request.getName());
                        results.put(self, DistributedMessages.ResultStatus.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK).build());
                    } catch (IOException e) {
                        results.put(self, DistributedMessages.ResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
                    }
                }

                // build response
                responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder()
                    .setSuccess(true)
                    .setCode(Messages.ResultCode.OK)
                    .putAllResults(results)
                    .build());

            } else {
                responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not Authenticated").build());
            }
        } catch (ValidationException e) {
            responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void distributedSignalProgram(DistributedMessages.DistributedNameDataRequest request, StreamObserver<DistributedMessages.DistributedResultStatus> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                ExecutorCompletionService<DistributedMessages.ResultStatus> completionService = new ExecutorCompletionService<>(executorService);
                Map<String, DistributedMessages.ResultStatus> results = new HashMap<>();
                List<Node> queryList = getQueryList(request.getNodesList());

                // query nodes in parallel
                List<Future<DistributedMessages.ResultStatus>> ful = new ArrayList<>();
                for (Node node : queryList) {
                    if (!node.getName().equalsIgnoreCase(broadcastService.getSelf().getName())) { // don't query self
                        Future<DistributedMessages.ResultStatus> fu = completionService.submit(new Callable<DistributedMessages.ResultStatus>() {
                            @Override
                            public DistributedMessages.ResultStatus call() throws Exception {
                                ManagedChannel channel = channelService.getChannel(node.getAddress());
                                XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
                                Messages.Result result = stub.signalProcess(Messages.NameDataRequest.newBuilder().setName(request.getName()).setToken(request.getToken()).setData(request.getData()).build());
                                return DistributedMessages.ResultStatus.newBuilder().setName(node.getName()).setSuccess(result.getSuccess()).setCode(result.getCode()).setMessage(result.getMessage()).build();
                            }
                        });
                        ful.add(fu);
                    }
                }

                // collect results
                for (int i = 0; i < ful.size(); i++) {
                    try {
                        Future<DistributedMessages.ResultStatus> f = completionService.poll(TIME_OUT, TimeUnit.SECONDS);
                        if (f != null) {
                            DistributedMessages.ResultStatus r = f.get();
                            results.put(r.getName(), r);
                        }
                    } catch (InterruptedException e) {
                        log.debug("completionservice timed out", e);
                    } catch (ExecutionException e) {
                        log.error("Unable to get result from completionservice", e);
                    }
                }

                // clean up
                for (Future<DistributedMessages.ResultStatus> future : ful) {
                    future.cancel(true);
                }

                // run on self
                String self = broadcastService.getSelf().getName();
                if (request.getNodesList().contains(self) || request.getNodesList().contains("all")) {
                    try {
                        xmlRpcService.signalProcess(request.getName(), request.getData());
                        results.put(self, DistributedMessages.ResultStatus.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK).build());
                    } catch (XmlRpcException e) {
                        results.put(self, DistributedMessages.ResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
                    }
                }

                // build response
                responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder()
                    .setSuccess(true)
                    .setCode(Messages.ResultCode.OK)
                    .putAllResults(results)
                    .build());

            } else {
                responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not Authenticated").build());
            }
        } catch (ValidationException e) {
            responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void distributedSendMessage(DistributedMessages.DistributedNameDataRequest request, StreamObserver<DistributedMessages.DistributedResultStatus> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                ExecutorCompletionService<DistributedMessages.ResultStatus> completionService = new ExecutorCompletionService<>(executorService);
                Map<String, DistributedMessages.ResultStatus> results = new HashMap<>();
                List<Node> queryList = getQueryList(request.getNodesList());

                // query nodes in parallel
                List<Future<DistributedMessages.ResultStatus>> ful = new ArrayList<>();
                for (Node node : queryList) {
                    if (!node.getName().equalsIgnoreCase(broadcastService.getSelf().getName())) { // don't query self
                        Future<DistributedMessages.ResultStatus> fu = completionService.submit(new Callable<DistributedMessages.ResultStatus>() {
                            @Override
                            public DistributedMessages.ResultStatus call() throws Exception {
                                ManagedChannel channel = channelService.getChannel(node.getAddress());
                                XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
                                Messages.Result result = stub.sendProcessStdin(Messages.NameDataRequest.newBuilder().setName(request.getName()).setToken(request.getToken()).setData(request.getData()).build());
                                return DistributedMessages.ResultStatus.newBuilder().setName(node.getName()).setSuccess(result.getSuccess()).setCode(result.getCode()).setMessage(result.getMessage()).build();
                            }
                        });
                        ful.add(fu);
                    }
                }

                // collect results
                for (int i = 0; i < ful.size(); i++) {
                    try {
                        Future<DistributedMessages.ResultStatus> f = completionService.poll(TIME_OUT, TimeUnit.SECONDS);
                        if (f != null) {
                            DistributedMessages.ResultStatus r = f.get();
                            results.put(r.getName(), r);
                        }
                    } catch (InterruptedException e) {
                        log.debug("completionservice timed out", e);
                    } catch (ExecutionException e) {
                        log.error("Unable to get result from completionservice", e);
                    }
                }

                // clean up
                for (Future<DistributedMessages.ResultStatus> future : ful) {
                    future.cancel(true);
                }

                // run on self
                String self = broadcastService.getSelf().getName();
                if (request.getNodesList().contains(self) || request.getNodesList().contains("all")) {
                    try {
                        xmlRpcService.sendProcessStdin(request.getName(), request.getData());
                        results.put(self, DistributedMessages.ResultStatus.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK).build());
                    } catch (XmlRpcException e) {
                        results.put(self, DistributedMessages.ResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
                    }
                }

                // build response
                responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder()
                    .setSuccess(true)
                    .setCode(Messages.ResultCode.OK)
                    .putAllResults(results)
                    .build());

            } else {
                responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not Authenticated").build());
            }
        } catch (ValidationException e) {
            responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void distributedSendCommEvent(DistributedMessages.DistributedNameDataRequest request, StreamObserver<DistributedMessages.DistributedResultStatus> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                ExecutorCompletionService<DistributedMessages.ResultStatus> completionService = new ExecutorCompletionService<>(executorService);
                Map<String, DistributedMessages.ResultStatus> results = new HashMap<>();
                List<Node> queryList = getQueryList(request.getNodesList());

                // query nodes in parallel
                List<Future<DistributedMessages.ResultStatus>> ful = new ArrayList<>();
                for (Node node : queryList) {
                    if (!node.getName().equalsIgnoreCase(broadcastService.getSelf().getName())) { // don't query self
                        Future<DistributedMessages.ResultStatus> fu = completionService.submit(new Callable<DistributedMessages.ResultStatus>() {
                            @Override
                            public DistributedMessages.ResultStatus call() throws Exception {
                                ManagedChannel channel = channelService.getChannel(node.getAddress());
                                XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
                                Messages.Result result = stub.sendRemoteCommEvent(Messages.NameDataRequest.newBuilder().setName(request.getName()).setToken(request.getToken()).setData(request.getData()).setType(request.getType()).build());
                                return DistributedMessages.ResultStatus.newBuilder().setName(node.getName()).setSuccess(result.getSuccess()).setCode(result.getCode()).setMessage(result.getMessage()).build();
                            }
                        });
                        ful.add(fu);
                    }
                }

                // collect results
                for (int i = 0; i < ful.size(); i++) {
                    try {
                        Future<DistributedMessages.ResultStatus> f = completionService.poll(TIME_OUT, TimeUnit.SECONDS);
                        if (f != null) {
                            DistributedMessages.ResultStatus r = f.get();
                            results.put(r.getName(), r);
                        }
                    } catch (InterruptedException e) {
                        log.debug("completionservice timed out", e);
                    } catch (ExecutionException e) {
                        log.error("Unable to get result from completionservice", e);
                    }
                }

                // clean up
                for (Future<DistributedMessages.ResultStatus> future : ful) {
                    future.cancel(true);
                }

                // run on self
                String self = broadcastService.getSelf().getName();
                if (request.getNodesList().contains(self) || request.getNodesList().contains("all")) {
                    try {
                        xmlRpcService.sendRemoteCommEvent(request.getType(), request.getData());
                        results.put(self, DistributedMessages.ResultStatus.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK).build());
                    } catch (XmlRpcException e) {
                        results.put(self, DistributedMessages.ResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
                    }
                }

                // build response
                responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder()
                    .setSuccess(true)
                    .setCode(Messages.ResultCode.OK)
                    .putAllResults(results)
                    .build());

            } else {
                responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not Authenticated").build());
            }
        } catch (ValidationException e) {
            responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void distributedRestartSupervisor(DistributedMessages.DistributedToken request, StreamObserver<DistributedMessages.DistributedResultStatus> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                ExecutorCompletionService<DistributedMessages.ResultStatus> completionService = new ExecutorCompletionService<>(executorService);
                Map<String, DistributedMessages.ResultStatus> results = new HashMap<>();
                List<Node> queryList = getQueryList(request.getNodesList());

                // query nodes in parallel
                List<Future<DistributedMessages.ResultStatus>> ful = new ArrayList<>();
                for (Node node : queryList) {
                    if (!node.getName().equalsIgnoreCase(broadcastService.getSelf().getName())) { // don't query self
                        Future<DistributedMessages.ResultStatus> fu = completionService.submit(new Callable<DistributedMessages.ResultStatus>() {
                            @Override
                            public DistributedMessages.ResultStatus call() throws Exception {
                                ManagedChannel channel = channelService.getChannel(node.getAddress());
                                XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
                                Messages.Result result = stub.restart(Messages.Token.newBuilder().setToken(request.getToken()).build());
                                return DistributedMessages.ResultStatus.newBuilder().setName(node.getName()).setSuccess(result.getSuccess()).setCode(result.getCode()).setMessage(result.getMessage()).build();
                            }
                        });
                        ful.add(fu);
                    }
                }

                // collect results
                for (int i = 0; i < ful.size(); i++) {
                    try {
                        Future<DistributedMessages.ResultStatus> f = completionService.poll(TIME_OUT, TimeUnit.SECONDS);
                        if (f != null) {
                            DistributedMessages.ResultStatus r = f.get();
                            results.put(r.getName(), r);
                        }
                    } catch (InterruptedException e) {
                        log.debug("completionservice timed out", e);
                    } catch (ExecutionException e) {
                        log.error("Unable to get result from completionservice", e);
                    }
                }

                // clean up
                for (Future<DistributedMessages.ResultStatus> future : ful) {
                    future.cancel(true);
                }

                // run on self
                String self = broadcastService.getSelf().getName();
                if (request.getNodesList().contains(self) || request.getNodesList().contains("all")) {
                    try {
                        xmlRpcService.restart();
                        results.put(self, DistributedMessages.ResultStatus.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK).build());
                    } catch (XmlRpcException e) {
                        results.put(self, DistributedMessages.ResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
                    }
                }

                // build response
                responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder()
                    .setSuccess(true)
                    .setCode(Messages.ResultCode.OK)
                    .putAllResults(results)
                    .build());

            } else {
                responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not Authenticated").build());
            }
        } catch (ValidationException e) {
            responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void distributedClearProcessLogs(DistributedMessages.DistributedNameRequest request, StreamObserver<DistributedMessages.DistributedResultStatus> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                ExecutorCompletionService<DistributedMessages.ResultStatus> completionService = new ExecutorCompletionService<>(executorService);
                Map<String, DistributedMessages.ResultStatus> results = new HashMap<>();
                List<Node> queryList = getQueryList(request.getNodesList());

                // query nodes in parallel
                List<Future<DistributedMessages.ResultStatus>> ful = new ArrayList<>();
                for (Node node : queryList) {
                    if (!node.getName().equalsIgnoreCase(broadcastService.getSelf().getName())) { // don't query self
                        Future<DistributedMessages.ResultStatus> fu = completionService.submit(new Callable<DistributedMessages.ResultStatus>() {
                            @Override
                            public DistributedMessages.ResultStatus call() throws Exception {
                                ManagedChannel channel = channelService.getChannel(node.getAddress());
                                XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
                                Messages.Result result = stub.clearProcesslogs(Messages.NameRequest.newBuilder().setName(request.getName()).setToken(request.getToken()).build());
                                return DistributedMessages.ResultStatus.newBuilder().setName(node.getName()).setSuccess(result.getSuccess()).setCode(result.getCode()).setMessage(result.getMessage()).build();
                            }
                        });
                        ful.add(fu);
                    }
                }

                // collect results
                for (int i = 0; i < ful.size(); i++) {
                    try {
                        Future<DistributedMessages.ResultStatus> f = completionService.poll(TIME_OUT, TimeUnit.SECONDS);
                        if (f != null) {
                            DistributedMessages.ResultStatus r = f.get();
                            results.put(r.getName(), r);
                        }
                    } catch (InterruptedException e) {
                        log.debug("completionservice timed out", e);
                    } catch (ExecutionException e) {
                        log.error("Unable to get result from completionservice", e);
                    }
                }

                // clean up
                for (Future<DistributedMessages.ResultStatus> future : ful) {
                    future.cancel(true);
                }

                // run on self
                String self = broadcastService.getSelf().getName();
                if (request.getNodesList().contains(self) || request.getNodesList().contains("all")) {
                    try {
                        xmlRpcService.clearProcessLogs(request.getName());
                        results.put(self, DistributedMessages.ResultStatus.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK).build());
                    } catch (XmlRpcException e) {
                        results.put(self, DistributedMessages.ResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
                    }
                }

                // build response
                responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder()
                    .setSuccess(true)
                    .setCode(Messages.ResultCode.OK)
                    .putAllResults(results)
                    .build());

            } else {
                responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not Authenticated").build());
            }
        } catch (ValidationException e) {
            responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void distributedUpdate(DistributedMessages.DistributedToken request, StreamObserver<DistributedMessages.DistributedResultStatus> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                ExecutorCompletionService<DistributedMessages.ResultStatus> completionService = new ExecutorCompletionService<>(executorService);
                Map<String, DistributedMessages.ResultStatus> results = new HashMap<>();
                List<Node> queryList = getQueryList(request.getNodesList());

                // query nodes in parallel
                List<Future<DistributedMessages.ResultStatus>> ful = new ArrayList<>();
                for (Node node : queryList) {
                    if (!node.getName().equalsIgnoreCase(broadcastService.getSelf().getName())) { // don't query self
                        Future<DistributedMessages.ResultStatus> fu = completionService.submit(new Callable<DistributedMessages.ResultStatus>() {
                            @Override
                            public DistributedMessages.ResultStatus call() throws Exception {
                                ManagedChannel channel = channelService.getChannel(node.getAddress());
                                XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
                                Messages.Result result = stub.update(Messages.Token.newBuilder().setToken(request.getToken()).build());
                                return DistributedMessages.ResultStatus.newBuilder().setName(node.getName()).setSuccess(result.getSuccess()).setCode(result.getCode()).setMessage(result.getMessage()).build();
                            }
                        });
                        ful.add(fu);
                    }
                }

                // collect results
                for (int i = 0; i < ful.size(); i++) {
                    try {
                        Future<DistributedMessages.ResultStatus> f = completionService.poll(TIME_OUT, TimeUnit.SECONDS);
                        if (f != null) {
                            DistributedMessages.ResultStatus r = f.get();
                            results.put(r.getName(), r);
                        }
                    } catch (InterruptedException e) {
                        log.debug("completionservice timed out", e);
                    } catch (ExecutionException e) {
                        log.error("Unable to get result from completionservice", e);
                    }
                }

                // clean up
                for (Future<DistributedMessages.ResultStatus> future : ful) {
                    future.cancel(true);
                }

                // run on self
                String self = broadcastService.getSelf().getName();
                if (request.getNodesList().contains(self) || request.getNodesList().contains("all")) {
                    try {
                        xmlRpcService.update();
                        results.put(self, DistributedMessages.ResultStatus.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK).build());
                    } catch (XmlRpcException e) {
                        results.put(self, DistributedMessages.ResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
                    }
                }

                // build response
                responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder()
                    .setSuccess(true)
                    .setCode(Messages.ResultCode.OK)
                    .putAllResults(results)
                    .build());

            } else {
                responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not Authenticated").build());
            }
        } catch (ValidationException e) {
            responseObserver.onNext(DistributedMessages.DistributedResultStatus.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void distributedTailLog(DistributedMessages.DistributedLogRequest request, StreamObserver<DistributedMessages.DistributedTailLogResult> responseObserver) {
        try {
            ExecutorCompletionService<Messages.TailLogResult> completionService = new ExecutorCompletionService<>(executorService);
            Map<String, Messages.TailLogResult> results = new HashMap<>();
            List<Node> queryList = getQueryList(request.getNodesList());

            // query nodes in parallel
            List<Future<Messages.TailLogResult>> ful = new ArrayList<>();
            for (Node node : queryList) {
                if (!node.getName().equalsIgnoreCase(broadcastService.getSelf().getName())) { // don't query self
                    Future<Messages.TailLogResult> fu = completionService.submit(new Callable<Messages.TailLogResult>() {
                        @Override
                        public Messages.TailLogResult call() throws Exception {
                            ManagedChannel channel = channelService.getChannel(node.getAddress());
                            XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
                            Messages.TailLogResult result = stub.tailLog(Messages.LogRequest.newBuilder().setName(request.getName()).setOffset(request.getOffset()).setLength(request.getLength()).build());
                            return Messages.TailLogResult.newBuilder().mergeFrom(result).setName(node.getName()).build();
                        }
                    });
                    ful.add(fu);
                }
            }

            // collect results
            for (int i = 0; i < ful.size(); i++) {
                try {
                    Future<Messages.TailLogResult> f = completionService.poll(TIME_OUT, TimeUnit.SECONDS);
                    if (f != null) {
                        Messages.TailLogResult r = f.get();
                        results.put(r.getName(), r);
                    }
                } catch (InterruptedException e) {
                    log.debug("completionservice timed out", e);
                } catch (ExecutionException e) {
                    log.error("Unable to get result from completionservice", e);
                }
            }

            // clean up
            for (Future<Messages.TailLogResult> future : ful) {
                future.cancel(true);
            }

            // run on self
            String self = broadcastService.getSelf().getName();
            if (request.getNodesList().contains(self) || request.getNodesList().contains("all")) {
                try {
                    TailLog tailLog = xmlRpcService.tailProcessStdoutLog(request.getName(), request.getOffset(), request.getLength());
                    results.put(self, Messages.TailLogResult.newBuilder()
                        .setSuccess(true)
                        .setCode(Messages.ResultCode.OK)
                        .setLog(tailLog.getLog())
                        .setOffset(tailLog.getOffset())
                        .setOverflow(tailLog.isOverflow())
                        .build());
                } catch (XmlRpcException e) {
                    results.put(self, Messages.TailLogResult.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
                }
            }

            // build response
            responseObserver.onNext(DistributedMessages.DistributedTailLogResult.newBuilder()
                .setSuccess(true)
                .setCode(Messages.ResultCode.OK)
                .putAllResults(results)
                .build());

        } catch (ValidationException e) {
            responseObserver.onNext(DistributedMessages.DistributedTailLogResult.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void distributedTailErrorLog(DistributedMessages.DistributedLogRequest request, StreamObserver<DistributedMessages.DistributedTailLogResult> responseObserver) {
        try {
            ExecutorCompletionService<Messages.TailLogResult> completionService = new ExecutorCompletionService<>(executorService);
            Map<String, Messages.TailLogResult> results = new HashMap<>();
            List<Node> queryList = getQueryList(request.getNodesList());

            // query nodes in parallel
            List<Future<Messages.TailLogResult>> ful = new ArrayList<>();
            for (Node node : queryList) {
                if (!node.getName().equalsIgnoreCase(broadcastService.getSelf().getName())) { // don't query self
                    Future<Messages.TailLogResult> fu = completionService.submit(new Callable<Messages.TailLogResult>() {
                        @Override
                        public Messages.TailLogResult call() throws Exception {
                            ManagedChannel channel = channelService.getChannel(node.getAddress());
                            XmlRpcGrpc.XmlRpcBlockingStub stub = XmlRpcGrpc.newBlockingStub(channel);
                            Messages.TailLogResult result = stub.tailErrorLog(Messages.LogRequest.newBuilder().setName(request.getName()).setOffset(request.getOffset()).setLength(request.getLength()).build());
                            return Messages.TailLogResult.newBuilder().mergeFrom(result).setName(node.getName()).build();
                        }
                    });
                    ful.add(fu);
                }
            }

            // collect results
            for (int i = 0; i < ful.size(); i++) {
                try {
                    Future<Messages.TailLogResult> f = completionService.poll(TIME_OUT, TimeUnit.SECONDS);
                    if (f != null) {
                        Messages.TailLogResult r = f.get();
                        results.put(r.getName(), r);
                    }
                } catch (InterruptedException e) {
                    log.debug("completionservice timed out", e);
                } catch (ExecutionException e) {
                    log.error("Unable to get result from completionservice", e);
                }
            }

            // clean up
            for (Future<Messages.TailLogResult> future : ful) {
                future.cancel(true);
            }

            // run on self
            String self = broadcastService.getSelf().getName();
            if (request.getNodesList().contains(self) || request.getNodesList().contains("all")) {
                try {
                    TailLog tailLog = xmlRpcService.tailProcessStderrLog(request.getName(), request.getOffset(), request.getLength());
                    results.put(self, Messages.TailLogResult.newBuilder()
                        .setSuccess(true)
                        .setCode(Messages.ResultCode.OK)
                        .setLog(tailLog.getLog())
                        .setOffset(tailLog.getOffset())
                        .setOverflow(tailLog.isOverflow())
                        .build());
                } catch (XmlRpcException e) {
                    results.put(self, Messages.TailLogResult.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
                }
            }

            // build response
            responseObserver.onNext(DistributedMessages.DistributedTailLogResult.newBuilder()
                .setSuccess(true)
                .setCode(Messages.ResultCode.OK)
                .putAllResults(results)
                .build());

        } catch (ValidationException e) {
            responseObserver.onNext(DistributedMessages.DistributedTailLogResult.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }
}
