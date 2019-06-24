package dk.syslab.supv.web.api;

import dk.syslab.supv.rpc.BroadcastRpcService;
import dk.syslab.supv.rpc.FileRpcService;
import dk.syslab.supv.rpc.model.FileUuidBytes;
import dk.syslab.supv.storage.FileService;
import dk.syslab.supv.web.Validator;
import dk.syslab.supv.web.api.model.FineUploaderProgram;
import dk.syslab.supv.web.api.model.FineUploaderRequest;
import dk.syslab.supv.web.api.model.FineUploaderResponse;
import dk.syslab.supv.web.api.model.Result;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
public class FineUploaderController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    Validator validator;

    @Autowired
    FileService fileService;

    @Autowired
    FileRpcService fileRpcService;

    @Autowired
    BroadcastRpcService broadcastRpcService;

    @RequestMapping(
        value = "/api/{node}/fineuploader/upload",
        method = RequestMethod.POST
    )
    public ResponseEntity<FineUploaderResponse> fineUploaderUpload(@RequestHeader(value = "Authorization", required = false) String token,
                                                                  @RequestParam(value = "qqfile", required = false) MultipartFile file,
                                                                  @RequestParam(value = "qqtransaction", required = false) String transactionUuid,
                                                                  @RequestParam(value = "qquuid", required = false) String uuid,
                                                                  @RequestParam(value = "qqfilename", required = false) String fileName,
                                                                  @RequestParam(value = "qqpath", required = false, defaultValue = "") String path,
                                                                  @RequestParam(value = "qqpartindex", required = false, defaultValue = "-1") int partIndex,
                                                                  @RequestParam(value = "qqtotalparts", required = false, defaultValue = "-1") int totalParts,
                                                                  @RequestParam(value = "qqtotalfilesize", required = false, defaultValue = "-1") long totalFileSize,
                                                                   @PathVariable(required = false) String node,
                                                                  HttpServletResponse response) {
        if (transactionUuid == null || transactionUuid.isEmpty()) return ResponseEntity.badRequest().body(new FineUploaderResponse(false, "Transaction UUID was not provided"));
        if (uuid == null || uuid.isEmpty()) return ResponseEntity.badRequest().body(new FineUploaderResponse(false, "UUID was not provided"));
        if (fileName == null || fileName.isEmpty()) return ResponseEntity.badRequest().body(new FineUploaderResponse(false, "Filename was not provided"));
        if (file == null) return ResponseEntity.badRequest().body(new FineUploaderResponse(false, "File was not provided"));
        try {
            Claims claims = validator.validate(token, response); // this one does not use the response entity, the code is older, the response will not be correct for fine-uploader
            if (claims != null) {
                try {
                    if (!validator.isAdmin(claims) && (!validator.validateName(claims.getSubject()))) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new FineUploaderResponse(false, "Username is invalid or not allowed!"));
                    }
                    FineUploaderRequest request = new FineUploaderRequest(transactionUuid, uuid, file);
                    request.setFileName(fileName);
                    request.setPath(path);
                    request.setTotalFileSize(totalFileSize);
                    request.setPartIndex(partIndex);
                    request.setTotalParts(totalParts);

                    fileService.saveChunk(request);
                    return ResponseEntity.ok().body(new FineUploaderResponse(true));
                } catch (IOException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new FineUploaderResponse(false, e.getMessage()));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new FineUploaderResponse(false, e.getMessage()));
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new FineUploaderResponse(false, "Not Authenticated"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new FineUploaderResponse(false, "Not Authenticated"));
        }
    }

    @RequestMapping(
        value = "/api/{node}/fineuploader/done",
        method = RequestMethod.POST
    )
    public ResponseEntity<FineUploaderResponse> fineUploaderMergeChunks(@RequestHeader(value = "Authorization", required = false) String token,
                                                                        @RequestParam(value = "qqtransaction", required = false) String transactionUuid,
                                                                        @RequestParam(value = "qquuid", required = false) String uuid,
                                                                        @RequestParam(value = "qqfilename", required = false) String fileName,
                                                                        @RequestParam(value = "qqpath", required = false, defaultValue = "") String path,
                                                                        @RequestParam(value = "qqtotalparts", required = false) int totalParts,
                                                                        @RequestParam(value = "qqtotalfilesize", required = false) long totalFileSize,
                                                                        @PathVariable(required = false) String node,
                                                                        HttpServletResponse response) {
        if (transactionUuid == null || transactionUuid.isEmpty()) return ResponseEntity.badRequest().body(new FineUploaderResponse(false, "Transaction UUID was not provided"));
        if (uuid == null || uuid.isEmpty()) return ResponseEntity.badRequest().body(new FineUploaderResponse(false, "UUID was not provided"));
        if (totalParts <= 0) return ResponseEntity.badRequest().body(new FineUploaderResponse(false, "Total parts can not be 0 or less"));
        if (totalFileSize <= 0) return ResponseEntity.badRequest().body(new FineUploaderResponse(false, "Total filesize can not be 0 or less"));
        try {
            Claims claims = validator.validate(token, response); // this one does not use the response entity, the code is older, the response will not be correct for fine-uploader
            if (claims != null) {
                try {
                    if (!validator.isAdmin(claims) && (!validator.validateName(claims.getSubject()))) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new FineUploaderResponse(false, "Username is invalid or not allowed!"));
                    }
                    fileService.mergeChunks(transactionUuid, uuid, fileName, totalParts, totalFileSize);
                    return ResponseEntity.ok().body(new FineUploaderResponse(true));
                } catch (IOException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new FineUploaderResponse(false, e.getMessage()));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new FineUploaderResponse(false, e.getMessage()));
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new FineUploaderResponse(false, "Not Authenticated"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new FineUploaderResponse(false, "Not Authenticated"));
        }
    }

    @RequestMapping(
        value = "/api/{node}/fineuploader/move",
        method = RequestMethod.POST
    )
    public Result uploadFilesToNode(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable String node, @RequestBody(required = false) FineUploaderProgram program, HttpServletResponse response) throws IOException {
        if (node == null || node.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No node host address supplied!");
            return null;
        }
        String target = broadcastRpcService.lookUpAddress(node);
        if (target == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to lookup address for node host!");
            return null;
        }
        if (program == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Requestbody is malformed (data is missing)");
            return null;
        }
        Claims claims = validator.validate(token, response);
        if (claims != null) {
            try {
                if (program.getName() == null || program.getName().isEmpty()) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No process name supplied");
                    return null;
                }
                List<FileUuidBytes> fileUuidBytes = fileService.generateFileUuidBytesList(program);
                fileRpcService.uploadFiles(target, token, fileUuidBytes);
                fileRpcService.uploadProgram(target, token, program);
                return new Result(true);
            } catch (IOException e) {
                log.error("unable to move data to program: " + program.getName(), e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return null;
            }
        }
        return null;
    }
}
