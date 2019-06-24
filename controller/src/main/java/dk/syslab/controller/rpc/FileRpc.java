package dk.syslab.controller.rpc;

import dk.syslab.controller.rpc.protobuf.FileRpcGrpc;
import dk.syslab.controller.rpc.protobuf.Messages;
import dk.syslab.controller.storage.FileService;
import dk.syslab.controller.storage.Groups;
import dk.syslab.controller.validation.ValidationException;
import dk.syslab.controller.validation.ValidationService;
import io.grpc.stub.StreamObserver;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class FileRpc extends FileRpcGrpc.FileRpcImplBase {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private ValidationService validationService;
    private FileService fileService;

    public FileRpc(ValidationService validationService, FileService fileService) {
        this.validationService = validationService;
        this.fileService = fileService;
    }

    @Override
    public StreamObserver<Messages.Chunk> uploadFiles(StreamObserver<Messages.Result> responseObserver) {
        return new StreamObserver<Messages.Chunk>() {
            boolean success = true;
            String message;

            @Override
            public void onNext(Messages.Chunk value) {
                try {
                    Claims claims = validationService.validate(value.getToken());
                    if (claims != null) {
                        fileService.saveChunk(value.getProgramUuid(), value.getFileUuid(), value.getData().toByteArray());
                    }
                } catch (IOException e) {
                    success = false;
                    message = e.getMessage();
                    log.error("Saving file chuck failed", e);
                    responseObserver.onError(e);
                } catch (ValidationException ex) {
                    success = false;
                    message = ex.getMessage();
                    log.error("Authentication failed", ex);
                    responseObserver.onError(ex);
                }
            }

            @Override
            public void onError(Throwable t) {
                success = false;
                message = t.getMessage();
                log.error("Transferring file chunk failed", t);
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                if (success) {
                    responseObserver.onNext(Messages.Result.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK).build());
                } else {
                    responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(message).build());
                }
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void uploadProgram(Messages.Program request, StreamObserver<Messages.Result> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                fileService.storeConfiguration(request);
                fileService.storeProgram(request);
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK).build());
            } else {
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not authenticated").build());
            }
        } catch (ValidationException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } catch (IOException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteProgram(Messages.NameRequest request, StreamObserver<Messages.Result> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                fileService.deleteProgram(request.getName());
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK).build());
            } else {
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not authenticated").build());
            }
        } catch (ValidationException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } catch (IOException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void addProgramToGroup(Messages.GroupNameRequest request, StreamObserver<Messages.Result> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                int priority = request.getPritority();
                if (priority == 0) priority = 999;
                fileService.addProgramToGroup(request.getGroup(), request.getName(), priority);
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK).build());
            } else {
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not authenticated").build());
            }
        } catch (ValidationException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } catch (IOException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void removeProgramFromGroup(Messages.GroupNameRequest request, StreamObserver<Messages.Result> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                fileService.removeProgramFromGroup(request.getGroup(), request.getName());
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK).build());
            } else {
                responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not authenticated").build());
            }
        } catch (ValidationException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } catch (IOException e) {
            responseObserver.onNext(Messages.Result.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void listGroups(Messages.Token request, StreamObserver<Messages.Group> responseObserver) {
        try {
            Groups groups = fileService.listGroups();
            Messages.Group.Builder builder = Messages.Group.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK);
            for (Map.Entry<String, List<String>> entry : groups.getGroups().entrySet()) {
                builder.putGroups(entry.getKey(), Messages.Group.GroupNames.newBuilder()
                    .addAllName(entry.getValue())
                    .build()
                );
            }
            responseObserver.onNext(builder.build());
        } catch (ValidationException e) {
            responseObserver.onNext(Messages.Group.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } catch (IOException e) {
            responseObserver.onNext(Messages.Group.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void listProgramFiles(Messages.NameRequest request, StreamObserver<Messages.ProgramFilesResult> responseObserver) {
        try {
            Claims claims = validationService.validate(request.getToken());
            if (claims != null) {
                List<String> files = fileService.listProgramFiles(request.getName());
                responseObserver.onNext(Messages.ProgramFilesResult.newBuilder().addAllFiles(files).setSuccess(true).setCode(Messages.ResultCode.OK).build());
            } else {
                responseObserver.onNext(Messages.ProgramFilesResult.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage("Not authenticated").build());
            }
        } catch (ValidationException e) {
            responseObserver.onNext(Messages.ProgramFilesResult.newBuilder().setSuccess(false).setCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } catch (IOException e) {
            responseObserver.onNext(Messages.ProgramFilesResult.newBuilder().setSuccess(false).setCode(Messages.ResultCode.INTERNAL_SERVER_ERROR).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void readProgramConfiguration(Messages.NameRequest request, StreamObserver<Messages.Program> responseObserver) {
        try {
            Messages.Program program = fileService.readProgramConfiguration(request.getName());
            responseObserver.onNext(Messages.Program.newBuilder().mergeFrom(program).setSuccess(true).setResultCode(Messages.ResultCode.OK).build());
        } catch (ValidationException e) {
            responseObserver.onNext(Messages.Program.newBuilder().setSuccess(false).setResultCode(Messages.ResultCode.UNAUTHORIZED).setMessage(e.getMessage()).build());
        } finally {
            responseObserver.onCompleted();
        }
    }
}
