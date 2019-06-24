package dk.syslab.controller.rpc;

import dk.syslab.controller.broadcast.BroadcastService;
import dk.syslab.controller.broadcast.Node;
import dk.syslab.controller.rpc.protobuf.BroadcastRpcGrpc;
import dk.syslab.controller.rpc.protobuf.Messages;
import dk.syslab.controller.validation.ValidationService;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentMap;

public class BroadcastRpc extends BroadcastRpcGrpc.BroadcastRpcImplBase {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private BroadcastService broadcastService;

    public BroadcastRpc(BroadcastService broadcastService) {
        this.broadcastService = broadcastService;
    }

    @Override
    public void getNodeMap(Messages.Token request, StreamObserver<Messages.NodeMap> responseObserver) {
        try {
            ConcurrentMap<String, Node> map = broadcastService.getNodeMap();
            Messages.NodeMap.Builder builder = Messages.NodeMap.newBuilder();
            for (Map.Entry<String, Node> entry : map.entrySet()) {
                Node n = entry.getValue();
                Messages.Node build = Messages.Node.newBuilder()
                    .setName(n.getName())
                    .setAddress(n.getAddress())
                    .setStatuscode(n.getStatuscode())
                    .setRunning(n.getRunning())
                    .setTotal(n.getTotal())
                    .setTimestamp(n.getTimestamp())
                    .build();
                builder.putNodeMap(n.getName(), build);
            }
            responseObserver.onNext(builder.build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getSortedNodes(Messages.Token request, StreamObserver<Messages.SortedNodes> responseObserver) {
        try {
            SortedSet<Node> list = broadcastService.getSortedNodes();
            Messages.SortedNodes.Builder builder = Messages.SortedNodes.newBuilder();
            for (Node n : list) {
                Messages.Node build = Messages.Node.newBuilder()
                    .setName(n.getName())
                    .setAddress(n.getAddress())
                    .setStatuscode(n.getStatuscode())
                    .setRunning(n.getRunning())
                    .setTotal(n.getTotal())
                    .setTimestamp(n.getTimestamp())
                    .build();
                builder.addNode(build);
            }
            responseObserver.onNext(builder.build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getNodeStatistics(Messages.Token request, StreamObserver<Messages.NodeStatistics> responseObserver) {
        try {
            Map<String, Integer> map = broadcastService.getNodeStatistics();
            responseObserver.onNext(Messages.NodeStatistics.newBuilder()
                .setSupervisorsRunning(map.get("supervisors-running"))
                .setSupervisorsTotal(map.get("supervisors-total"))
                .setProcessesRunning(map.get("processes-running"))
                .setProcessesTotal(map.get("processes-total"))
                .build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getSelf(Messages.Token request, StreamObserver<Messages.Node> responseObserver) {
        try {
            Node self = broadcastService.getSelf();
            responseObserver.onNext(Messages.Node.newBuilder()
                .setName(self.getName())
                .setAddress(self.getAddress())
                .setStatuscode(self.getStatuscode())
                .setRunning(self.getRunning())
                .setTotal(self.getTotal())
                .setTimestamp(self.getTimestamp())
                .build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getNodeList(Messages.Token request, StreamObserver<Messages.NodeListResult> responseObserver) {
        try {
            SortedSet<Node> list = broadcastService.getSortedNodes();
            ConcurrentMap<String, Node> map = broadcastService.getNodeMap();
            Node self = broadcastService.getSelf();
            Map<String, Integer> stats = broadcastService.getNodeStatistics();

            Messages.NodeListResult.Builder builder = Messages.NodeListResult.newBuilder().setSuccess(true).setCode(Messages.ResultCode.OK);

            for (Node node : list) {
                builder.addNode(node.getName());
            }

            for (Map.Entry<String, Node> entry : map.entrySet()) {
                builder.putAddress(entry.getKey(), entry.getValue().getAddress());
            }

            Messages.Node selfNode = Messages.Node.newBuilder()
                .setName(self.getName())
                .setAddress(self.getAddress())
                .setStatuscode(self.getStatuscode())
                .setRunning(self.getRunning())
                .setTotal(self.getTotal())
                .setTimestamp(self.getTimestamp())
                .build();
            builder.setSelf(selfNode);

            Messages.NodeStatistics nodeStatistics = Messages.NodeStatistics.newBuilder()
                .setSupervisorsRunning(stats.get("supervisors-running"))
                .setSupervisorsTotal(stats.get("supervisors-total"))
                .setProcessesRunning(stats.get("processes-running"))
                .setProcessesTotal(stats.get("processes-total"))
                .build();
            builder.setStatistics(nodeStatistics);

            responseObserver.onNext(builder.build());
        } finally {
            responseObserver.onCompleted();
        }
    }
}
