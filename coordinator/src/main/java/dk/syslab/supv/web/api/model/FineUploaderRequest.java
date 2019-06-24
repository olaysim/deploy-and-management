package dk.syslab.supv.web.api.model;

import org.springframework.web.multipart.MultipartFile;

public class FineUploaderRequest {
    private final String transactionUuid;
    private final String uuid;
    private final MultipartFile file;
    private int partIndex = -1;
    private long partSize = -1;
    private int totalParts = -1;
    private long totalFileSize = -1;
    private String fileName;
    private String path;

    public FineUploaderRequest(String transactionUuid, String uuid, MultipartFile file) {
        this.uuid = uuid;
        this.file = file;
        this.transactionUuid = transactionUuid;
    }

    public String getUuid() {
        return uuid;
    }

    public MultipartFile getFile() {
        return file;
    }

    public int getPartIndex() {
        return partIndex;
    }

    public void setPartIndex(int partIndex) {
        this.partIndex = partIndex;
    }

    public long getPartSize() {
        return partSize;
    }

    public void setPartSize(long partSize) {
        this.partSize = partSize;
    }

    public int getTotalParts() {
        return totalParts;
    }

    public void setTotalParts(int totalParts) {
        this.totalParts = totalParts;
    }

    public long getTotalFileSize() {
        return totalFileSize;
    }

    public void setTotalFileSize(long totalFileSize) {
        this.totalFileSize = totalFileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTransactionUuid() {
        return transactionUuid;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "UploadRequest{" +
            "transaction='" + transactionUuid + "'" +
            ", uuid='" + uuid + '\'' +
            ", path='" + path + '\'' +
            ", partIndex=" + partIndex +
            ", partSize=" + partSize +
            ", totalParts=" + totalParts +
            ", totalFileSize=" + totalFileSize +
            ", fileName='" + fileName + '\'' +
            '}';
    }
}
