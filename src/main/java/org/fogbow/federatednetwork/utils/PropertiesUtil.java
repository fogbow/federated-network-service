package org.fogbow.federatednetwork.utils;

import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.constants.Messages;
import org.fogbowcloud.ras.core.HomeDir;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesUtil {

    private static Logger LOGGER = Logger.getLogger(PropertiesUtil.class);

    public static Properties readProperties(String fileName) {
        Properties properties = new Properties();
        try {
            FileInputStream input = new FileInputStream(HomeDir.getPath() + fileName);
            properties.load(input);
            return properties;
        } catch (IOException e) {
            LOGGER.fatal(Messages.Fatal.ERROR_LOADING_PROPERTIES, e);
            System.exit(1);
        }
        return properties;
    }
}
