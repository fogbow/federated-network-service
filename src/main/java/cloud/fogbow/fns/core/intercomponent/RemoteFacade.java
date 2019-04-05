package cloud.fogbow.fns.core.intercomponent;

import cloud.fogbow.fns.core.model.ConfigurationMode;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import cloud.fogbow.fns.core.serviceconnector.ServiceConnector;
import cloud.fogbow.fns.core.serviceconnector.ServiceConnectorFactory;

public class RemoteFacade {
    // TODO ARNETT
    private static final String LOCAL_MEMBER_ID = "retrieveFromFile";

    private static RemoteFacade instance;

    private RemoteFacade() {
    }

    public static RemoteFacade getInstance() {
        synchronized (RemoteFacade.class) {
            if (instance == null) {
                instance = new RemoteFacade();
            }
            return instance;
        }
    }

    public MemberConfigurationState configureMember(FederatedNetworkOrder order) {
        ServiceConnector serviceConnector = ServiceConnectorFactory.getInstance().getServiceConnector(
                order.getConfigurationMode(), LOCAL_MEMBER_ID);
        return serviceConnector.configure(order);
    }

}
