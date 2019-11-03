package cloud.fogbow.fns.core.drivers.vanilla;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.util.CloudInitUserDataBuilder;
import cloud.fogbow.common.util.ProcessUtil;
import cloud.fogbow.fns.api.parameters.FederatedCompute;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.drivers.CommonServiceDriver;
import cloud.fogbow.fns.core.drivers.constants.DriversConfigurationPropertyDefaults;
import cloud.fogbow.fns.core.drivers.constants.DriversConfigurationPropertyKeys;
import cloud.fogbow.fns.core.exceptions.AgentCommunicationException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import cloud.fogbow.fns.core.drivers.dfns.AgentConfiguration;
import cloud.fogbow.fns.utils.FederatedNetworkUtil;
import cloud.fogbow.ras.core.models.UserData;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class VanillaServiceDriver extends CommonServiceDriver {

    private static final Logger LOGGER = Logger.getLogger(VanillaServiceDriver.class);
    public static final String SERVICE_NAME = "vanilla";
    private static Properties properties = PropertiesHolder.getInstance().getProperties(SERVICE_NAME);
    private final int DEFAULT_VLAN_ID = -1;
    public static final String DELETE_FEDERATED_NETWORK_SCRIPT_PREFIX = "delete-federated-network";
    public static final String CREATE_FEDERATED_NETWORK_SCRIPT_PREFIX = "create-federated-network";
    public static final String IPSEC_INSTALLATION_PATH = "bin/agent-scripts/vanilla/ipsec-configuration";
    public static final String FEDERATED_NETWORK_USER_DATA_TAG = "FNS_SCRIPT";
    public static final String LEFT_SOURCE_IP_KEY = "#LEFT_SOURCE_IP#";
    public static final String RIGHT_IP = "#RIGHT_IP#";
    public static final String RIGHT_SUBNET_KEY = "#RIGHT_SUBNET#";
    public static final String IS_FEDERATED_VM_KEY = "#IS_FEDERATED_VM#";
    public static final String PRE_SHARED_KEY_KEY = "#PRE_SHARED_KEY#";
    public static final String REMOVE_FEDERATED_NETWORK_SCRIPT_PATH_KEY = "bin/agent-scripts/vanilla/delete-federated-network";
    public static final String ADD_FEDERATED_NETWORK_SCRIPT_PATH_KEY = "bin/agent-scripts/vanilla/create-federated-network";

    public VanillaServiceDriver() {
    }

    @Override
    public void processOpen(FederatedNetworkOrder order) {
        order.setVlanId(DEFAULT_VLAN_ID);
    }

    @Override
    public void processSpawning(FederatedNetworkOrder order) throws FogbowException {
        try {
            SubnetUtils.SubnetInfo subnetInfo = FederatedNetworkUtil.getSubnetInfo(order.getCidr());
            createFederatedNetwork(order.getCidr(), subnetInfo.getLowAddress());
        } catch (FogbowException e) {
            LOGGER.error(e.getMessage(), e);
            throw new FogbowException(e.getMessage());
        }
    }

    @Override
    public void processClosed(FederatedNetworkOrder order) throws FogbowException {
        for (String provider : order.getProviders().keySet()) {
            if (!order.getProviders().get(provider).equals(MemberConfigurationState.REMOVED)) {
               remove(order, provider);
            }
        }
    }

    private void remove(FederatedNetworkOrder order, String provider) throws FogbowException{
        try {
            deleteFederatedNetwork(order.getCidr());
            order.getProviders().put(provider, MemberConfigurationState.REMOVED);
        } catch(FogbowException ex) {
            throw new UnexpectedException(Messages.Exception.UNABLE_TO_REMOVE_FEDERATED_NETWORK, ex);
        }
    }

    @Override
    public AgentConfiguration configureAgent(String provider) {
        return null;
    }
    @Override
    public AgentConfiguration doConfigureAgent(String publicKey) { return null; }

    @Override
    public UserData getComputeUserData(AgentConfiguration agentConfiguration, FederatedCompute compute, FederatedNetworkOrder order, String instanceIp) throws FogbowException {
        try {
            return getVanillaUserData(instanceIp, order.getCidr());
        } catch (IOException e) {
            throw new FogbowException(e.getMessage(), e);
        }
    }

    @Override
    public void cleanupAgent(FederatedNetworkOrder order, String hostIp){

    }

    @Override
    public String getAgentIp() {
        // Vanilla uses the public IP of the agent to establish the tunnel between the VM and the agent
        return properties.getProperty(DriversConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_PUBLIC_ADDRESS_KEY);
    }

    private void createFederatedNetwork(String cidrNotation, String virtualIpAddress) throws FogbowException {
        String permissionFilePath = properties.getProperty(DriversConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH_KEY);
        String agentUser = properties.getProperty(DriversConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_USER_KEY);
        String agentPrivateIp = properties.getProperty(DriversConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_PRIVATE_ADDRESS_KEY);
        String agentPublicIp = properties.getProperty(DriversConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_PUBLIC_ADDRESS_KEY);
        String addFederatedNetworkScriptPath = ADD_FEDERATED_NETWORK_SCRIPT_PATH_KEY;
        String hostScriptPath = properties.getProperty(DriversConfigurationPropertyKeys.AGENT_SCRIPTS_PATH_KEY, DriversConfigurationPropertyDefaults.AGENT_SCRIPTS_PATH) + CREATE_FEDERATED_NETWORK_SCRIPT_PREFIX;

        String remoteFilePath = pasteScript(addFederatedNetworkScriptPath, agentPublicIp, hostScriptPath, permissionFilePath, agentUser);

        ProcessBuilder builder = new ProcessBuilder("ssh", "-o", "UserKnownHostsFile=/dev/null", "-o",
                "StrictHostKeyChecking=no", "-i", permissionFilePath, agentUser + "@" + agentPublicIp,
                "sudo", remoteFilePath, agentPrivateIp, agentPublicIp, cidrNotation, virtualIpAddress);

        int resultCode = 0;
        try {
            Process process = builder.start();
            resultCode = process.waitFor();
        } catch (Exception e) {
            LOGGER.error(String.format(Messages.Error.UNABLE_TO_CALL_AGENT, resultCode), e);
        }

        if(resultCode != 0) {
            throw new AgentCommunicationException(String.format(Messages.Error.UNABLE_TO_CALL_AGENT, resultCode));
        }
    }

    private void deleteFederatedNetwork(String cidr) throws FogbowException {
        String permissionFilePath = properties.getProperty(DriversConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH_KEY);
        String agentUser = properties.getProperty(DriversConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_USER_KEY);
        String agentPublicIp = properties.getProperty(DriversConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_PUBLIC_ADDRESS_KEY);
        String removeFederatedNetworkScriptPath = REMOVE_FEDERATED_NETWORK_SCRIPT_PATH_KEY;
        String hostScriptPath = properties.getProperty(DriversConfigurationPropertyKeys.AGENT_SCRIPTS_PATH_KEY, DriversConfigurationPropertyDefaults.AGENT_SCRIPTS_PATH) + DELETE_FEDERATED_NETWORK_SCRIPT_PREFIX;

        String remoteFilePath = pasteScript(removeFederatedNetworkScriptPath, agentPublicIp, hostScriptPath, permissionFilePath, agentUser);

        ProcessBuilder builder = new ProcessBuilder("ssh", "-o", "UserKnownHostsFile=/dev/null", "-o",
                "StrictHostKeyChecking=no", "-i", permissionFilePath, agentUser + "@" + agentPublicIp,
                "sudo", remoteFilePath, cidr);

        int resultCode = 0;
        try {
            Process process = builder.start();
            resultCode = process.waitFor();
        } catch (Exception e) {
            LOGGER.error(String.format(Messages.Error.UNABLE_TO_DELETE_AGENT, resultCode), e);
        }

        if(resultCode != 0) {
            throw new AgentCommunicationException(String.format(Messages.Error.UNABLE_TO_DELETE_AGENT, resultCode));
        }
    }

    @NotNull
    private UserData getVanillaUserData(String federatedIp, String cidr) throws IOException {
        InputStream inputStream = new FileInputStream(IPSEC_INSTALLATION_PATH);
        String cloudInitScript = IOUtils.toString(inputStream);
        String newScript = replaceScriptValues(cloudInitScript, federatedIp, properties.getProperty(DriversConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_PUBLIC_ADDRESS_KEY),
                cidr, properties.getProperty(DriversConfigurationPropertyKeys.Vanilla.FEDERATED_NETWORK_PRE_SHARED_KEY_KEY));

        byte[] scriptBytes = newScript.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedScriptBytes = Base64.encodeBase64(scriptBytes);
        String encryptedScript = new String(encryptedScriptBytes, StandardCharsets.UTF_8);

        return new UserData(encryptedScript,
                CloudInitUserDataBuilder.FileType.SHELL_SCRIPT, FEDERATED_NETWORK_USER_DATA_TAG);
    }

    private String replaceScriptValues(String script, String federatedComputeIp, String agentPublicIp,
            String cidr, String preSharedKey) {
        String isFederatedVM = "true";
        String scriptReplaced = script.replace(IS_FEDERATED_VM_KEY, isFederatedVM);
        scriptReplaced = scriptReplaced.replace(LEFT_SOURCE_IP_KEY, federatedComputeIp);
        scriptReplaced = scriptReplaced.replace(RIGHT_IP, agentPublicIp);
        scriptReplaced = scriptReplaced.replace(RIGHT_SUBNET_KEY, cidr);
        scriptReplaced = scriptReplaced.replace(PRE_SHARED_KEY_KEY, preSharedKey);
        scriptReplaced = scriptReplaced.replace("\n", "[[\\n]]");
        scriptReplaced = scriptReplaced.replace("\r", "");
        return scriptReplaced;
    }
}
