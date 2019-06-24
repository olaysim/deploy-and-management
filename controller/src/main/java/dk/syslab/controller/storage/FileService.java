package dk.syslab.controller.storage;

import dk.syslab.controller.Configuration;
import dk.syslab.controller.broadcast.BroadcastService;
import dk.syslab.controller.rpc.model.FileUuidBytes;
import dk.syslab.controller.rpc.protobuf.DistributedMessages;
import dk.syslab.controller.rpc.protobuf.Messages;
import dk.syslab.controller.validation.ValidationService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ini4j.Wini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class FileService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final static char PATHS_SEPARATOR = '#';

    private ValidationService validator;
    private BroadcastService broadcastService;

    private String basepath;
    private String configdir;
    private String user;

    public FileService(ValidationService validator, BroadcastService broadcastService, Configuration configuration) {
        this.validator = validator;
        this.broadcastService = broadcastService;
        this.user = configuration.getRequiredProperty("supervisor.user");
        this.basepath  = configuration.getRequiredProperty("supervisor.basepath");
        this.configdir = configuration.getRequiredProperty("supervisor.configdir");
    }

    private Map<String, Map<String,String>> splitTransforms(Map<String, String> transforms) {
        if (transforms == null) return null;
        Map<String, Map<String, String>> t = new HashMap<>();

        for (Map.Entry<String, String> entry : transforms.entrySet()) {
            int idx = entry.getValue().indexOf(PATHS_SEPARATOR);
            if (idx > 0) {
                String filename = entry.getValue().substring(0, idx);
                String transf = entry.getValue().substring(idx + 1);
                if (validator.validateFilename(filename) && validator.validateFilename(transf)) {
                    if (!t.containsKey(entry.getKey())) {
                        t.put(entry.getKey(), new HashMap<>());
                    }
                    t.get(entry.getKey()).put(filename, transf);
                } else {
                    throw new IllegalArgumentException("Filename or Transform in TRANSFORMS parameters contains invalid characters!");
                }
            } else {
                throw new IllegalArgumentException("Filename or Transform is invalid and cannot be parsed!");
            }
        }
        return t;
    }

    public void saveChunk(String programUuid, String fileUuid, byte[] data) throws IOException {
        Path tempdir = Files.createTempDirectory("locateSupvTempDir");
        Path basedir = tempdir.getParent().resolve("supv-grpcupload-" + programUuid);
        Path targetFile = basedir.resolve(fileUuid);
        Files.createDirectories(basedir);
        FileUtils.writeByteArrayToFile(targetFile.toFile(), data, true);
//        if (request.getPartIndex() > -1) {
//            targetFile = basedir.resolve(request.getUuid()).resolve(String.format("%s_%05d", request.getUuid(), request.getPartIndex()));
//        } else {
//            targetFile = basedir.resolve(request.getUuid()).resolve(request.getFileName());
//        }
//        try {
//            Files.createDirectories(targetFile.getParent());
//            Files.copy(request.getFile().getInputStream(), targetFile);
//        } catch (IOException e) {
//            String errorMsg = String.format("Error occurred when saving file with uuid = [%s]", request);
//            log.error(errorMsg, e);
//            throw new IOException(errorMsg, e);
//        }
    }

    public void storeProgram(Messages.Program program) throws IOException {
        if (program.getUuidFilenamesMap().size() <= 0) return;
        if (!validator.validateName(program.getName())) {
            throw new IllegalArgumentException("Program name contains invalid characters!");
        }
        Path tempdir = Files.createTempDirectory("locateSupvTempDir");
        Path sourcedir = tempdir.getParent().resolve("supv-grpcupload-" + program.getProgramUuid());
        Path targetdir = Paths.get(basepath, program.getName());
        Map<String, String> paths = program.getPathsMap();
        Map<String, Map<String, String>> transforms = splitTransforms(program.getTransformsMap());
        String nodename = broadcastService.getSelf().getName();

        for (Map.Entry<String, String> entry : program.getUuidFilenamesMap().entrySet()) {
            Path sourceFile = sourcedir.resolve(entry.getKey());
            String relativePath = program.getUuidPathsMap().get(entry.getKey());
            Path targetFile = null;
            String filename = entry.getValue();
            if (transforms != null && nodename != null && transforms.get(nodename) != null) {
                String transf = transforms.get(nodename).get(filename);
                if (transf != null && !transf.isEmpty()) {
                    filename = transf;
                }
            }
            if (paths != null) {
                String tmp = paths.get(filename);
                if (tmp != null && !tmp.isEmpty()) {
                    relativePath = tmp;
                }
            }
            if (relativePath != null && !relativePath.isEmpty()) {
                targetFile = targetdir.resolve(relativePath).resolve(filename);
            } else {
                targetFile = targetdir.resolve(filename);
            }
            Files.createDirectories(targetFile.getParent());
            Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void storeConfiguration(Messages.Program program) throws IOException {
        if (!validator.validateName(program.getName())) {
            throw new IllegalArgumentException("Program name contains invalid characters!");
        }
        Path programdir = Paths.get(basepath, program.getName());
        File f = Paths.get(configdir, program.getName() + ".conf").toFile();
        if (Files.notExists(Paths.get(configdir))) {
            Files.createDirectories(Paths.get(configdir));
        }
        Wini conf = new Wini();
        String section = "program:" + program.getName();
        if (f.exists()) {
            conf.load(f);
        }
        // set user and directory
        conf.put(section, "user", user);
        conf.put(section, "directory", programdir);

        // update config
        if (program.getCommand() != null && !program.getCommand().isEmpty()) conf.put(section, "command", program.getCommand());
        if (program.getPriority() != 0 && program.getPriority() != 999) conf.put(section, "priority", program.getPriority());
         conf.put(section, "autostart", program.getAutostart());
        if (program.getAutorestart() != null && !program.getAutorestart().isEmpty()) conf.put(section, "autorestart", program.getAutorestart());
        if (program.getStartsecs() > 0) conf.put(section, "startsecs", program.getStartsecs());
        if (program.getStartretries() != 3) conf.put(section, "startretries", program.getStartretries());
        if (program.getExitcodes() != null && !program.getExitcodes().isEmpty()) conf.put(section, "exitcodes", program.getExitcodes());
        if (program.getStopwaitsecs() != 10) conf.put(section, "stopwaitsecs", program.getStopwaitsecs());
        if (program.getEnvironment() != null && !program.getEnvironment().isEmpty()) conf.put(section, "environment", program.getEnvironment());

        conf.store(f);
    }

    public void deleteProgram(String program) throws IOException {
        if (!validator.validateName(program)) {
            throw new IllegalArgumentException("Program name contains invalid characters!");
        }
        Path programdir = Paths.get(basepath, program);
        Path config = Paths.get(configdir, program + ".conf");
        Files.deleteIfExists(config);
        FileUtils.deleteDirectory(programdir.toFile());
    }

    public void addProgramToGroup(String group, String name, int priority) throws IOException {
        if (!validator.validateName(group) || !validator.validateName(name)) {
            throw new IllegalArgumentException("Group name or Program name contains invalid characters!");
        }
        Path config = Paths.get(configdir, "group_" + group + ".conf");
        String section = "group:" + group;
        if (Files.notExists(config)) {
            Wini conf = new Wini();
            conf.put(section, "programs", name);
            conf.put(section, "priority", priority);
            conf.store(config.toFile());
        } else {
            Wini conf = new Wini(config.toFile());
            String programs = conf.get(section, "programs");
            if (Arrays.asList(programs.split(",")).contains(name)) {
                return;
            }
            conf.put(section, "programs", programs + "," + name);
            conf.put(section, "priority", priority);
            conf.store();
        }
    }

    public void removeProgramFromGroup(String group, String name) throws IOException {
        if (!validator.validateName(group) || !validator.validateName(name)) {
            throw new IllegalArgumentException("Group name or Program name contains invalid characters!");
        }
        Path config = Paths.get(configdir, "group_" + group + ".conf");
        String section = "group:" + group;
        if (Files.notExists(config)) {
            throw new IllegalArgumentException("BAD_NAME");
        } else {
            Wini conf = new Wini(config.toFile());
            String programs = conf.get(section, "programs");
            List<String> names = new LinkedList<>(Arrays.asList(programs.split(",")));
            names.remove(name);
            if (names.size() >= 1) {
                conf.put(section, "programs", StringUtils.join(names, ','));
                conf.store();
            } else {
                Files.deleteIfExists(config);
            }
        }
    }

    public Groups listGroups() throws IOException {
        File config = Paths.get(configdir).toFile();
        Groups groups = new Groups();
        if (config != null && config.exists()) {
            for (File f : config.listFiles(pathname -> pathname.getName().startsWith("group_"))) {
                Wini conf = new Wini(f);
                String key = f.getName().substring(6, f.getName().indexOf('.'));
                String programs = conf.get("group:" + key, "programs");
                for (String name : Arrays.asList(programs.split(","))) {
                    groups.add(key, name);
                }
            }
        }
        return groups;
    }

    public List<String> listProgramFiles(String program) throws IOException {
        if (!validator.validateName(program)) {
            throw new IllegalArgumentException("Program name contains invalid characters!");
        }
        List<String> files = new ArrayList<>();
        File programdir = Paths.get(basepath, program).toFile();
        if (programdir.exists()) {
            for (File file : FileUtils.listFiles(programdir, null, true)) {
                files.add(file.getPath().substring(programdir.getPath().length()).replace('\\', '/'));
            }
        } else {
            throw new IllegalArgumentException("Could not locate directory to scan for files");
        }
        return files;
    }

    public Messages.Program readProgramConfiguration(String programname) {
        if (!validator.validateName(programname)) {
            throw new IllegalArgumentException("Program name contains invalid characters!");
        }
        Messages.Program.Builder program = Messages.Program.newBuilder();
        File config_dir = Paths.get(configdir).toFile();
        if (config_dir.exists()) {
            for (File file : FileUtils.listFiles(config_dir, null, false)) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(programname.toLowerCase() + ".conf")) {
                    try {
                        Wini conf = new Wini(file);
                        String section = "program:" + programname;
                        program.setName(programname);
                        program.setCommand(conf.get(section, "command", String.class));
                        program.setPriority(conf.get(section, "priority", Integer.class));
                        program.setAutostart(conf.get(section, "autostart", Boolean.class));
                        program.setAutorestart(conf.get(section, "autorestart", String.class));
                        program.setStartsecs(conf.get(section, "startsecs", Integer.class));
                        program.setStartretries(conf.get(section, "startretries", Integer.class));
                        program.setExitcodes(conf.get(section, "exitcodes", String.class));
                        program.setStopwaitsecs(conf.get(section, "stopwaitsecs", Integer.class));
                        program.setEnvironment(conf.get(section, "environment", String.class));
                    } catch (Exception ignore) {}
                    return program.build();
                }
            }
        }
        throw new IllegalArgumentException("Program not found!");
    }

    public List<FileUuidBytes> generateFileUuidBytesList(DistributedMessages.DistributedProgram program) throws IOException {
        List<FileUuidBytes> list = new ArrayList<>();
        Path tempdir = Files.createTempDirectory("locateSupvTempDir");
        Path sourcedir = tempdir.getParent().resolve("supv-grpcupload-" + program.getProgramUuid());
        for (Map.Entry<String, String> entry : program.getUuidFilenamesMap().entrySet()) {
            FileUuidBytes file = new FileUuidBytes();
//            Path sourceFile = sourcedir.resolve(entry.getKey()).resolve(entry.getValue());
            Path sourceFile = sourcedir.resolve(entry.getKey());
            file.setFile(sourceFile);
            file.setProgramUuid(program.getProgramUuid());
            file.setFileUuid(entry.getKey());
            list.add(file);
        }
        return list;
    }
}
