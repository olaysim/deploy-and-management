package dk.syslab.supv.dto.distributed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReloadMap {
    private List<String> added;
    private List<String> changed;
    private List<String> removed;

    public List<String> getAdded() {
        return added;
    }

    public void setAdded(List<String> added) {
        this.added = added;
    }

    public List<String> getChanged() {
        return changed;
    }

    public void setChanged(List<String> changed) {
        this.changed = changed;
    }

    public List<String> getRemoved() {
        return removed;
    }

    public void setRemoved(List<String> removed) {
        this.removed = removed;
    }
}
