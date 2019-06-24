package dk.syslab.supv.storage;

import dk.syslab.supv.rpc.model.FileUuidBytes;
import dk.syslab.supv.web.Validator;
import dk.syslab.supv.web.api.model.FineUploaderProgram;
import dk.syslab.supv.web.api.model.FineUploaderRequest;
import dk.syslab.supv.web.api.model.Program;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class FileService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final static char PATHS_SEPARATOR = '#';

    @Autowired
    Validator validator;

//    private String basepath;
//    private String configdir;
//    private String user;

//    public FileService(Environment env) {
//        this.user = env.getRequiredProperty("supervisor.user");
//        this.basepath  = env.getRequiredProperty("supervisor.basepath");
//        this.configdir = env.getRequiredProperty("supervisor.configdir");
//    }

    private Map<String, String> splitPaths(List<String> paths) {
        if (paths == null) return null;
        Map<String, String> p = new HashMap<>();
        for (String path : paths) {
            int idx = path.indexOf(PATHS_SEPARATOR);
            if (idx > 0) {
                String filename = path.substring(0, idx);
                String relativePath = path.substring(idx+1);
                // handle custom case where '/' is given as a relative path but really meaning ./
                if (relativePath.equalsIgnoreCase("/")) continue;
                // handle removing '/' in front of relative paths
                if (relativePath.startsWith("/")) {
                    relativePath = relativePath.substring(1);
                }
                if (validator.validateFilename(filename) && validator.validateDirectory(relativePath)) {
                    p.put(filename, relativePath);
                }
                else {
                    throw new IllegalArgumentException("Filename or Relative path in PATHS parameters contains invalid characters!");
                }
            }
        }
        return p;
    }

    private Map<String,String> reduceTransforms(List<String> transforms) {
        if (transforms == null) return null;
        Map<String, String> t = new HashMap<>();
        for (String transform : transforms) {
            int idx = transform.indexOf(PATHS_SEPARATOR);
            if (idx > 0) {
                String nodename = transform.substring(0, idx);
                String transf = transform.substring(idx + 1);
                t.put(nodename, transf);
            }
        }
        return t;
    }

//    private Map<String,Map<String,String>> splitTransforms(List<String> transforms) {
//        if (transforms == null) return null;
//        Map<String, Map<String, String>> t = new HashMap<>();
//        for (String transform : transforms) {
//            int idx1 = transform.indexOf(PATHS_SEPARATOR);
//            int idx2 = transform.indexOf(PATHS_SEPARATOR, idx1 + 1);
//            if (idx1 > 0 && idx2 > idx1) {
//                String nodename = transform.substring(0, idx1);
//                String filename = transform.substring(idx1 + 1, idx2);
//                String transf = transform.substring(idx2 + 1);
//                if (validator.validateFilename(filename) && validator.validateFilename(transf)) {
//                    if (!t.containsKey(nodename)) {
//                        t.put(nodename, new HashMap<>());
//                    }
//                    t.get(nodename).put(filename, transf);
//                } else {
//                    throw new IllegalArgumentException("Nodename, filename or transform in TRANSFORMS parameters contains invalid characters!");
//                }
//            }
//        }
//        return t;
//    }

    public FineUploaderProgram saveProgramToTemporaryLocation(Program program, String programUuid) {
        FineUploaderProgram prgm = new FineUploaderProgram(program);
        prgm.setTransaction(programUuid);
        prgm.setPaths(splitPaths(program.getPaths()));
        prgm.setTransforms(reduceTransforms(program.getTransforms()));
        if (program.getFiles() != null) {
            try {
                Path tempdir = Files.createTempDirectory("locateSupvTempDir");
                Path basedir = tempdir.getParent().resolve("supv-fileupload-" + programUuid);
                Map<String, String> uuidFilenames = new HashMap<>();
                prgm.setUuidFilenames(uuidFilenames);
                for (MultipartFile file : program.getFiles()) {
                    String fileUuid = UUID.randomUUID().toString();
                    Path targetFile = Paths.get(basedir.toString(), fileUuid, file.getOriginalFilename());
                    Files.createDirectories(targetFile.getParent());
                    FileOutputStream fo = new FileOutputStream(targetFile.toFile());
                    IOUtils.copy(file.getInputStream(), fo);
                    fo.close();
                    uuidFilenames.put(fileUuid, file.getOriginalFilename());
                }
            } catch (IOException e) {
                return null;
            }
        }
        return prgm;
    }

