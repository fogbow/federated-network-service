package cloud.fogbow.fns.core.serviceconnector;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.fns.core.exceptions.NoVlanIdsLeftException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import cloud.fogbow.fns.utils.BashScriptRunner;
import org.apache.log4j.Logger;

public class LocalDfnsServiceConnector extends DfnsServiceConnector {
    private static final Logger LOGGER = Logger.getLogger(LocalDfnsServiceConnector.class);

    private BashScriptRunner runner;

    public LocalDfnsServiceConnector(BashScriptRunner runner) {
        this.runner = runner;
    }

    @Override
    public int acquireVlanId() throws NoVlanIdsLeftException {
        return -1;
    }

    @Override
    public void releaseVlanId() {
    }

    @Override
    public MemberConfigurationState configure(FederatedNetworkOrder order) {
        try {
            this.runner.run("echo", "Hello");
        } catch (UnexpectedException e) {
            e.printStackTrace();
        }
        return MemberConfigurationState.FAILED;
    }
}
