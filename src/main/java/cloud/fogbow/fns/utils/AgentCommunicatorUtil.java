package cloud.fogbow.fns.utils;

import org.apache.log4j.Logger;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.common.util.ProcessUtil;
import cloud.fogbow.fns.core.constants.Messages;

import static cloud.fogbow.fns.core.constants.ConfigurationConstants.*;

public class AgentCommunicatorUtil {

    private static final Logger LOGGER = Logger.getLogger(AgentCommunicatorUtil.class);

    public static boolean createFederatedNetwork(String cidrNotation, String virtualIpAddress) {
        String permissionFilePath = PropertiesHolder.getInstance().getProperty(FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH_KEY);
        String agentUser = PropertiesHolder.getInstance().getProperty(FEDERATED_NETWORK_AGENT_USER_KEY);
        String agentPrivateIp = PropertiesHolder.getInstance().getProperty(FEDERATED_NETWORK_AGENT_PRIVATE_ADDRESS_KEY);
        String agentPublicIp = PropertiesHolder.getInstance().getProperty(FEDERATED_NETWORK_AGENT_ADDRESS_KEY);
        String addFederatedNetworkScriptPath = PropertiesHolder.getInstance().getProperty(ADD_FEDERATED_NETWORK_SCRIPT_PATH_KEY);

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

    public static boolean deleteFederatedNetwork(String cidr) {
        String permissionFilePath = PropertiesHolder.getInstance().getProperty(FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH_KEY);
        String agentUser = PropertiesHolder.getInstance().getProperty(FEDERATED_NETWORK_AGENT_USER_KEY);
        String agentPublicIp = PropertiesHolder.getInstance().getProperty(FEDERATED_NETWORK_AGENT_ADDRESS_KEY);
        String removeFederatedNetworkScriptPath = PropertiesHolder.getInstance().getProperty(REMOVE_FEDERATED_NETWORK_SCRIPT_PATH_KEY);

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
