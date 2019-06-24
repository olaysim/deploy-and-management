package dk.syslab.controller.rpc.model;

import java.nio.file.Path;

public class FileUuidBytes {
    private String programUuid;
    private String fileUuid;
    private Path file;

    public FileUuidBytes() {
    }

    public FileUuidBytes(String programUuid, String fileUuid, Path file) {
        this.programUuid = programUuid;
        this.fileUuid = fileUuid;
        this.file = file;
    }

    public String getProgramUuid() {
        return programUuid;
    }

    public void setProgramUuid(String programUuid) {
        this.programUuid = programUuid;
    }

    public String getFileUuid() {
        return fileUuid;
    }

    public void setFileUuid(String fileUuid) {
        this.fileUuid = fileUuid;
    }

    public Path getFile() {
        return file;
    }

    public void setFile(Path file) {
        this.file = file;
    }
}