//    public void storeProgram(String name, Program program) throws IOException, IllegalArgumentException {
//        if (!validator.validateName(name) || !validator.validateName(program.getName())) {
//            throw new IllegalArgumentException("Username or Program name contains invalid characters!");
//        }
//        if (program.getFiles() != null) {
//            Path container = Paths.get(basepath, name);
//            Path programdir = Paths.get(basepath, name, program.getName());
//            if (Files.notExists(container)) {
//                Files.createDirectories(container);
//            }
//            if (Files.notExists(programdir)) {
//                Files.createDirectory(programdir);
//            }
//            Map<String, String> paths = splitPaths(program.getPaths());
//            Map<String, Map<String, String>> transforms = splitTransforms(program.getTransforms());
////            String nodename = broadcastService.getSelf().getName();
//            String nodename = null;
//            for (MultipartFile file : program.getFiles()) {
////                if (file.isEmpty())
////                    continue;
//                String filename = file.getOriginalFilename();
//                if (transforms != null && nodename != null && transforms.get(nodename) != null) {
//                    String tmp = transforms.get(nodename).get(filename);
//                    if (tmp != null && !tmp.isEmpty()) {
//                        filename = tmp;
//                    }
//                }
//                String relativePath = "";
//                if (paths != null) {
//                    String tmp = paths.get(filename);
//                    if (tmp != null && !tmp.isEmpty()) {
//                        relativePath = tmp;
//                    }
//                }
//                Path destFolder = Paths.get(basepath, name, program.getName(), relativePath);
//                if (Files.notExists(destFolder)) {
//                    Files.createDirectories(destFolder);
//                }
//                Path dest = Paths.get(basepath, name, program.getName(), relativePath, filename);
//                if (Files.exists(dest)) Files.deleteIfExists(dest);
//                FileOutputStream fo = new FileOutputStream(dest.toFile());
//                IOUtils.copy(file.getInputStream(), fo);
//                fo.close();
////                file.transferTo(new File(dest.toUri()));
//            }
//        }
//    }
//    public void storeProgramWithFileList(String name, Program program, List<File> files) throws IOException, IllegalArgumentException {
//        if (!validator.validateName(name) || !validator.validateName(program.getName())) {
//            throw new IllegalArgumentException("Username or Program name contains invalid characters!");
//        }
//        if (files != null) {
//            Path container = Paths.get(basepath, name);
//            Path programdir = Paths.get(basepath, name, program.getName());
//            if (Files.notExists(container)) {
//                Files.createDirectories(container);
//            }
//            if (Files.notExists(programdir)) {
//                Files.createDirectory(programdir);
//            }
//            Map<String, String> paths = splitPaths(program.getPaths());
//            Map<String, Map<String, String>> transforms = splitTransforms(program.getTransforms());
////            String nodename = broadcastService.getSelf().getName();
//            String nodename = null;
//            for (File file : files) {
//                if (!file.isFile() && !file.exists())
//                    continue;
//                String filename = file.getName();
//                if (transforms != null && nodename != null && transforms.get(nodename) != null) {
//                    String tmp = transforms.get(nodename).get(filename);
//                    if (tmp != null && !tmp.isEmpty()) {
//                        filename = tmp;
//                    }
//                }
//                String relativePath = "";
//                if (paths != null) {
//                    String tmp = paths.get(filename);
//                    if (tmp != null && !tmp.isEmpty()) {
//                        relativePath = tmp;
//                    }
//                }
//                Path destFolder = Paths.get(basepath, name, program.getName(), relativePath);
//                if (Files.notExists(destFolder)) {
//                    Files.createDirectories(destFolder);
//                }
//                Path dest = Paths.get(basepath, name, program.getName(), relativePath, filename);
//                if (Files.exists(dest)) Files.deleteIfExists(dest);
//                FileUtils.copyFile(file, dest.toFile());
//            }
//        }
//    }



//    public void storeConfiguration(String name, Program program) throws IOException {
//        if (!validator.validateName(name) || !validator.validateName(program.getName())) {
//            throw new IllegalArgumentException("Username or Program name contains invalid characters!");
//        }
//        Path programdir = Paths.get(basepath, name, program.getName());
//        File f = Paths.get(configdir, name + "_" + program.getName() + ".conf").toFile();
//        if (Files.notExists(Paths.get(configdir))) {
//            Files.createDirectories(Paths.get(configdir));
//        }
//        Wini conf = new Wini();
//        String section = "program:" + program.getName();
//        if (f.exists()) {
//            conf.load(f);
//        }
//        // set user and directory
//        conf.put(section, "user", user);
//        conf.put(section, "directory", programdir);
//
//        // update config
//        if (program.getCommand() != null && !program.getCommand().isEmpty()) conf.put(section, "command", program.getCommand());
//        if (program.getPriority() != null) conf.put(section, "priority", program.getPriority());
//        if (program.getAutostart() != null) conf.put(section, "autostart", program.getAutostart());
//        if (program.getAutorestart() != null && !program.getAutorestart().isEmpty()) conf.put(section, "autorestart", program.getAutorestart());
//        if (program.getStartsecs() != null) conf.put(section, "startsecs", program.getStartsecs());
//        if (program.getStartretries() != null) conf.put(section, "startretries", program.getStartretries());
//        if (program.getExitcodes() != null && !program.getExitcodes().isEmpty()) conf.put(section, "exitcodes", program.getExitcodes());
//        if (program.getStopwaitsecs() != null) conf.put(section, "stopwaitsecs", program.getStopwaitsecs());
//        if (program.getEnvironment() != null && !program.getEnvironment().isEmpty()) conf.put(section, "environment", program.getEnvironment());
//
//        conf.store(f);
//    }

