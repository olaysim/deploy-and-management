package dk.syslab.controller.storage;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CleanUpService implements Runnable {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private ScheduledExecutorService executor;

    public CleanUpService() {
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        executor.scheduleWithFixedDelay(this, 2, 2, TimeUnit.DAYS);
    }

    @Override
    public void run() {
        try {
            Path temp = Files.createTempDirectory("locateSupvTempDir");
            for (File file : temp.getParent().toFile().listFiles()) {
                if ((file.getName().toLowerCase().startsWith("supv") || file.getName().toLowerCase().startsWith("locateSupvTempDir")) && file.exists() && file.isDirectory()) {
                    try {
                        FileUtils.deleteDirectory(file);
                    } catch (Exception ex) {
                        log.warn("cleanup service failed to delete directory", ex);
                    }
                }
            }
        } catch (Exception e) {
            log.error("clean up failed", e);
        }
    }
}
