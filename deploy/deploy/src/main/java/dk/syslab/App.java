package dk.syslab;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.syslab.deploy.Configuration;
import dk.syslab.supv.client.RelativePathBuilder;
import dk.syslab.supv.client.SupvApi;
import dk.syslab.supv.client.SupvClient;
import dk.syslab.supv.client.TransformationsBuilder;
import dk.syslab.supv.dto.Program;
import dk.syslab.supv.dto.distributed.NodeList;
import dk.syslab.supv.dto.distributed.ResultProcessInfo;
import dk.syslab.supv.dto.distributed.ResultStatus;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("Duplicates")
public class App {
    private static Options options;

    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        initializeOptions();

        try {
            // read commandline arguments
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                showUsage();
                return;
            }

            // parse command
            if (cmd.getArgList().size() <= 0) {
                System.out.println("\nERROR: No command provided\n");
                showUsage();
                System.exit(-1);
            }
            String command = cmd.getArgList().get(0);

            // parse config file
            Configuration conf = new Configuration();
            if (cmd.hasOption("c")) {
                Path path = Paths.get(cmd.getOptionValue("c"));
                if (Files.notExists(path)) {
                    System.out.println("\nERROR: Configuration file does not exist");
                    System.exit(-1);
                }
                try {
                    conf = objectMapper.readValue(path.toFile(), Configuration.class);
                } catch (IOException e) {
                    System.out.println("\nERROR: Unable to parse configuration file");
                    System.out.println(e.getMessage());
                    System.exit(-1);
                }
            }

            boolean returnzero = cmd.hasOption("return-zero");

            // parse token
            if (cmd.hasOption("t")) {
                String token = cmd.getOptionValue("t");
                if (token == null || token.isEmpty()) {
                    System.out.println("\nERROR: Invalid token provided");
                    System.exit(-1);
                }
                conf.setToken(token);
            }

            // parse nodes
            if (cmd.hasOption("n")) {
                String nodes = cmd.getOptionValue("n");
                if (nodes == null || nodes.isEmpty()) {
                    System.out.println("\nERROR: Invalid list of nodes provided");
                    System.exit(-1);
                }
                String[] strings = nodes.toLowerCase().split(",");
                conf.setNodes(Arrays.stream(strings).map(String::trim).collect(Collectors.toList()));
            }

            // parse program name
            if (cmd.hasOption("p")) {
                String program = cmd.getOptionValue("p");
                if (program == null || program.isEmpty()) {
                    System.out.println("\nERROR: Invalid program name provided");
                    System.exit(-1);
                }
                conf.setName(program);
            }

            // parse host
            if (cmd.hasOption("a")) {
                String host = cmd.getOptionValue("a");
                if (host == null || host.isEmpty()) {
                    System.out.println("\nERROR: Invalid host address provided");
                    System.exit(-1);
                }
                conf.setHost(host);
            }
            // set default port if someone forgot ;-)
            if (conf.getHost() != null && !conf.getHost().isEmpty()) {
                if (!conf.getHost().contains(":")) conf.setHost(conf.getHost() + ":9080");
            }

            SupvApi api = SupvClient.getApi();

