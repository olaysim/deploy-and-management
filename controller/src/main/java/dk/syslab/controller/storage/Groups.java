package dk.syslab.controller.storage;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class Groups {
    private Map<String, List<String>> groups;

    public Groups() {
        groups = new Hashtable<>();
    }

    public void add(String group, String name) {
        if (!groups.containsKey(group)) {
            groups.put(group, new ArrayList<>());
        }
        groups.get(group).add(name);
    }

    public Map<String, List<String>> getGroups() {
        return groups;
    }
}
