package org.fogbow.federatednetwork.utils;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesUtil {

    private static Logger LOGGER = Logger.getLogger(PropertiesUtil.class);

    public static final String FEDERATED_NETWORK_CONF = "federated-network.conf";

    public static Properties readProperties() {
        Properties properties = new Properties();
        try {
            FileInputStream input = new FileInputStream(FEDERATED_NETWORK_CONF);
            properties.load(input);
            return properties;
        } catch (IOException e) {
            LOGGER.error("Error to load properties", e);
            System.exit(1);
        }
        return properties;
    }
}
