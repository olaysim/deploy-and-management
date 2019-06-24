package dk.syslab.supv.web.api.model.distributed;

import dk.syslab.supv.web.api.model.Program;

import java.util.List;

public class ProgramNodes extends Program {
    private List<String> nodes;

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }
}
