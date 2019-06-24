package dk.syslab.supv.dto;

public class SendData {
    private String data;
    private String type;

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
