package dk.syslab.controller.rpc;

import dk.syslab.controller.Configuration;
import dk.syslab.controller.broadcast.BroadcastService;
import dk.syslab.controller.statistics.StatisticsService;
import dk.syslab.controller.storage.FileService;
import dk.syslab.controller.validation.ValidationService;
import dk.syslab.controller.xmlrpc.XmlRpcService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.IOException;

public class RpcService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final Server server;

    public RpcService(Configuration configuration, ValidationService validationService, XmlRpcService xmlRpcService, FileService fileService, StatisticsService statisticsService, BroadcastService broadcastService, RpcChannelService channelService) {
        ServerBuilder<?> serverBuilder = ServerBuilder.forPort(Integer.parseInt(configuration.getRequiredProperty("grpc.server")));
        this.server = serverBuilder
            .addService(new XmlRpc(validationService, xmlRpcService))
            .addService(new FileRpc(validationService, fileService))
            .addService(new StatisticsRpc(statisticsService))
            .addService(new BroadcastRpc(broadcastService))
            .addService(new DistributedRpc(validationService, fileService, broadcastService, channelService, xmlRpcService))
            .build();
    }

    public void start() throws IOException {
        server.start();
        log.info("RPC Server started, listening on port " + server.getPort());
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));
    }

    // Needed to support Spring Boot DevTools reload/restart functionality
    // DevTools does not fully shutdown the application on restart but does a classloader reload
    // which means that the socket is not released, this releases the socket.
    @PreDestroy
    public void destroy() {
        server.shutdownNow();
    }
}
