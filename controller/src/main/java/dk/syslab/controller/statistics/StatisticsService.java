package dk.syslab.controller.statistics;

import dk.syslab.controller.xmlrpc.ProcessInfo;
import dk.syslab.controller.xmlrpc.ProcessState;
import dk.syslab.controller.xmlrpc.XmlRpcService;
import oshi.SystemInfo;
import oshi.hardware.Baseboard;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;
import oshi.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StatisticsService {
    private ProcessStatistics processStatistics;
    private SystemStatistics systemStatistics;
    private SystemInformation systemInformation;

    private ScheduledExecutorService executorService;

    private SystemInfo si;
    private GlobalMemory mem;
    private OperatingSystem os;
    private CentralProcessor pro;

    private XmlRpcService xmlRpcService;

    public StatisticsService(XmlRpcService xmlRpcService) {
        this.xmlRpcService = xmlRpcService;

        processStatistics = new ProcessStatistics();
        systemStatistics = new SystemStatistics();
        systemInformation = new SystemInformation();
        executorService = Executors.newScheduledThreadPool(3);

        si = new SystemInfo();
        mem = si.getHardware().getMemory();
        os = si.getOperatingSystem();
        pro = si.getHardware().getProcessor();

        loadInformation();
        submitThreads();
    }

    private void loadInformation() {
        systemInformation.setOs(os.toString());
        systemInformation.setManufacturer(os.getManufacturer());
        final Baseboard baseboard = si.getHardware().getComputerSystem().getBaseboard();
        systemInformation.setBaseboardManufacturer(baseboard.getManufacturer());
        systemInformation.setBaseboardModel(baseboard.getModel());
        systemInformation.setCpu(pro.getPhysicalProcessorCount());
        systemInformation.setLogicalCpu(pro.getLogicalProcessorCount());
        systemInformation.setMemory(mem.getTotal());
        systemInformation.setMemoryStr(FormatUtil.formatBytes(mem.getTotal()));
        systemInformation.setSwap(mem.getSwapTotal());
        systemInformation.setSwapStr(FormatUtil.formatBytes(mem.getSwapTotal()));
    }

    private void submitThreads() {
        // schedule 1 minute samples for hour collection
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                long timestamp = getMinuteAlignedTimestamp();
                try {
                    // sample process data
                    for (ProcessInfo processInfo : xmlRpcService.getAllProcessInfo()) {
                        if (processInfo.getState() == ProcessState.RUNNING) {
                            if (processInfo.getPid() > 0) {
                                OSProcess p = os.getProcess(processInfo.getPid());
                                double cpu = 100d * (p.getKernelTime() + p.getUserTime()) / p.getUpTime();
                                double memory = 100d * (p.getResidentSetSize() / mem.getTotal());
                                long vsz = p.getVirtualSize();
                                long rss = p.getResidentSetSize();
                                processStatistics.add(timestamp, processInfo.getName(), cpu, memory, vsz, rss);
                            }
                        } else {
                            processStatistics.add(timestamp, processInfo.getName(), 0, 0, 0, 0);
                        }
                    }
                } catch (Exception ignore) {}
                try {
                    // sample system data
                    long[] prevTicks = pro.getSystemCpuLoadTicks();
                    Util.sleep(1000);
                    long[] ticks = pro.getSystemCpuLoadTicks();
                    long tuser = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
                    long tnice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
                    long tsys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
                    long tidle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
                    long tiowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
                    long tirq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
                    long tsoftirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
                    long tsteal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
                    long totalCpu = tuser + tnice + tsys + tidle + tiowait + tirq + tsoftirq + tsteal;
                    double[] loads = pro.getSystemLoadAverage(3);

                    double user = 100d * tuser / totalCpu;
                    double nice = 100d * tnice / totalCpu;
                    double system = 100d * tsys / totalCpu;
                    double idle = 100d * tidle / totalCpu;
                    double load1 = loads[0];
                    double load2 = loads[1];
                    double load3 = loads[2];
                    long memory = mem.getAvailable();
                    long swap = mem.getSwapUsed();
                    systemStatistics.add(timestamp, user, nice, system, idle, load1, load2, load3, memory, swap);
                } catch (Exception ignore) {}
            }
        }, 1, 1, TimeUnit.MINUTES);

        // schedule 1 hour average for weeks collection
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    long timestamp = getHourAlignedTimestamp();
                    processStatistics.average(timestamp);
                    systemStatistics.average(timestamp);
                } catch (Exception ignore) {}
            }
        }, 1, 1, TimeUnit.HOURS);

        // schedule 24 hours cleanup task
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000); // just need to avoid clashing with actual sampling of data before doing cleanup
                    List<String> active = new ArrayList<>();
                    for (ProcessInfo processInfo : xmlRpcService.getAllProcessInfo()) {
                        active.add(processInfo.getName());
                    }
                    processStatistics.removeOldEntries(active);
                } catch (Exception ignore) {}
            }
        }, 24, 24, TimeUnit.HOURS);
    }

    private long getMinuteAlignedTimestamp() {
        long timestamp = System.currentTimeMillis();
        return timestamp - (timestamp % 60000);
    }

    private long getHourAlignedTimestamp() {
        long timestamp = System.currentTimeMillis();
        return timestamp - (timestamp % 3600000);
    }

    public ProcessStatistics getProcessStatistics() {
        return processStatistics;
    }

    public SystemStatistics getSystemStatistics() {
        return systemStatistics;
    }

    public SystemInformation getSystemInformation() {
        return systemInformation;
    }
}
