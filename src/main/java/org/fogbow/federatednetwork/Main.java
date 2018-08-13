package org.fogbow.federatednetwork;

import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.controllers.FederatedNetworkController;
import org.fogbow.federatednetwork.utils.PropertiesUtil;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Properties;

import static org.fogbow.federatednetwork.ConfigurationConstants.*;

@Component
public class Main implements ApplicationRunner {


    private static final Logger LOGGER = Logger.getLogger(Main.class);

    private ApplicationFacade applicationFacade = ApplicationFacade.getInstance();

    @Override
    public void run(ApplicationArguments args) {
        Properties properties = PropertiesUtil.readProperties();

        String permissionFilePath = properties.getProperty(FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH);
        String agentUser = properties.getProperty(FEDERATED_NETWORK_AGENT_USER);
        String agentPrivateIp = properties.getProperty(FEDERATED_NETWORK_AGENT_PRIVATE_ADDRESS);
        String agentPublicIp = properties.getProperty(FEDERATED_NETWORK_AGENT_ADDRESS);
        String preSharedKey = properties.getProperty(FEDERATED_NETWORK_PRE_SHARED_KEY);
        String addFederatedNetworkScriptPath = properties.getProperty(ADD_FEDERATED_NETWORK_SCRIPT_PATH);
        String removeFederatedNetworkScriptPath = properties.getProperty(REMOVE_FEDERATED_NETWORK_SCRIPT_PATH);

        FederatedNetworkController federatedNetworkController = new FederatedNetworkController(
                permissionFilePath, agentUser, agentPrivateIp, agentPublicIp, preSharedKey, addFederatedNetworkScriptPath,
                removeFederatedNetworkScriptPath);

        OrderController orderController = new OrderController(properties);

        applicationFacade.setFederatedNetworkController(federatedNetworkController);

        applicationFacade.setOrderController(orderController);
    }
}