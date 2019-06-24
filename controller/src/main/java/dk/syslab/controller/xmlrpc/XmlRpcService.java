package dk.syslab.controller.xmlrpc;

import dk.syslab.controller.Configuration;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.client.AsyncCallback;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class XmlRpcService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private XmlRpcClient client;
    private SupervisorInfo info;
    private ScheduledExecutorService scheduledExecutorService;

    public XmlRpcService(Configuration configuration) throws MalformedURLException {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setBasicUserName(configuration.getRequiredProperty("xmlrpc.username"));
        config.setBasicPassword(configuration.getRequiredProperty("xmlrpc.password"));
        config.setServerURL(new URL("http", "localhost", Integer.parseInt(configuration.getRequiredProperty("xmlrpc.port")), configuration.getRequiredProperty("xmlrpc.file")));
        client = new XmlRpcClient();
        client.setConfig(config);

        info = new SupervisorInfo();
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleWithFixedDelay(this::updateState, 20, 20, TimeUnit.SECONDS);
    }

    public void updateState() { // this method should not really be "public", but it is hard to mock otherwise :-/
        try {
            updateSupervisorInfo();
        } catch (XmlRpcException ignored) {}
//        updateProcessInfo();
    }

//    private void updateProcessInfo() {
//        try {
//            processes = getAllProcessInfo();
//        } catch (XmlRpcException e) {
//            log.error("Unable to update list of all processes info", e);
//        }
//    }

    private void updateSupervisorInfo() throws XmlRpcException {
        String[] nop = new String[]{};

        client.executeAsync("supervisor.getAPIVersion", nop, new AsyncCallback() {
            @Override
            public void handleResult(XmlRpcRequest pRequest, Object pResult) {
                info.setApiVersion((String)pResult);
            }
            @Override
            public void handleError(XmlRpcRequest pRequest, Throwable pError) {
//                log.error("Unable to get API Version", pError);
//                log.error("Unable to get API Version");
                log.error("Unable to connect to local Supervisor instance"); // just print this one log line (if one fails, the other fails as well, but don't spam log)
            }
        });

        client.executeAsync("supervisor.getSupervisorVersion", nop, new AsyncCallback() {
            @Override
            public void handleResult(XmlRpcRequest pRequest, Object pResult) {
                info.setPackageVersion((String)pResult);
            }
            @Override
            public void handleError(XmlRpcRequest pRequest, Throwable pError) {
//                log.error("Unable to get Supervisor Version", pError);
//                log.error("Unable to get Supervisor Version");
            }
        });

        client.executeAsync("supervisor.getIdentification", nop, new AsyncCallback() {
            @Override
            public void handleResult(XmlRpcRequest pRequest, Object pResult) {
                info.setIdentifier((String)pResult);
            }
            @Override
            public void handleError(XmlRpcRequest pRequest, Throwable pError) {
//                log.error("Unable to get Identification Version", pError);
//                log.error("Unable to get Identification Version");
            }
        });

        client.executeAsync("supervisor.getPID", nop, new AsyncCallback() {
            @Override
            public void handleResult(XmlRpcRequest pRequest, Object pResult) {
                info.setPid((Integer)pResult);
            }
            @Override
            public void handleError(XmlRpcRequest pRequest, Throwable pError) {
//                log.error("Unable to get PID", pError);
//                log.error("Unable to get PID");
            }
        });

        client.executeAsync("supervisor.getState", nop, new AsyncCallback() {
            @Override
            public void handleResult(XmlRpcRequest pRequest, Object pResult) {
                HashMap map = (HashMap) pResult;
                info.setState((String)map.get("statename"));
                info.setCode((Integer)map.get(("statecode")));
            }
            @Override
            public void handleError(XmlRpcRequest pRequest, Throwable pError) {
//                log.error("Unable to get Supervisor Version", pError);
//                log.error("Unable to get Supervisor Version");
            }
        });
    }

    /**
     * Clear the main log
     * @return always returns True unless error
     * @throws XmlRpcException
     */
    public boolean clearLog() throws XmlRpcException {
        return (boolean)client.execute("supervisor.clearLog", new String[]{});
    }

    /**
     * Shut down the supervisor process
     * @return always returns True unless error
     * @throws XmlRpcException
     */
    public boolean shutdown() throws XmlRpcException {
        return (boolean)client.execute("supervisor.shutdown", new String[]{});
    }

    /**
     * Restart the supervisor process
     * @return always return True unless error
     * @throws XmlRpcException
     */
    public boolean restart() throws XmlRpcException {
        return (boolean)client.execute("supervisor.restart", new String[]{});
    }

    /**
     * Reload the configuration (reread) does not actually restart nor refresh any processes or process groups<br />
     * (you need to implement an update functionality yourself: https://github.com/Supervisor/supervisor/blob/3.0a10/src/supervisor/supervisorctl.py#L923-L961)
     * @return The result contains three arrays containing names of process groups: [[added, changed, removed]]
     * @throws XmlRpcException
     */
    public Map<String, List<String>> reloadConfig() throws XmlRpcException {
        Map<String, List<String>> map = new Hashtable<>();
        List<String> added = new ArrayList<>();
        List<String> changed = new ArrayList<>();
        List<String> removed = new ArrayList<>();
        map.put("added", added);
        map.put("changed", changed);
        map.put("removed", removed);
        Object[] arr = (Object[])client.execute("supervisor.reloadConfig", new String[]{});
        arr = (Object[])arr[0];
        for (Object obj : (Object[])arr[0]) {
            added.add((String)obj);
        }
        for (Object obj : (Object[])arr[1]) {
            changed.add((String)obj);
        }
        for (Object obj : (Object[])arr[2]) {
            removed.add((String)obj);
        }
        return map;
    }

    /**
     * Read length bytes from the main log starting at offset<br/>
     * It can either return the entire log, a number of characters from the tail of the log, or a slice of the log specified by the offset and length parameters:<br/>
     * @see <a href="http://supervisord.org/api.html#supervisor.rpcinterface.SupervisorNamespaceRPCInterface.readLog">http://supervisord.org/api.html#supervisor.rpcinterface.SupervisorNamespaceRPCInterface.readLog</a>
     */
    public String readLog(int offset, int length) throws XmlRpcException {
        return (String)client.execute("supervisor.readLog", new Integer[]{offset, length});
    }

    /**
     * Get info about a process named name
     * @param name The name of the process (or ‘group:name’)
     * @return A structure containing data about the process
     * @throws XmlRpcException
     */
    public ProcessInfo getProcessInfo(String name) throws XmlRpcException {
        HashMap map = (HashMap)client.execute("supervisor.getProcessInfo", new String[]{name});
        return parseProcessInfo(map);
    }

    private ProcessInfo parseProcessInfo(HashMap map) {
        ProcessInfo info = new ProcessInfo();
        info.setName((String)map.get("name"));
        info.setGroup((String)map.get("group"));
        info.setDescription((String)map.get("description"));
        info.setStart((Integer)map.get("start"));
        info.setStop((Integer)map.get("stop"));
        info.setNow((Integer)map.get("now"));
        info.setStatename((String)map.get("statename"));
        info.setState((Integer)map.get("state"));
        info.setSpawnerr((String)map.get("spawnerr"));
        info.setExitstatus((Integer)map.get("exitstatus"));
        info.setLogfile((String)map.get("logfile"));
        info.setStdOutLogfile((String)map.get("stdout_logfile"));
        info.setStdErrLogfile((String)map.get("stderr_logfile"));
        info.setPid((Integer)map.get("pid"));
        return info;
    }

    /**
     * Get info about all processes
     * @return An array of process status results
     * @throws XmlRpcException
     */
    public List<ProcessInfo> getAllProcessInfo() throws XmlRpcException {
        List<ProcessInfo> list = new ArrayList<>();
        Object[] arr = (Object[])client.execute("supervisor.getAllProcessInfo", new String[]{});
        for (Object obj : arr) {
            HashMap map = (HashMap)obj;
            ProcessInfo info = parseProcessInfo(map);
            list.add(info);
        }
        return list;
    }

    /**
     * Start a process
     * @param name Process name (or group:name, or group:*)
     * @param wait Wait for process to be fully started
     * @return Always true unless error
     * @throws XmlRpcException
     */
    public boolean startProcess(String name, boolean wait) throws XmlRpcException {
        return (boolean)client.execute("supervisor.startProcess", new Object[]{name, wait});
    }

    /**
     * Start all processes listed in the configuration file
     * @param wait Wait for each process to be fully started
     * @return An array of process status info structs
     * @throws XmlRpcException
     */
    public List<ProcessStatus> startAllProcesses(boolean wait) throws XmlRpcException {
        List<ProcessStatus> list = new ArrayList<>();
        Object[] arr = (Object[])client.execute("supervisor.startAllProcesses", new Boolean[]{wait});
        for (Object obj : arr) {
            HashMap map = (HashMap)obj;
            ProcessStatus status = parseProcessStatus(map);
            list.add(status);
        }
        return list;
    }

    private ProcessStatus parseProcessStatus(HashMap map) {
        ProcessStatus status = new ProcessStatus();
        status.setName((String)map.get("name"));
        status.setDescription((String)map.get("description"));
        status.setStatus((Integer)map.get("status"));
        status.setGroup((String)map.get("group"));
        return status;
    }

    /**
     * Start all processes in the group named ‘name’
     * @param name The group name
     * @param wait Wait for each process to be fully started
     * @return An array of process status info structs
     * @throws XmlRpcException
     */
    public List<ProcessStatus> startProcessGroup(String name, boolean wait) throws XmlRpcException {
        List<ProcessStatus> list = new ArrayList<>();
        Object[] arr = (Object[])client.execute("supervisor.startProcessGroup", new Object[]{name, wait});
        for (Object obj : arr) {
            HashMap map = (HashMap)obj;
            ProcessStatus status = parseProcessStatus(map);
            list.add(status);
        }
        return list;
    }

    /**
     * Stop a process named by name
     * @param name The name of the process to stop (or ‘group:name’)
     * @param wait Wait for the process to be fully stopped
     * @return Always return True unless error
     * @throws XmlRpcException
     */
    public boolean stopProcess(String name, boolean wait) throws XmlRpcException {
        return (boolean)client.execute("supervisor.stopProcess", new Object[]{name, wait});
    }

    /**
     * Stop all processes in the process group named ‘name’
     * @param name The group name
     * @param wait Wait for each process to be fully stopped
     * @return An array of process status info structs
     * @throws XmlRpcException
     */
    public List<ProcessStatus> stopProcessGroup(String name, boolean wait) throws XmlRpcException {
        List<ProcessStatus> list = new ArrayList<>();
        Object[] arr = (Object[])client.execute("supervisor.stopProcessGroup", new Object[]{name, wait});
        for (Object obj : arr) {
            HashMap map = (HashMap)obj;
            ProcessStatus status = parseProcessStatus(map);
            list.add(status);
        }
        return list;
    }

    /**
     * Stop all processes in the process list
     * @param wait Wait for each process to be fully stopped
     * @return An array of process status info structs
     * @throws XmlRpcException
     */
    public List<ProcessStatus> stopAllProcesses(boolean wait) throws XmlRpcException {
        List<ProcessStatus> list = new ArrayList<>();
        Object[] arr = (Object[])client.execute("supervisor.stopAllProcesses", new Boolean[]{wait});
        for (Object obj : arr) {
            HashMap map = (HashMap)obj;
            ProcessStatus status = parseProcessStatus(map);
            list.add(status);
        }
        return list;
    }

    /**
     * Send an arbitrary UNIX signal to the process named by name
     * @param name Name of the process to signal (or ‘group:name’)
     * @param signal Signal to send, as name (‘HUP’) or number (‘1’)
     * @return boolean
     * @throws XmlRpcException
     */
    public boolean signalProcess(String name, String signal) throws XmlRpcException {
        return (boolean)client.execute("supervisor.signalProcess", new String[]{name, signal});
    }

    /**
     * Send a signal to all processes in the group named ‘name’
     * @param name The group name
     * @param signal Signal to send, as name (‘HUP’) or number (‘1’)
     * @return An array of process status info structs
     * @throws XmlRpcException
     */
    public List<ProcessStatus> signalProcessGroup(String name, String signal) throws XmlRpcException {
        List<ProcessStatus> list = new ArrayList<>();
        Object[] arr = (Object[])client.execute("supervisor.signalProcessGroup", new String[]{name, signal});
        for (Object obj : arr) {
            HashMap map = (HashMap)obj;
            ProcessStatus status = parseProcessStatus(map);
            list.add(status);
        }
        return list;
    }

    /**
     * Send a signal to all processes in the process list
     * @param signal Signal to send, as name (‘HUP’) or number (‘1’)
     * @return An array of process status info structs
     * @throws XmlRpcException
     */
    public List<ProcessStatus> signalAllProcesses(String signal) throws XmlRpcException {
        List<ProcessStatus> list = new ArrayList<>();
        Object[] arr = (Object[])client.execute("supervisor.signalAllProcesses", new String[]{signal});
        for (Object obj : arr) {
            HashMap map = (HashMap)obj;
            ProcessStatus status = parseProcessStatus(map);
            list.add(status);
        }
        return list;
    }

    /**
     * Send a string of chars to the stdin of the process name. If non-7-bit data is sent (unicode), it is encoded to utf-8 before being sent to the process’ stdin
     * @param name The process name to send to (or ‘group:name’)
     * @param chars The character data to send to the process
     * @return Always return True unless error
     * @throws XmlRpcException
     */
    public boolean sendProcessStdin(String name, String chars) throws XmlRpcException {
        return (boolean)client.execute("supervisor.sendProcessStdin", new String[]{name, chars});
    }

    /**
     * Send an event that will be received by event listener subprocesses subscribing to the RemoteCommunicationEvent.
     * @param type String for the “type” key in the event header
     * @param data Data for the event body
     * @return Always return True unless error
     * @throws XmlRpcException
     */
    public boolean sendRemoteCommEvent(String type, String data) throws XmlRpcException {
        return (boolean)client.execute("supervisor.sendRemoteCommEvent", new String[]{type, data});
    }

    /**
     * Update the config for a running process from config file.
     * @param name name of process group to add
     * @return true if successful
     * @throws XmlRpcException
     */
    public boolean addProcessGroup(String name) throws XmlRpcException {
        return (boolean)client.execute("supervisor.addProcessGroup", new String[]{name});
    }

    /**
     * Remove a stopped process from the active configuration.
     * @param name name of process group to remove
     * @return Indicates whether the removal was successful
     * @throws XmlRpcException
     */
    public boolean removeProcessGroup(String name) throws XmlRpcException {
        return (boolean)client.execute("supervisor.removeProcessGroup", new String[]{name});
    }

    /**
     * Read length bytes from name’s stdout log starting at offset
     * @param name the name of the process (or ‘group:name’)
     * @param offset offset to start reading from
     * @param length number of bytes to read from the log
     * @return Bytes of log
     * @throws XmlRpcException
     */
    public String readProcessStdoutLog(String name, int offset, int length) throws XmlRpcException {
        return (String)client.execute("supervisor.readProcessStdoutLog", new Object[]{name, offset, length});
    }

    /**
     * Read length bytes from name’s stderr log starting at offset
     * @param name the name of the process (or ‘group:name’)
     * @param offset offset to start reading from
     * @param length number of bytes to read from the log
     * @return Bytes of log
     * @throws XmlRpcException
     */
    public String readProcessStderrLog(String name, int offset, int length) throws XmlRpcException {
        return (String)client.execute("supervisor.readProcessStderrLog", new Object[]{name, offset, length});
    }

    /**
     * Provides a more efficient way to tail the (stdout) log than readProcessStdoutLog(). Use readProcessStdoutLog() to read chunks and tailProcessStdoutLog() to tail.<br/>
     * Requests (length) bytes from the (name)’s log, starting at (offset). If the total log size is greater than (offset + length), the overflow flag is set and the (offset) is automatically increased to position the buffer at the end of the log. If less than (length) bytes are available, the maximum number of available bytes will be returned. (offset) returned is always the last offset in the log +1.
     * @param name the name of the process (or ‘group:name’)
     * @param offset offset to start reading from
     * @param length maximum number of bytes to return
     * @return [string bytes, int offset, bool overflow]
     * @throws XmlRpcException
     */
    public TailLog tailProcessStdoutLog(String name, int offset, int length) throws XmlRpcException {
        TailLog log = new TailLog();
        Object[] arr = (Object[])client.execute("supervisor.tailProcessStdoutLog", new Object[]{name, offset, length});
        log.setLog((String)arr[0]);
        log.setOffset((Integer)arr[1]);
        log.setOverflow((Boolean)arr[2]);
        return log;
    }

    /**
     * Provides a more efficient way to tail the (stderr) log than readProcessStderrLog(). Use readProcessStderrLog() to read chunks and tailProcessStderrLog() to tail.<br/>
     * Requests (length) bytes from the (name)’s log, starting at (offset). If the total log size is greater than (offset + length), the overflow flag is set and the (offset) is automatically increased to position the buffer at the end of the log. If less than (length) bytes are available, the maximum number of available bytes will be returned. (offset) returned is always the last offset in the log +1.
     * @param name the name of the process (or ‘group:name’)
     * @param offset offset to start reading from
     * @param length maximum number of bytes to return
     * @return [string bytes, int offset, bool overflow]
     * @throws XmlRpcException
     */
    public TailLog tailProcessStderrLog(String name, int offset, int length) throws XmlRpcException {
        TailLog log = new TailLog();
        Object[] arr = (Object[])client.execute("supervisor.tailProcessStderrLog", new Object[]{name, offset, length});
        log.setLog((String)arr[0]);
        log.setOffset((Integer)arr[1]);
        log.setOverflow((Boolean)arr[2]);
        return log;
    }

    /**
     * Clear the stdout and stderr logs for the named process and reopen them.
     * @param name The name of the process (or ‘group:name’)
     * @return Always True unless error
     * @throws XmlRpcException
     */
    public boolean clearProcessLogs(String name) throws XmlRpcException {
        return (boolean)client.execute("supervisor.clearProcessLogs", new String[]{name});
    }

    /**
     * Clear all process log files
     * @return  An array of process status info structs
     * @throws XmlRpcException
     */
    public List<ProcessStatus> clearAllProcessLogs() throws XmlRpcException {
        List<ProcessStatus> list = new ArrayList<>();
        Object[] arr = (Object[])client.execute("supervisor.clearAllProcessLogs", new String[]{});
        for (Object obj : arr) {
            HashMap map = (HashMap)obj;
            ProcessStatus status = parseProcessStatus(map);
            list.add(status);
        }
        return list;
    }

    public boolean update() throws XmlRpcException {
        Map<String, List<String>> changes = reloadConfig();
        for (String removed : changes.get("removed")) {
            try {
                stopProcessGroup(removed, true);
            } catch (XmlRpcException e) {
                // ignore
            }
            removeProcessGroup(removed);
        }

        for (String changed : changes.get("changed")) {
            try {
                stopProcessGroup(changed, true);
            } catch (XmlRpcException e) {
                // ignore
            }
            removeProcessGroup(changed);
            addProcessGroup(changed);
        }

        for (String added : changes.get("added")) {
            addProcessGroup(added);
        }

        return true;
    }


    /**
     * Returns the current Supervirsor Info
     * @return SupervisorInfo
     */
    public SupervisorInfo currentSupervisorInfo() {
        return info;
    }


//    /**
//     * Returns the current process info for all processes
//     * @return
//     */
//    public List<ProcessInfo> currentProcessesInfo() {
//        return processes;
//    }
}
