package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import cloud.fogbow.fns.utils.BashScriptRunner;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalDfnsServiceConnector extends DfnsServiceConnector {
    private static final Logger LOGGER = Logger.getLogger(LocalDfnsServiceConnector.class);

    private static final String LOCAL_MEMBER_NAME = PropertiesHolder.getInstance().getProperty(
            ConfigurationPropertyKeys.XMPP_JID_KEY);

    private static final int SUCCESS_EXIT_CODE = 0;
    public static final String CREATE_TUNNELS_SCRIPT_PATH = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.CREATE_TUNNELS_SCRIPT_PATH);
    public static final String BASH_COMMAND = "bash";

    private BashScriptRunner runner;

    public LocalDfnsServiceConnector(BashScriptRunner runner) {
        this.runner = runner;
    }

    @Override
    public MemberConfigurationState configure(FederatedNetworkOrder order) throws UnexpectedException {
        String permissionFilePath = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH_KEY);
        String agentUser = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_USER_KEY);
        String agentPublicIp = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_ADDRESS_KEY);
        String sshCredentials = agentUser + "@" + agentPublicIp;

        try {
            List<String> command = new ArrayList<>();
            command.add("ssh");
            command.add(sshCredentials);
            command.add("-i");
            command.add(permissionFilePath);
            command.add("-T");
            command.addAll(getConfigureCommand(getIpAddresses(order.getProviders().keySet())));

            BashScriptRunner.Output output = this.runner.runtimeRun(command.toArray(new String[] {}));
            return (output.getExitCode() == SUCCESS_EXIT_CODE) ? MemberConfigurationState.SUCCESS : MemberConfigurationState.FAILED;
        } catch (UnknownHostException e) {
            LOGGER.error(e.getMessage(), e);
            return MemberConfigurationState.FAILED;
        }
    }

    @Override
    public boolean remove(FederatedNetworkOrder order) throws UnexpectedException {
        BashScriptRunner.Output output = this.runner.run("echo", "Hello");
        return output.getExitCode() == SUCCESS_EXIT_CODE;
    }

    @Override
    public boolean removeAgentToComputeTunnel(String hostIp, int vlanId) throws UnexpectedException {
        BashScriptRunner.Output output = this.runner.run("echo", "Hello");
        return output.getExitCode() == SUCCESS_EXIT_CODE;
    }

    @Override
    public boolean allowAccessFromComputeToAgent(String instancePublicKey) throws UnexpectedException {
        String permissionFilePath = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH_KEY);
        String agentUser = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_USER_KEY);
        String agentPublicIp = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_ADDRESS_KEY);

        String sshCredentials = agentUser + "@" + agentPublicIp;
        String publicKey = String.format("'%s'", instancePublicKey);
        BashScriptRunner.Output output = this.runner.run("echo", publicKey, "|", "ssh", sshCredentials, "-i",
                permissionFilePath,  "-T", "cat", ">>", "~/.ssh/authorized_keys");

        return output.getExitCode() == SUCCESS_EXIT_CODE;
    }

    private List<String> getConfigureCommand(Collection<String> allProviders) {
        List<String> command = new ArrayList<>();
        command.add(BASH_COMMAND);
        command.add(CREATE_TUNNELS_SCRIPT_PATH);
        command.addAll(excludeLocalProvider(allProviders));
        return command;
    }

    private List<String> excludeLocalProvider(Collection<String> allProviders) {
        Stream<String> providersStream = allProviders.stream();
        return providersStream.filter(provider -> !provider.equals(LOCAL_MEMBER_NAME)).collect(Collectors.toList());
    }

    private Set<String> getIpAddresses(Set<String> serverNames) throws UnknownHostException {
        Set<String> ipAddresses = new HashSet<>();
        for (String serverName : serverNames) {
            ipAddresses.add(getIpAddress(serverName));
        }
        return ipAddresses;
    }

    private String getIpAddress(String serverName) throws UnknownHostException {
        return InetAddress.getByName(serverName).getHostAddress();
    }
}