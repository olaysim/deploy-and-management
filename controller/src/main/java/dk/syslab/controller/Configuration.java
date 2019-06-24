package dk.syslab.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Configuration {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private Properties configuration;

    public Configuration() {
        configuration = new Properties();
        try {
            configuration.load(getClass().getResourceAsStream("/application.properties"));
        } catch (IOException e) {
            log.error("Failed to read configuration from resources", e);
            System.exit(-1);
        }
        Path customProperties = Paths.get("application.properties");
        if (Files.exists(customProperties)) {
            try {
                Properties userProperties = new Properties();
                userProperties.load(new FileInputStream(customProperties.toFile()));
                configuration.putAll(userProperties);
            } catch (IOException e) {
                log.debug("User defined properties not found!");
            }
        }
    }

    public Properties getConfiguration() {
        return configuration;
    }

    public String getRequiredProperty(String path) {
        String result = configuration.getProperty(path);
        if (result == null) {
            log.error("Property REQUIRED by application not found: " + path);
            System.exit(-2);
        }
        return result;
    }

    public String getProperty(String path) {
        return configuration.getProperty(path);
    }

    public String getProperty(String path, String defaultValue) {
        return configuration.getProperty(path, defaultValue);
    }

}