//    public void deleteProgram(String name, String program) throws IOException {
//        if (!validator.validateName(name) || !validator.validateName(program)) {
//            throw new IllegalArgumentException("Username or Program name contains invalid characters!");
//        }
//        Path programdir = Paths.get(basepath, name, program);
//        Path config = Paths.get(configdir, name + "_" + program + ".conf");
//        Files.deleteIfExists(config);
//        FileUtils.deleteDirectory(programdir.toFile());
//    }

//    public void addProgramToGroup(String group, String name, int priority) throws IOException {
//        if (!validator.validateName(group) || !validator.validateName(name)) {
//            throw new IllegalArgumentException("Group name or Program name contains invalid characters!");
//        }
//        Path config = Paths.get(configdir, "group_" + group + ".conf");
//        String section = "group:" + group;
//        if (Files.notExists(config)) {
//            Wini conf = new Wini();
//            conf.put(section, "programs", name);
//            conf.put(section, "priority", priority);
//            conf.store(config.toFile());
//        } else {
//            Wini conf = new Wini(config.toFile());
//            String programs = conf.get(section, "programs");
//            if (Arrays.asList(programs.split(",")).contains(name)) {
//                return;
//            }
//            conf.put(section, "programs", programs + "," + name);
//            conf.put(section, "priority", priority);
//            conf.store();
//        }
//    }

//    public void removeProgramFromGroup(String group, String name) throws IOException {
//        if (!validator.validateName(group) || !validator.validateName(name)) {
//            throw new IllegalArgumentException("Group name or Program name contains invalid characters!");
//        }
//        Path config = Paths.get(configdir, "group_" + group + ".conf");
//        String section = "group:" + group;
//        if (Files.notExists(config)) {
//            throw new IllegalArgumentException("BAD_NAME");
//        } else {
//            Wini conf = new Wini(config.toFile());
//            String programs = conf.get(section, "programs");
//            List<String> names = new LinkedList<>(Arrays.asList(programs.split(",")));
//            names.remove(name);
//            if (names.size() >= 1) {
//                conf.put(section, "programs", StringUtils.collectionToCommaDelimitedString(names));
//                conf.store();
//            } else {
//                Files.deleteIfExists(config);
//            }
//        }
//    }

//    public Groups listGroups() throws IOException {
//        File config = Paths.get(configdir).toFile();
//        Groups groups = new Groups();
//        if (config != null && config.exists()) {
//            for (File f : config.listFiles(pathname -> pathname.getName().startsWith("group_"))) {
//                Wini conf = new Wini(f);
//                String key = f.getName().substring(6, f.getName().indexOf('.'));
//                String programs = conf.get("group:" + key, "programs");
//                for (String name : Arrays.asList(programs.split(","))) {
//                    groups.add(key, name);
//                }
//            }
//        }
//        return groups;
//    }

//    public List<String> listProgramFiles(String name, String program) throws IOException {
//        if (!validator.validateName(name) || !validator.validateName(program)) {
//            throw new IllegalArgumentException("Username or Program name contains invalid characters!");
//        }
//        List<String> files = new ArrayList<>();
//        File programdir = Paths.get(basepath, name, program).toFile();
//        if (programdir.exists()) {
//            for (File file : FileUtils.listFiles(programdir, null, true)) {
//                files.add(file.getPath().substring(programdir.getPath().length()).replace('\\', '/'));
//            }
//        } else {
//            throw new IllegalArgumentException("Could not locate directory to scan for files");
//        }
//        return files;
//    }

