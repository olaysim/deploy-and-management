package dk.syslab.controller.statistics;

public class SystemInformation {
    private String os;
    private String manufacturer;
    private String baseboardManufacturer;
    private String baseboardModel;
    private int cpu;
    private int logicalCpu;
    private long memory;
    private String memoryStr;
    private long swap;
    private String swapStr;

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getBaseboardManufacturer() {
        return baseboardManufacturer;
    }

    public void setBaseboardManufacturer(String baseboardManufacturer) {
        this.baseboardManufacturer = baseboardManufacturer;
    }

    public String getBaseboardModel() {
        return baseboardModel;
    }

    public void setBaseboardModel(String baseboardModel) {
        this.baseboardModel = baseboardModel;
    }

    public int getCpu() {
        return cpu;
    }

    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    public int getLogicalCpu() {
        return logicalCpu;
    }

    public void setLogicalCpu(int logicalCpu) {
        this.logicalCpu = logicalCpu;
    }

    public long getMemory() {
        return memory;
    }

    public void setMemory(long memory) {
        this.memory = memory;
    }

    public String getMemoryStr() {
        return memoryStr;
    }

    public void setMemoryStr(String memoryStr) {
        this.memoryStr = memoryStr;
    }

    public long getSwap() {
        return swap;
    }

    public void setSwap(long swap) {
        this.swap = swap;
    }

    public String getSwapStr() {
        return swapStr;
    }

    public void setSwapStr(String swapStr) {
        this.swapStr = swapStr;
    }
}
