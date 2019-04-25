package cloud.fogbow.fns.core.intercomponent;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.fns.constants.ConfigurationPropertyDefaults;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import cloud.fogbow.fns.core.serviceconnector.ServiceConnector;
import cloud.fogbow.fns.core.serviceconnector.ServiceConnectorFactory;
import cloud.fogbow.ras.core.models.orders.ComputeOrder;

public class RemoteFacade {
    private static final String LOCAL_MEMBER_NAME = PropertiesHolder.getInstance().getProperty(
            ConfigurationPropertyDefaults.XMPP_JID_KEY);

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

    public MemberConfigurationState configureMember(FederatedNetworkOrder order) throws UnexpectedException {
        ServiceConnector serviceConnector = ServiceConnectorFactory.getInstance().getServiceConnector(
                order.getConfigurationMode(), LOCAL_MEMBER_NAME);
        return serviceConnector.configure(order);
    }

    public void remove(FederatedNetworkOrder order) throws UnexpectedException {
        ServiceConnector serviceConnector = ServiceConnectorFactory.getInstance().getServiceConnector(
                order.getConfigurationMode(), LOCAL_MEMBER_NAME);
        serviceConnector.remove(order);
    }

    public void removeAgentToComputeTunnel(String hostIp, int vlanId) {
        // TODO DFNS remove the tunnel created for this hostIp via this vlanId
    }
}