//    public Program readProgramConfiguration(String programname) {
//        if (!validator.validateName(programname)) {
//            throw new IllegalArgumentException("Username or Program name contains invalid characters!");
//        }
//        Program program = new Program();
//        // don't actually know the name, anybody could be owner... this is an issue..
//        // but it is not an issue because there can only exist one program with a specific name in supervisor anyways.. names are unique
//        File config_dir = Paths.get(configdir).toFile();
//        if (config_dir.exists()) {
//            for (File file : FileUtils.listFiles(config_dir, null, false)) {
//                if (file.isFile() && file.getName().toLowerCase().endsWith(programname.toLowerCase() + ".conf")) {
//                    try {
//                        Wini conf = new Wini(file);
//                        String section = "program:" + programname;
//                        program.setName(programname);
//                        program.setCommand(conf.get(section, "command", String.class));
//                        program.setPriority(conf.get(section, "priority", Integer.class));
//                        program.setAutostart(conf.get(section, "autostart", Boolean.class));
//                        program.setAutorestart(conf.get(section, "autorestart", String.class));
//                        program.setStartsecs(conf.get(section, "startsecs", Integer.class));
//                        program.setStartretries(conf.get(section, "startretries", Integer.class));
//                        program.setExitcodes(conf.get(section, "exitcodes", String.class));
//                        program.setStopwaitsecs(conf.get(section, "stopwaitsecs", Integer.class));
//                        program.setEnvironment(conf.get(section, "environment", String.class));
//                    } catch (Exception ignore) {}
//                    return program;
//                }
//            }
//        }
//        return program;
//    }

    public void saveChunk(FineUploaderRequest request) throws IOException {
        if (request.getFile().isEmpty()) {
            throw new IOException(String.format("File with uuid = [%s] is empty", request.getUuid().toString()));
        }

        Path tempdir = Files.createTempDirectory("locateSupvTempDir");
        Path basedir = tempdir.getParent().resolve("supv-fileupload-" + request.getTransactionUuid());
        Path targetFile;
        if (request.getPartIndex() > -1) {
            targetFile = basedir.resolve(request.getUuid()).resolve(String.format("%s_%05d", request.getUuid(), request.getPartIndex()));
        } else {
            targetFile = basedir.resolve(request.getUuid()).resolve(request.getFileName());
        }
        try {
            Files.createDirectories(targetFile.getParent());
            Files.copy(request.getFile().getInputStream(), targetFile);
        } catch (IOException e) {
            String errorMsg = String.format("Error occurred when saving file with uuid = [%s]", request);
            log.error(errorMsg, e);
            throw new IOException(errorMsg, e);
        }
    }

    public void mergeChunks(String transaction, String uuid, String fileName, int totalParts, long totalFileSize) throws IOException {
        Path tempdir = Files.createTempDirectory("locateSupvTempDir");
        Path basedir = tempdir.getParent().resolve("supv-fileupload-" + transaction);
        File targetFile = basedir.resolve(uuid).resolve(fileName).toFile();
        try (FileChannel dest = new FileOutputStream(targetFile, true).getChannel()) {
            for (int i = 0; i < totalParts; i++) {
                File sourceFile = basedir.resolve(uuid).resolve(String.format("%s_%05d", uuid, i)).toFile();
                try (FileChannel src = new FileInputStream(sourceFile).getChannel()) {
                    dest.position(dest.size());
                    src.transferTo(0, src.size(), dest);
                }
                sourceFile.delete();
            }
        } catch (IOException e) {
            String errorMsg = String.format("Error occurred when merging chunks for uuid = [%s]", uuid);
            log.error(errorMsg, e);
            throw new IOException(errorMsg, e);
        }
    }

//    public void moveData(String name, FineUploaderProgram program) throws IOException {
//        Path tempdir = Files.createTempDirectory("locateSupvTempDir");
//        Path sourcedir = tempdir.getParent().resolve("supv-fileupload-" + program.getTransaction());
//        Path targetdir = Paths.get(basepath, name, program.getName());
//        for (Map.Entry<String, String> entry : program.getUuidFilenames().entrySet()) {
//            Path sourceFile = sourcedir.resolve(entry.getKey()).resolve(entry.getValue());
//            String relativePath = program.getUuidPaths().get(entry.getKey());
//            Path targetFile = null;
//            if (relativePath != null && !relativePath.isEmpty()) {
//                targetFile = targetdir.resolve(relativePath).resolve(entry.getValue());
//            } else {
//                targetFile = targetdir.resolve(entry.getValue());
//            }
//            Files.createDirectories(targetFile.getParent());
//            Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
//        }
//    }

    public List<FileUuidBytes> generateFileUuidBytesList(FineUploaderProgram program) throws IOException {
        List<FileUuidBytes> list = new ArrayList<>();
        Path tempdir = Files.createTempDirectory("locateSupvTempDir");
        Path sourcedir = tempdir.getParent().resolve("supv-fileupload-" + program.getTransaction());
        for (Map.Entry<String, String> entry : program.getUuidFilenames().entrySet()) {
            FileUuidBytes file = new FileUuidBytes();
            Path sourceFile = sourcedir.resolve(entry.getKey()).resolve(entry.getValue());
            file.setFile(sourceFile);
            file.setProgramUuid(program.getTransaction());
            file.setFileUuid(entry.getKey());
            list.add(file);
        }
        return list;
    }
}
