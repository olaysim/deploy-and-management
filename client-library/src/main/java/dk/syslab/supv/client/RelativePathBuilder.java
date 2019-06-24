package dk.syslab.supv.client;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Convenience class to help build the relative paths list for use with uploadProgram()
 */
public class RelativePathBuilder {
    private Map<String, String> paths;
    private int programdir = -1;

    public RelativePathBuilder() {
        paths = new HashMap<>();
    }

    /**
     * Create the path builder or just use 'new'
     * @return an instance of this path builder
     */
    public static RelativePathBuilder create() {
        return new RelativePathBuilder();
    }

    /**
     * Create a list of relative paths using the required syntax of the REST API
     * @return a list of relative paths
     */
    public List<String> build() {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : paths.entrySet()) {
            list.add(entry.getKey() + "#" + entry.getValue());
        }
        return list;
    }

    /**
     * Add a relative path
     * @param filename filename is the filmename of the file, with extension
     * @param relativePath relativePath is a string of the path. The path must be relative to the program folder and NOT the system root, it is NOT an absolute path.
     * @return returns this, so multiple add()'s can be chained
     */
    public RelativePathBuilder add(String filename, String relativePath) {
        if (filename == null || filename.isEmpty() || filename.contains("#"))
            throw new IllegalArgumentException("filename is not valid");
        if (relativePath == null || relativePath.isEmpty() || relativePath.contains("#"))
            throw new IllegalArgumentException("relative path is not valid");
        paths.put(filename, relativePath);
        return this;
    }

    /**
     * Set the program dir. Use before adding files with File as argument.
     * @param dir the base dir of the program
     * @return returns this, so multiple add()'s can be chained
     */
    public RelativePathBuilder setProgramDir(String dir) {
        if (dir.endsWith("/"))
            this.programdir = dir.length();
        else
            this.programdir = dir.length() + 1;
        return this;
    }
    /**
     * Set the program dir. Use before adding files with File as argument.
     * @param dir the base dir of the program
     * @return returns this, so multiple add()'s can be chained
     */
    public RelativePathBuilder setProgramDir(File dir) {
        if (dir == null)
            throw new IllegalArgumentException("directory is null");
        if (!dir.exists())
            throw new IllegalArgumentException("directory does not exist");
        this.programdir = dir.getParent().length();
        return this;
    }

    /**
     * Add a relative path by adding a File<br />
     * It is requires to first use setProgramDir() with the basedir of the program, this is used to subtract the absolute dir from the file.
     * The file is then added with its relative path to the program dir.<br />
     * A file's relative path is only added if the file is not located in the program dir base.
     * @param file the file do add a relative path for
     * @return returns this, so multiple add()'s can be chained
     */
    public RelativePathBuilder add(File file) {
        if (file == null)
            throw new IllegalArgumentException("file is null");
        if (!file.exists())
            throw new IllegalArgumentException("file does not exist");
        if (programdir < 0)
            throw new IllegalArgumentException("must set program dir before adding paths using File");
        if (programdir < file.getPath().length() - file.getName().length() ) {
            paths.put(file.getName(), file.getPath().substring(programdir, file.getPath().length() - file.getName().length() - 1));
        }
        return this;
    }
}
