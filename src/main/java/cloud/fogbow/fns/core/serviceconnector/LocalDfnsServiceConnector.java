package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import cloud.fogbow.fns.utils.BashScriptRunner;
import org.apache.log4j.Logger;

import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalDfnsServiceConnector extends DfnsServiceConnector {
    private static final Logger LOGGER = Logger.getLogger(LocalDfnsServiceConnector.class);

    public static final String LOCAL_MEMBER_NAME = PropertiesHolder.getInstance().getProperty(
            ConfigurationPropertyKeys.LOCAL_MEMBER_NAME);

    public static final int SUCCESS_EXIT_CODE = 0;
    public static final String CREATE_TUNNELS_SCRIPT_PATH = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.CREATE_TUNNELS_SCRIPT_PATH);

    public LocalDfnsServiceConnector(BashScriptRunner runner) {
        super(runner);
    }

    @Override
    public MemberConfigurationState configure(FederatedNetworkOrder order) throws UnexpectedException {
        try {
            Set<String> allProviders = order.getProviders().keySet();
            Collection<String> ipAddresses = getIpAddresses(excludeLocalProvider(allProviders));

            BashScriptRunner.Output output = this.runner.runtimeRun(getConfigureCommand(ipAddresses).toArray(new String[]{}));
            return (output.getExitCode() == SUCCESS_EXIT_CODE) ? MemberConfigurationState.SUCCESS : MemberConfigurationState.FAILED;
        } catch (UnknownHostException e) {
            LOGGER.error(e.getMessage(), e);
            return MemberConfigurationState.FAILED;
        }
    }

    // TODO Remove this
    public MemberConfigurationState configureWithSsh(FederatedNetworkOrder order) throws UnexpectedException {
        String permissionFilePath = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH_KEY);
        String agentUser = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_USER_KEY);
        String agentPublicIp = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_ADDRESS_KEY);
        String sshCredentials = agentUser + "@" + agentPublicIp;

        try {
            String[] commandFirstPart = {"ssh", sshCredentials, "-i", permissionFilePath, "-T"};
            List<String> command = new ArrayList<>(Arrays.asList(commandFirstPart));
            Set<String> allProviders = order.getProviders().keySet();
            Collection<String> ipAddresses = getIpAddresses(excludeLocalProvider(allProviders));
            command.addAll(getConfigureCommand(ipAddresses));

            BashScriptRunner.Output output = this.runner.runtimeRun(command.toArray(new String[]{}));
            return (output.getExitCode() == SUCCESS_EXIT_CODE) ? MemberConfigurationState.SUCCESS : MemberConfigurationState.FAILED;
        } catch (UnknownHostException e) {
            LOGGER.error(e.getMessage(), e);
            return MemberConfigurationState.FAILED;
        }
    }

    @Override
    public boolean remove(FederatedNetworkOrder order) throws UnexpectedException {
        // TODO implement this
        BashScriptRunner.Output output = this.runner.run("echo", "Hello");
        return output.getExitCode() == SUCCESS_EXIT_CODE;
    }

    @Override
    public boolean removeAgentToComputeTunnel(String hostIp, int vlanId) throws UnexpectedException {
        // TODO implement this
        BashScriptRunner.Output output = this.runner.run("echo", "Hello");
        return output.getExitCode() == SUCCESS_EXIT_CODE;
    }

    @Override
    public boolean addKeyToAgentAuthorizedPublicKeys(String publicKey) throws UnexpectedException {
        String permissionFilePath = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH_KEY);
        String agentUser = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_USER_KEY);
        String agentPublicIp = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_ADDRESS_KEY);

        String sshCredentials = agentUser + "@" + agentPublicIp;
        List<String> command = new ArrayList<>();
        BashScriptRunner.Output output = this.runner.run("echo", publicKey, "|", "ssh", sshCredentials, "-i",
                permissionFilePath, "-T", "cat", ">>", "~/.ssh/authorized_keys");

        return output.getExitCode() == SUCCESS_EXIT_CODE;
    }

    @Override
    public DfnsAgentConfiguration getDfnsAgentConfiguration(String serializedPublicKey) throws UnknownHostException {
        String defaultNetworkCidr = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.DEFAULT_NETWORK_CIDR_KEY);

        String agentUser = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_USER_KEY);

        return new DfnsAgentConfiguration(defaultNetworkCidr, agentUser, serializedPublicKey);
    }

    private List<String> getConfigureCommand(Collection<String> providersIps) {
        List<String> command = new ArrayList<>();
        command.add("bash");
        command.add(CREATE_TUNNELS_SCRIPT_PATH);
        command.addAll(providersIps);
        return command;
    }

    private Collection<String> excludeLocalProvider(Collection<String> allProviders) {
        Stream<String> providersStream = allProviders.stream();
        return providersStream.filter(provider -> !provider.equals(LOCAL_MEMBER_NAME)).collect(Collectors.toList());
    }

    @Override
    public boolean copyScriptForTunnelFromAgentToComputeCreationIntoAgent() throws UnexpectedException {
        String createTunnelFromAgentToComputeScriptPath = PropertiesHolder.getInstance().getProperty(
                ConfigurationPropertyKeys.CREATE_TUNNEL_FROM_AGENT_TO_COMPUTE_SCRIPT_PATH);

        BashScriptRunner.Output output = this.runner.run("cp", createTunnelFromAgentToComputeScriptPath, SCRIPT_TARGET_PATH);
        return output.getExitCode() == SUCCESS_EXIT_CODE;
    }
}