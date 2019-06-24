package dk.syslab.supv.web.api.model.distributed;

import java.util.ArrayList;
import java.util.List;

public class RequestNodes {
    private List<String> nodes;

    public RequestNodes() {
        this.nodes = new ArrayList<>();
    }

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }
}
