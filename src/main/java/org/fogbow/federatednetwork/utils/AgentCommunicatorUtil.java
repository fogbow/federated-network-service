package org.fogbow.federatednetwork.utils;

import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.constants.Messages;

import java.util.Properties;

import static org.fogbow.federatednetwork.constants.ConfigurationPropertiesKeys.*;

public class AgentCommunicatorUtil {

    private static final Logger LOGGER = Logger.getLogger(AgentCommunicatorUtil.class);

    public static boolean createFederatedNetwork(String cidrNotation, String virtualIpAddress, Properties properties) {
        String permissionFilePath = properties.getProperty(FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH);
        String agentUser = properties.getProperty(FEDERATED_NETWORK_AGENT_USER);
        String agentPrivateIp = properties.getProperty(FEDERATED_NETWORK_AGENT_PRIVATE_ADDRESS);
        String agentPublicIp = properties.getProperty(FEDERATED_NETWORK_AGENT_ADDRESS);
        String addFederatedNetworkScriptPath = properties.getProperty(ADD_FEDERATED_NETWORK_SCRIPT_PATH);

        ProcessBuilder builder = new ProcessBuilder("ssh", "-o", "UserKnownHostsFile=/dev/null", "-o",
                "StrictHostKeyChecking=no", "-i", permissionFilePath, agentUser + "@" + agentPublicIp,
                "sudo", addFederatedNetworkScriptPath, agentPrivateIp, agentPublicIp, cidrNotation, virtualIpAddress);
        LOGGER.info("Trying to call agent with atts (" + cidrNotation + "): " + builder.command());

        int resultCode = 0;
        try {
            Process process = builder.start();
            LOGGER.info(String.format(Messages.Error.TRYING_TO_CREATE_AGENT_OUTPUT, cidrNotation,
                    ProcessUtil.getOutput(process)));
            LOGGER.info(String.format(Messages.Error.TRYING_TO_CREATE_AGENT_ERROR, cidrNotation, ProcessUtil.getError(process)));
            resultCode = process.waitFor();
            if (resultCode == 0) {
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        LOGGER.error(String.format(Messages.Error.UNABLE_TO_CALL_AGENT, resultCode));
        return false;
    }

    public static boolean deleteFederatedNetwork(String cidr, Properties properties) {
        String permissionFilePath = properties.getProperty(FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH);
        String agentUser = properties.getProperty(FEDERATED_NETWORK_AGENT_USER);
        String agentPublicIp = properties.getProperty(FEDERATED_NETWORK_AGENT_ADDRESS);
        String removeFederatedNetworkScriptPath = properties.getProperty(REMOVE_FEDERATED_NETWORK_SCRIPT_PATH);

        ProcessBuilder builder = new ProcessBuilder("ssh", "-o", "UserKnownHostsFile=/dev/null", "-o",
                "StrictHostKeyChecking=no", "-i", permissionFilePath, agentUser + "@" + agentPublicIp,
                "sudo", removeFederatedNetworkScriptPath, cidr);
        LOGGER.info("Trying to remove network on agent with atts (" + cidr + "): " + builder.command());

        int resultCode = 0;
        try {
            Process process = builder.start();
            LOGGER.info(String.format(Messages.Error.TRYING_TO_DELETE_AGENT_OUTPUT, cidr,
                    ProcessUtil.getOutput(process)));
            LOGGER.info(String.format(Messages.Error.TRYING_TO_DELETE_AGENT_ERROR, cidr, ProcessUtil.getError(process)));
            resultCode = process.waitFor();
            if (resultCode == 0) {
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        LOGGER.error(String.format(Messages.Error.UNABLE_TO_DELETE_AGENT, resultCode));
        return false;
    }
}
