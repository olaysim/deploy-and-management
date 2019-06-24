package dk.syslab.controller;

import dk.syslab.controller.broadcast.BroadcastService;
import dk.syslab.controller.rpc.RpcChannelService;
import dk.syslab.controller.storage.CleanUpService;
import dk.syslab.controller.validation.JwtService;
import dk.syslab.controller.rpc.RpcService;
import dk.syslab.controller.validation.ValidationService;
import dk.syslab.controller.statistics.StatisticsService;
import dk.syslab.controller.storage.FileService;
import dk.syslab.controller.xmlrpc.XmlRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static XmlRpcService xmlRpcService;
    private static FileService fileService;
    private static StatisticsService statisticsService;
    private static JwtService jwtService;
    private static Configuration configuration;
    private static ValidationService validationService;
    private static BroadcastService broadcastService;
    private static CleanUpService cleanUpService;
    private static RpcChannelService channelService;

    public static void main(String[] args) throws Exception {
        log.info("Starting Supervisor Controller");
        // start rpc server
        RpcService rpcService = new RpcService(getConfiguration(), getValidationService(), getXmlRpcService(), getFileService(), getStatisticsService(), getBroadcastService(), getChannelService());
        rpcService.start();
        // start clean up service
        getCleanUpService().start();
        //noinspection InfiniteLoopStatement
        do Thread.sleep(100000);
        while (true);
    }


    // create services as singletons (this is very manual dependency injection, but what won't you do to avoid loading too many libraries)
    // these methods/beans/singletons can be mocked during testing
    public static XmlRpcService getXmlRpcService() {
        if (xmlRpcService == null) {
            try {
                xmlRpcService = new XmlRpcService(getConfiguration());
            } catch (MalformedURLException e) {
                log.error("Unable to satisfy dependency for XML RPC Service", e);
                System.exit(-3);
            }
        }
        return xmlRpcService;
    }

    public static Configuration getConfiguration() {
        if (configuration == null) configuration = new Configuration();
        return configuration;
    }

    public static FileService getFileService() {
        if (fileService == null) fileService = new FileService(getValidationService(), getBroadcastService(), getConfiguration());
        return fileService;
    }

    public static StatisticsService getStatisticsService() {
        if (statisticsService == null) statisticsService = new StatisticsService(getXmlRpcService());
        return statisticsService;
    }

    public static ValidationService getValidationService() {
        if (validationService == null) validationService = new ValidationService(getJwtService());
        return validationService;
    }

    public static BroadcastService getBroadcastService() {
        if (broadcastService == null) broadcastService = new BroadcastService(getXmlRpcService());
        return broadcastService;
    }

    public static JwtService getJwtService() {
        if (jwtService == null) {
            try {
                jwtService = new JwtService(getConfiguration());
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                log.error("Unable to satisfy dependency for JWT Service", e);
                System.exit(-3);
            }
        }
        return jwtService;
    }

    public static CleanUpService getCleanUpService() {
        if (cleanUpService == null) cleanUpService = new CleanUpService();
        return cleanUpService;
    }

    public static RpcChannelService getChannelService() {
        if (channelService == null) channelService = new RpcChannelService(getConfiguration());
        return channelService;
    }
}
