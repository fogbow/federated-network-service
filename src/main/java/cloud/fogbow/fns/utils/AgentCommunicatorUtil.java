package cloud.fogbow.fns.utils;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.core.exceptions.AgentCommunicationException;
import cloud.fogbow.fns.core.model.ConfigurationMode;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.apache.log4j.Logger;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.common.util.ProcessUtil;
import cloud.fogbow.fns.constants.Messages;

import java.io.IOException;
import java.util.UUID;

import static cloud.fogbow.fns.constants.ConfigurationPropertyKeys.*;

public class AgentCommunicatorUtil {

    private static final Logger LOGGER = Logger.getLogger(AgentCommunicatorUtil.class);

    public static final int SUCCESS_EXIT_CODE = 0;
    public static final int AGENT_SSH_PORT = 22;
    public static final String DELETE_FEDERATED_NETWORK_SCRIPT_PREFIX = "delete-federated-network";
    public static final String CREATE_FEDERATED_NETWORK_SCRIPT_PREFIX = "create-federated-network";

    public static void createFederatedNetwork(String cidrNotation, String virtualIpAddress) throws FogbowException {
        String permissionFilePath = PropertiesHolder.getInstance().getProperty(FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH_KEY);
        String agentUser = PropertiesHolder.getInstance().getProperty(FEDERATED_NETWORK_AGENT_USER_KEY);
        String agentPrivateIp = PropertiesHolder.getInstance().getProperty(FEDERATED_NETWORK_AGENT_PRIVATE_ADDRESS_KEY);
        String agentPublicIp = PropertiesHolder.getInstance().getProperty(FEDERATED_NETWORK_AGENT_ADDRESS_KEY);
        String addFederatedNetworkScriptPath = PropertiesHolder.getInstance().getProperty(ADD_FEDERATED_NETWORK_SCRIPT_PATH_KEY, ConfigurationMode.VANILLA);
        String hostScriptPath = PropertiesHolder.getInstance().getProperty(AGENT_SCRIPTS_PATH_KEY, ConfigurationMode.VANILLA) + CREATE_FEDERATED_NETWORK_SCRIPT_PREFIX;

        String remoteFilePath = pasteScript(addFederatedNetworkScriptPath, agentPublicIp, hostScriptPath, permissionFilePath, agentUser);

        ProcessBuilder builder = new ProcessBuilder("ssh", "-o", "UserKnownHostsFile=/dev/null", "-o",
                "StrictHostKeyChecking=no", "-i", permissionFilePath, agentUser + "@" + agentPublicIp,
                "sudo", remoteFilePath, agentPrivateIp, agentPublicIp, cidrNotation, virtualIpAddress);
        LOGGER.info("Trying to call agent with atts (" + cidrNotation + "): " + builder.command());

        int resultCode = 0;
        try {
            Process process = builder.start();
            LOGGER.info(String.format(Messages.Error.TRYING_TO_CREATE_AGENT_OUTPUT, cidrNotation,
                    ProcessUtil.getOutput(process)));
            LOGGER.info(String.format(Messages.Error.TRYING_TO_CREATE_AGENT_ERROR, cidrNotation, ProcessUtil.getError(process)));
            resultCode = process.waitFor();
        } catch (Exception e) {
            LOGGER.error(String.format(Messages.Error.UNABLE_TO_CALL_AGENT, resultCode), e);
        }

        if(resultCode != 0) {
            throw new AgentCommunicationException(String.format(Messages.Error.UNABLE_TO_CALL_AGENT, resultCode));
        }
    }

    public static void deleteFederatedNetwork(String cidr) throws FogbowException {
        String permissionFilePath = PropertiesHolder.getInstance().getProperty(FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH_KEY);
        String agentUser = PropertiesHolder.getInstance().getProperty(FEDERATED_NETWORK_AGENT_USER_KEY);
        String agentPublicIp = PropertiesHolder.getInstance().getProperty(FEDERATED_NETWORK_AGENT_ADDRESS_KEY);
        String removeFederatedNetworkScriptPath = PropertiesHolder.getInstance().getProperty(REMOVE_FEDERATED_NETWORK_SCRIPT_PATH_KEY, ConfigurationMode.VANILLA);
        String hostScriptPath = PropertiesHolder.getInstance().getProperty(AGENT_SCRIPTS_PATH_KEY, ConfigurationMode.VANILLA) + DELETE_FEDERATED_NETWORK_SCRIPT_PREFIX;

        String remoteFilePath = pasteScript(removeFederatedNetworkScriptPath, agentPublicIp, hostScriptPath, permissionFilePath, agentUser);

        ProcessBuilder builder = new ProcessBuilder("ssh", "-o", "UserKnownHostsFile=/dev/null", "-o",
                "StrictHostKeyChecking=no", "-i", permissionFilePath, agentUser + "@" + agentPublicIp,
                "sudo", remoteFilePath, cidr);
        LOGGER.info("Trying to remove network on agent with atts (" + cidr + "): " + builder.command());

        int resultCode = 0;
        try {
            Process process = builder.start();
            LOGGER.info(String.format(Messages.Error.TRYING_TO_DELETE_AGENT_OUTPUT, cidr,
                    ProcessUtil.getOutput(process)));
            LOGGER.info(String.format(Messages.Error.TRYING_TO_DELETE_AGENT_ERROR, cidr, ProcessUtil.getError(process)));
            resultCode = process.waitFor();
        } catch (Exception e) {
            LOGGER.error(String.format(Messages.Error.UNABLE_TO_DELETE_AGENT, resultCode), e);
        }

        if(resultCode != 0) {
            throw new AgentCommunicationException(String.format(Messages.Error.UNABLE_TO_DELETE_AGENT, resultCode));
        }
    }

    public static String pasteScript(String scriptFilePath, String hostIp, String hostScriptPath, String permissionFile, String remoteUser) throws FogbowException{
        String randomScriptSuffix = UUID.randomUUID().toString();
        String remoteFilePath = hostScriptPath + randomScriptSuffix;
        ProcessBuilder builder = new ProcessBuilder("scp", "-i", permissionFile, scriptFilePath, remoteUser + "@" + hostIp + ":" + remoteFilePath);

        int resultCode = 0;
        try {
            Process process = builder.start();
            resultCode = process.waitFor();
        } catch (Exception e) {
            LOGGER.error(String.format(Messages.Error.UNABLE_TO_COPY_FILE_REMOTLY, resultCode), e);
        }

        if(resultCode != 0) {
            throw new AgentCommunicationException(String.format(Messages.Error.UNABLE_TO_COPY_FILE_REMOTLY, resultCode));
        }

        return remoteFilePath;
    }

    public static void executeAgentCommand(String command, String exceptionMessage) throws FogbowException{
        String permissionFilePath = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH_KEY);
        String agentUser = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_USER_KEY);
        String agentPublicIp = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_ADDRESS_KEY);

        SSHClient client = new SSHClient();
        client.addHostKeyVerifier((arg0, arg1, arg2) -> true);

        try {
            try {
                // connects to the DMZ host
                client.connect(agentPublicIp, AGENT_SSH_PORT);

                // authorizes using the DMZ private key
                client.authPublickey(agentUser, permissionFilePath);

                try (Session session = client.startSession()) {
                    Session.Command c = session.exec(command);

                    // waits for the command to finish
                    c.join();

                    if(c.getExitStatus() != SUCCESS_EXIT_CODE) {
                        throw new UnexpectedException(exceptionMessage);
                    }
                }
            } finally {
                client.disconnect();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new UnexpectedException(e.getMessage(), e);
        }
    }
}
