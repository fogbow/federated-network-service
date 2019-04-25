package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import cloud.fogbow.fns.utils.BashScriptRunner;
import org.apache.log4j.Logger;

public class LocalDfnsServiceConnector extends DfnsServiceConnector {
    private static final Logger LOGGER = Logger.getLogger(LocalDfnsServiceConnector.class);

    private static final String LOCAL_MEMBER_NAME = PropertiesHolder.getInstance().getProperty(
            ConfigurationPropertyKeys.XMPP_JID_KEY);

    private static final int SUCCESS_EXIT_CODE = 0;

    private BashScriptRunner runner;

    public LocalDfnsServiceConnector(BashScriptRunner runner) {
        this.runner = runner;
    }

    @Override
    public MemberConfigurationState configure(FederatedNetworkOrder order) throws UnexpectedException {
        BashScriptRunner.Output output = this.runner.run("echo", "Hello");
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
}
