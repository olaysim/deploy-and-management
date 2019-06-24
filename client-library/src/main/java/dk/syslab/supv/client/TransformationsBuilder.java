package dk.syslab.supv.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Convenience class to help build the transformations list for use with uploadProgram()
 */
public class TransformationsBuilder {
    private Map<String, Map<String, String>> transforms;

    public TransformationsBuilder() {
        transforms = new HashMap<>();
    }

    /**
     * Create the transformations builder or just use 'new'
     * @return an instance of this transformations builder
     */
    public static TransformationsBuilder create() {
        return new TransformationsBuilder();
    }

    /**
     * Create a list of transformations using the required syntax of the REST API
     * @return a list of transformations
     */
    public List<String> build() {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> entry : transforms.entrySet()) {
            if (entry.getKey() != null && !entry.getKey().isEmpty() && entry.getValue() != null) {
                for (Map.Entry<String, String> tentry : entry.getValue().entrySet()) {
                    if (tentry.getKey() != null && !tentry.getKey().isEmpty() && tentry.getValue() != null && !tentry.getValue().isEmpty())
                    list.add(entry.getKey() + "#" + tentry.getKey() + "#" + tentry.getValue());
                }
            }
        }
        return list;
    }

    /**
     * Add a transformation
     * @param nodename node name is the name of the node on which the transformation should happen
     * @param filename filename is the filmename of the original file, with extension
     * @param transformation transformation is the new filename that the file should be renamed to when stored on the given node
     * @return returns this, so multiple add()'s can be chained
     */
    public TransformationsBuilder add(String nodename, String filename, String transformation) {
        if (nodename == null || nodename.isEmpty() || nodename.contains("#"))
            throw new IllegalArgumentException("nodename path is not valid");
        if (filename == null || filename.isEmpty() || filename.contains("#"))
            throw new IllegalArgumentException("filename is not valid");
        if (transformation == null || transformation.isEmpty() || transformation.contains("#"))
            throw new IllegalArgumentException("transformation is not valid");
        if (!transforms.containsKey(nodename)) {
            transforms.put(nodename, new HashMap<>());
        }
        transforms.get(nodename).put(filename, transformation);
        return this;
    }

    /**
     * Add a transformation
     * @param nodename node name is the name of the node on which the transformation should happen
     * @param transformations transformations is a map of the filmename of the original file, with extension, and the new filename that the file should be renamed to when stored on the given node
     * @return returns this, so multiple add()'s can be chained
     */
    public TransformationsBuilder add(String nodename, Map<String, String> transformations) {
        if (nodename == null || nodename.isEmpty() || nodename.contains("#"))
            throw new IllegalArgumentException("nodename path is not valid");
        if (transformations == null)
            throw new IllegalArgumentException("transformations is not valid");
        if (!transforms.containsKey(nodename)) {
            transforms.put(nodename, new HashMap<>());
        }
        transforms.get(nodename).putAll(transformations);
        return this;
    }

    /**
     * Add a transformation
     * @param transformationmap transformations is a map with key node name that is the name of the node on which the transformation should happen and the key is a map of the filmename of the original file, with extension, and the new filename that the file should be renamed to when stored on the given node
     * @return returns this, so multiple add()'s can be chained
     */
    public TransformationsBuilder add(Map<String, Map<String, String>> transformationmap) {
        if (transformationmap == null)
            throw new IllegalArgumentException("transformationmap is not valid");
        transforms.putAll(transformationmap);
        return this;
    }
}
