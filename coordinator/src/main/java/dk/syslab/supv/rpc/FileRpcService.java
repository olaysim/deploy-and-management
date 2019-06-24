package dk.syslab.supv.rpc;

import com.google.protobuf.ByteString;
import dk.syslab.controller.rpc.protobuf.FileRpcGrpc;
import dk.syslab.controller.rpc.protobuf.Messages;
import dk.syslab.supv.rpc.model.FileUuidBytes;
import dk.syslab.supv.web.api.model.FineUploaderProgram;
import dk.syslab.supv.web.api.model.Program;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("Duplicates")
@Service
public class FileRpcService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RpcChannelService channelService;

    private static class AsyncSuccess {
        public boolean success;
        public String message;
    }

    public boolean uploadFiles(String host, String token, List<FileUuidBytes> data) throws IOException {
        final CountDownLatch finishLatch = new CountDownLatch(1);
        final AsyncSuccess success = new AsyncSuccess();
        ManagedChannel channel = channelService.getChannel(host);
        FileRpcGrpc.FileRpcStub async = FileRpcGrpc.newStub(channel);

        StreamObserver<Messages.Result> responseObserver = new StreamObserver<Messages.Result>() {
            @Override
            public void onNext(Messages.Result result) {
                success.success = result.getSuccess();
                if (!result.getSuccess()) {
                    success.message = result.getMessage();
                }
            }

            @Override
            public void onError(Throwable t) {
                Status status = Status.fromThrowable(t);
                log.error("Failed to upload files", status.getDescription());
                success.success = false;
                success.message = status.getDescription();
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                log.debug("Finished file upload");
                success.success = true;
                finishLatch.countDown();
            }
        };

        StreamObserver<Messages.Chunk> requestObserver = async.uploadFiles(responseObserver);
        try {
            for (FileUuidBytes file : data) {
//                InputStream inputStream = Files.newInputStream(file.getFile());
//                ByteString bytes = ByteString.readFrom(inputStream);
//                inputStream.close();
                RandomAccessFile aFile = new RandomAccessFile(file.getFile().toFile(), "r");
                FileChannel inChannel = aFile.getChannel();
                ByteBuffer buffer = ByteBuffer.allocate(500000);
                while (inChannel.read(buffer) > 0) {
                    buffer.flip();
                    ByteString bytes = ByteString.copyFrom(buffer);
                    requestObserver.onNext(Messages.Chunk.newBuilder()
                        .setToken(token)
                        .setProgramUuid(file.getProgramUuid())
                        .setFileUuid(file.getFileUuid())
                        .setData(bytes)
                        .build()
                    );
                    buffer.clear();
                }
                inChannel.close();
                aFile.close();
//                inputStream.close();
            }
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        }
        requestObserver.onCompleted();

        try {
            finishLatch.await(2, TimeUnit.MINUTES);
        } catch (InterruptedException ignore) {
            log.error("what", ignore);
        }

        if (success.success) {
            return true;
        } else {
            throw new IOException(success.message);
        }
    }

    public boolean uploadProgram(String host, String token, FineUploaderProgram program) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        FileRpcGrpc.FileRpcBlockingStub stub = FileRpcGrpc.newBlockingStub(channel);
        Messages.Program.Builder builder = Messages.Program.newBuilder()
            .setToken(token)
            .setName(program.getName())
            .setProgramUuid(program.getTransaction());
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

        Messages.Result result = stub.uploadProgram(builder.build());
        if (result.getSuccess()) {
            return true;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public boolean deleteProgram(String host, String token, String name) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        FileRpcGrpc.FileRpcBlockingStub stub = FileRpcGrpc.newBlockingStub(channel);
        Messages.Result result = stub.deleteProgram(Messages.NameRequest.newBuilder().setName(name).setToken(token).build());
        if (result.getSuccess()) {
            return true;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public boolean addProgramToGroup(String host, String token, String group, String name, int prio) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        FileRpcGrpc.FileRpcBlockingStub stub = FileRpcGrpc.newBlockingStub(channel);
        Messages.Result result = stub.addProgramToGroup(Messages.GroupNameRequest.newBuilder()
            .setToken(token)
            .setGroup(group)
            .setName(name)
            .setPritority(prio)
            .build());
        if (result.getSuccess()) {
            return true;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public boolean removeProgramFromGroup(String host, String token, String group, String name) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        FileRpcGrpc.FileRpcBlockingStub stub = FileRpcGrpc.newBlockingStub(channel);
        Messages.Result result = stub.removeProgramFromGroup(Messages.GroupNameRequest.newBuilder()
            .setToken(token)
            .setGroup(group)
            .setName(name)
            .build());
        if (result.getSuccess()) {
            return true;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public Map<String, List<String>> listGroups(String host) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        FileRpcGrpc.FileRpcBlockingStub stub = FileRpcGrpc.newBlockingStub(channel);
        Messages.Group result = stub.listGroups(Messages.Token.newBuilder().build());
        if (result.getSuccess()) {
            Map<String, List<String>> map = new HashMap<>();
            for (Map.Entry<String, Messages.Group.GroupNames> entry : result.getGroupsMap().entrySet()) {
                map.put(entry.getKey(), entry.getValue().getNameList());
            }
            return map;
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public List<String> listProgramFiles(String host, String token, String name) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        FileRpcGrpc.FileRpcBlockingStub stub = FileRpcGrpc.newBlockingStub(channel);
        Messages.ProgramFilesResult result = stub.listProgramFiles(Messages.NameRequest.newBuilder().setName(name).setToken(token).build());
        if (result.getSuccess()) {
            return new ArrayList<>(result.getFilesList());
        } else {
            throw new IOException(result.getMessage());
        }
    }

    public Program readProgramConfiguration(String host, String name) throws IOException {
        ManagedChannel channel = channelService.getChannel(host);
        FileRpcGrpc.FileRpcBlockingStub stub = FileRpcGrpc.newBlockingStub(channel);
        Messages.Program result = stub.readProgramConfiguration(Messages.NameRequest.newBuilder().setName(name).build());
        if (result.getSuccess()) {
            Program prgm = new Program();
            prgm.setName(result.getName());
            prgm.setCommand(result.getCommand());
            prgm.setPriority(result.getPriority());
            prgm.setAutostart(result.getAutostart());
            prgm.setAutorestart(result.getAutorestart());
            prgm.setStartsecs(result.getStartsecs());
            prgm.setStartretries(result.getStartretries());
            prgm.setExitcodes(result.getExitcodes());
            prgm.setStopwaitsecs(result.getStopwaitsecs());
            prgm.setEnvironment(result.getEnvironment());
            return prgm;
        } else {
            throw new IOException(result.getMessage());
        }
    }
}
