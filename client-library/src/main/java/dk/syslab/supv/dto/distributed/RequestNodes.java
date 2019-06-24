package dk.syslab.supv.dto.distributed;

import java.util.List;

public class RequestNodes {
    private List<String> nodes;

    public RequestNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }
}