            switch (command) {
                case "upload":
                    int status = upload(conf, api, objectMapper, returnzero);
                    System.exit(status);
                    break;
                case "delete":
                    if (!conf.isValidCommand()) {
                        System.out.println("\nERROR: Final configuration is not valid (check your configuration/options)");
                        System.out.println(conf.getInvalidReasonForCommand());
                        System.exit(-1);
                    }
                    try {
                        Map<String, ResultStatus> result = api.nodes().deleteProgram(conf.getHost(), conf.getNodes(), conf.getName(), conf.getToken());
                        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
                        for (Map.Entry<String, ResultStatus> entry : result.entrySet()) {
                            if (!entry.getValue().isSuccess()) {
                                if (returnzero) System.exit(0);
                                else System.exit(-1);
                            }
                        }
                        System.exit(0);
                    } catch (IOException | URISyntaxException e) {
                        System.out.println(failureAsJson(e));
                        if (returnzero) System.exit(0);
                        else System.exit(-2);
                    }
                    break;
                case "start":
                    status = start(conf, api, objectMapper, returnzero);
                    System.exit(status);
                    break;
                case "stop":
                    status = stop(conf, api, objectMapper, returnzero);
                    System.exit(status);
                    break;
                case "status":
                    if (!conf.isValidCommand()) {
                        System.out.println("\nERROR: Final configuration is not valid (check your configuration/options)");
                        System.out.println(conf.getInvalidReasonForCommand());
                        System.exit(-1);
                    }
                    try {
                        Map<String, ResultProcessInfo> result = api.nodes().getProcessInfo(conf.getHost(), conf.getNodes(), conf.getName(), conf.getToken());
                        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
                        for (Map.Entry<String, ResultProcessInfo> entry : result.entrySet()) {
                            if (!entry.getValue().isSuccess()) {
                                if (returnzero) System.exit(0);
                                else System.exit(-1);
                            }
                        }
                        System.exit(0);
                    } catch (IOException | URISyntaxException e) {
                        System.out.println(failureAsJson(e));
                        if (returnzero) System.exit(0);
                        else System.exit(-2);
                    }
                    break;
                case "send":
                    if (cmd.hasOption("m") && cmd.hasOption("mn")) {
                        System.out.println("\nERROR: -m and -mn should not be specified simultaneously");
                        System.exit(-1);
                    }
                    String message = null;
                    if (cmd.hasOption("m")) {
                        message = cmd.getOptionValue("m");
                    } else if (cmd.hasOption("mn")) {
                        message = cmd.getOptionValue("mn");
                        if (message != null && !message.endsWith("\n")) {
                            message += "\n";
                        }
                    } else {
                        System.out.println("\nERROR: Send command was given, but no message was provided");
                        System.exit(-1);
                    }
                    if (message == null || message.isEmpty()) {
                        System.out.println("\nERROR: Message is invalid");
                        System.exit(-1);
                    }

                    if (!conf.isValidCommand()) {
                        System.out.println("\nERROR: Final configuration is not valid (check your configuration/options)");
                        System.out.println(conf.getInvalidReasonForCommand());
                        System.exit(-1);
                    }
                    try {
                        Map<String, ResultStatus> result = api.nodes().sendMessage(conf.getHost(), conf.getNodes(), conf.getName(), message, conf.getToken());
                        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
                        for (Map.Entry<String, ResultStatus> entry : result.entrySet()) {
                            if (!entry.getValue().isSuccess()) {
                                if (returnzero) System.exit(0);
                                else System.exit(-1);
                            }
                        }
                        System.exit(0);
                    } catch (IOException | URISyntaxException e) {
                        System.out.println(failureAsJson(e));
                        if (returnzero) System.exit(0);
                        else System.exit(-2);
                    }
                    break;
                case "list":
                    if (conf.getHost() == null || conf.getHost().isEmpty()) {
                        System.out.println("\nERROR: Final configuration is not valid (check your configuration/options)");
                        System.out.println("host address not set");
                        System.exit(-1);
                    }
                    try {
                        NodeList result = api.nodes().getNodes(conf.getHost());
                        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
                        System.exit(0);
                    } catch (IOException | URISyntaxException e) {
                        System.out.println(failureAsJson(e));
                        if (returnzero) System.exit(0);
                        else System.exit(-2);
                    }
                    break;
                case "update":
                    if (conf.getHost() == null || conf.getHost().isEmpty() || conf.getNodes() == null || conf.getNodes().size() <= 0 || conf.getToken() == null || conf.getToken().isEmpty()) {
                        System.out.println("\nERROR: Final configuration is not valid (check your configuration/options)");
                        System.out.println("host address or nodes list or token not set");
                        System.exit(-1);
                    }
                    try {
                        Map<String, ResultStatus> result = api.nodes().update(conf.getHost(), conf.getNodes(), conf.getToken());
                        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
                        for (Map.Entry<String, ResultStatus> entry : result.entrySet()) {
                            if (!entry.getValue().isSuccess()) {
                                if (returnzero) System.exit(0);
                                else System.exit(-1);
                            }
                        }
                        System.exit(0);
                    } catch (IOException | URISyntaxException e) {
                        System.out.println(failureAsJson(e));
                        if (returnzero) System.exit(0);
                        else System.exit(-2);
                    }
                    break;
                case "cycle":
                    System.out.println("{\n  \"stop\" :");
                    stop(conf, api, objectMapper, true);
                    System.out.println(", \"upload\" :");
                    status = upload(conf, api, objectMapper, false);
                    if (status != 0) {
                        System.out.println("}");
                        System.exit(status);
                    }
                    System.out.println(", \"start\" :");
                    status = start(conf, api, objectMapper, false);
                    System.out.println("}");
                    System.exit(status);
                    break;
                default:
                    showUsage();
            }


        } catch (ParseException e) {
            System.out.println("\nERROR: Unable to parse commandline arguments\n");
            showUsage();
        }
    }

    private static int start(Configuration conf, SupvApi api, ObjectMapper objectMapper, boolean returnzero) {
        if (!conf.isValidCommand()) {
            System.out.println("\nERROR: Final configuration is not valid (check your configuration/options)");
            System.out.println(conf.getInvalidReasonForCommand());
            System.exit(-1);
        }
        try {
            Map<String, ResultStatus> result = api.nodes().startProgram(conf.getHost(), conf.getNodes(), conf.getName(), true, conf.getToken());
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
            for (Map.Entry<String, ResultStatus> entry : result.entrySet()) {
                if (!entry.getValue().isSuccess()) {
                    if (returnzero) return 0;
                    else return -1;
                }
            }
            return 0;
        } catch (IOException | URISyntaxException e) {
            System.out.println(failureAsJson(e));
            if (returnzero) return 0;
            else return -2;
        }
    }

    private static int stop(Configuration conf, SupvApi api, ObjectMapper objectMapper, boolean returnzero) {
        if (!conf.isValidCommand()) {
            System.out.println("\nERROR: Final configuration is not valid (check your configuration/options)");
            System.out.println(conf.getInvalidReasonForCommand());
            System.exit(-1);
        }
        try {
            Map<String, ResultStatus> result = api.nodes().stopProgram(conf.getHost(), conf.getNodes(), conf.getName(), true, conf.getToken());
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
            for (Map.Entry<String, ResultStatus> entry : result.entrySet()) {
                if (!entry.getValue().isSuccess()) {
                    if (returnzero) return 0;
                    else return -1;
                }
            }
            return 0;
        } catch (IOException | URISyntaxException e) {
            System.out.println(failureAsJson(e));
            if (returnzero) return 0;
            else return -2;
        }
    }

    private static int upload(Configuration conf, SupvApi api, ObjectMapper objectMapper, boolean returnzero) {
        if (!conf.isValidUpload()) {
            System.out.println("\nERROR: Final configuration is not valid (check your configuration/options)");
            System.out.println(conf.getInvalidReasonForUpload());
            System.out.println(conf.getInvalidReasonForCommand());
            System.exit(-1);
        }
        try {
            RelativePathBuilder builder = RelativePathBuilder.create();
            List<File> files = new ArrayList<>();
            if (conf.getFiles() != null) {
                for (String f : conf.getFiles()) {
                    files.add(Paths.get(f).toFile());
                }
            }
            TransformationsBuilder tbuilder = TransformationsBuilder.create();
            if (conf.getTransforms() != null) {
                tbuilder.add(conf.getTransforms());
            }
            if (conf.getPaths() != null) {
                for (Map.Entry<String, String> entry : conf.getPaths().entrySet()) {
                    builder.add(entry.getKey(), entry.getValue());
                }

            }
            if (conf.getFolders() != null) {
                for (Map.Entry<String, String> entry : conf.getFolders().entrySet()) {
                    if (entry.getKey() != null && !entry.getKey().isEmpty() && entry.getValue() != null) {
                        builder.setProgramDir(entry.getKey());
                        for (File f : FileUtils.listFiles(Paths.get(entry.getValue()).toFile(), null, true)) {
                            files.add(f);
                            builder.add(f);
                        }
                    }
                }
            }
            Program p = new Program();
            p.setName(conf.getName());
            p.setCommand(conf.getCommand());
            p.setAutorestart(conf.getAutorestart());
            p.setAutostart(conf.getAutostart());
            p.setEnvironment(conf.getEnvironment());
            p.setExitcodes(conf.getExitcodes());
            p.setPriority(conf.getPriority());
            p.setStartretries(conf.getStartretries());
            p.setStartsecs(conf.getStartsecs());
            p.setStopwaitsecs(conf.getStopwaitsecs());
            Map<String, ResultStatus> result = api.nodes().uploadProgram(conf.getHost(), conf.getNodes(), p, files, builder.build(), tbuilder.build(), conf.getToken());
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
            for (Map.Entry<String, ResultStatus> entry : result.entrySet()) {
                if (!entry.getValue().isSuccess()) {
                    if (returnzero) return 0;
                    else return -1;
                }
            }
            return 0;
        } catch (IOException | URISyntaxException e) {
            System.out.println(failureAsJson(e));
            if (returnzero) return 0;
            else return -2;
        }
    }

    private static void initializeOptions() {
        options = new Options();
        options.addOption(Option.builder("c").longOpt("config").hasArg().argName("filename").desc("configuration file to use").build());
        options.addOption(Option.builder("t").longOpt("token").hasArg().argName("token string").desc("JWT authentication token").build());
        options.addOption(Option.builder("n").longOpt("nodes").hasArg().argName("nodes string").desc("list of nodes in a comma delimited string").build());
        options.addOption(Option.builder("p").longOpt("program").hasArg().argName("program name").desc("the name of the program").build());
        options.addOption(Option.builder("a").longOpt("address").hasArg().argName("host address").desc("the deployment host to utilize").build());
        options.addOption(Option.builder("m").longOpt("message").hasArg().argName("message string").desc("send this <message> to program when command is 'send'").build());
        options.addOption(Option.builder("mn").longOpt("message").hasArg().argName("message string").desc("send <message> with newline appended at the end (simulate 'enter' click)").build());
        options.addOption(Option.builder().longOpt("return-zero").desc("always exit application with 0 status code").build());
        options.addOption("h", "help", false, "print this help");
    }

    private static void showUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(null);
        formatter.setWidth(formatter.getWidth() * 2);
        formatter.printHelp("java -jar deploy.jar [options] <command>", "\noptions:", options, "");
        System.out.println("\ncommands:");
        System.out.println(" upload                           upload program to nodes");
        System.out.println(" delete                           delete program from nodes");
        System.out.println(" update                           update configuration on nodes");
        System.out.println(" start                            start program on nodes");
        System.out.println(" stop                             stop  program on nodes");
        System.out.println(" status                           get information about program from nodes");
        System.out.println(" send                             send a message to program stdin on nodes");
        System.out.println(" list                             get a list of nodes known to the host");
        System.out.println(" cycle                            do sequence: stop, upload and start");

        System.out.println("\nAll options can and should be defined in a config file. By mixing config files");
        System.out.println("with options, it is e.g. possible to run a configuration against other nodes.");
        System.out.println("The token can be defined in the configuration or given on the command line");
        System.out.println("and as such it is possible to run commands without a config file just using");
        System.out.println("options, except 'upload' (because a run-command is required).");
        System.out.println("\nDepending on your situation one or the other may be more secure. I.e. providing");
        System.out.println("the token on the command line ensures that no token is committed to GIT, but");
        System.out.println("using the commandline on a shared computer exposes the token in history.");
        System.out.println("Whatever you do, DOT NOT commit a token to version control, you will be killed!");
    }

    private static String failureAsJson(Exception e) {
        return "{\n" + "\t\"success\":false,\n\t\"message\":\"" + e.getMessage() + "\"\n}";
    }
}
