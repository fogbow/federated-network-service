package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import cloud.fogbow.fns.utils.BashScriptRunner;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalDfnsServiceConnector extends DfnsServiceConnector {
    private static final Logger LOGGER = Logger.getLogger(LocalDfnsServiceConnector.class);

    private static final String LOCAL_MEMBER_NAME = PropertiesHolder.getInstance().getProperty(
            ConfigurationPropertyKeys.XMPP_JID_KEY);

    private static final int SUCCESS_EXIT_CODE = 0;
    public static final String CREATE_TUNNELS_SCRIPT_PATH = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.CREATE_TUNNELS_SCRIPT_PATH);

    private BashScriptRunner runner;

    public LocalDfnsServiceConnector(BashScriptRunner runner) {
        this.runner = runner;
    }

    @Override
    public MemberConfigurationState configure(FederatedNetworkOrder order) throws UnexpectedException {
        String[] command = getConfigureCommand(order.getProviders().keySet());
        BashScriptRunner.Output output = this.runner.run(command);
        return (output.getExitCode() == SUCCESS_EXIT_CODE) ? MemberConfigurationState.SUCCESS : MemberConfigurationState.FAILED;
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
    public boolean addInstancePublicKeyToAgent(String instancePublicKey) throws UnexpectedException {
        String permissionFilePath = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_PERMISSION_FILE_PATH_KEY);
        String agentUser = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_USER_KEY);
        String agentPublicIp = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.FEDERATED_NETWORK_AGENT_ADDRESS_KEY);

        String sshCredentials = agentUser + "@" + agentPublicIp;
        String publicKey = String.format("'%s'", instancePublicKey);
        BashScriptRunner.Output output = this.runner.run("echo", publicKey, "|", "ssh", sshCredentials, "-i",
                permissionFilePath,  "-T", "cat", ">>", "~/.ssh/authorized_keys");

        return output.getExitCode() == SUCCESS_EXIT_CODE;
    }

    @NotNull
    private String[] getConfigureCommand(Collection<String> allProviders) {
        List<String> command = new ArrayList<>();
        command.add("bash");
        command.add(CREATE_TUNNELS_SCRIPT_PATH);
        command.addAll(excludeLocalProvider(allProviders));
        return command.toArray(new String[] {});
    }

    @NotNull
    private List<String> excludeLocalProvider(Collection<String> allProviders) {
        Stream<String> providersStream = allProviders.stream();
        return providersStream.filter(provider -> provider.equals(LOCAL_MEMBER_NAME)).collect(Collectors.toList());
    }
}
