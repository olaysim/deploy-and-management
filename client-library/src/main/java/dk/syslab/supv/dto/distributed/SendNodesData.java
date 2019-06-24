package dk.syslab.supv.dto.distributed;

import java.util.ArrayList;
import java.util.List;

public class SendNodesData {
    private List<String> nodes;
    private String data;
    private String type;

    public SendNodesData() {
        this.nodes = new ArrayList<>();
    }

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isDataReady() {
        return data != null && !data.isEmpty();
    }

    public boolean isTypeReady() {
        return type != null && !type.isEmpty();
    }
}
