package cloud.fogbow.fns.core.drivers.intercomponent;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.fns.core.ServiceDriverConnector;
import cloud.fogbow.fns.core.drivers.ServiceDriver;
import cloud.fogbow.fns.core.drivers.dfns.AgentConfiguration;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;

public class RemoteFacade {
    private static RemoteFacade instance;

    protected RemoteFacade() {
    }

    public static RemoteFacade getInstance() {
        synchronized (RemoteFacade.class) {
            if (instance == null) {
                instance = new RemoteFacade();
            }
            return instance;
        }
    }

    public void removeAgentToComputeTunnel(String providerId, FederatedNetworkOrder order, String hostIp) throws FogbowException {
        ServiceDriver driver = getDriver(order.getServiceName());
        driver.cleanupAgent(providerId, order, hostIp);
    }

    public AgentConfiguration configureAgent(String publicKey, String serviceName) throws FogbowException {
        ServiceDriver driver = getDriver(serviceName);
        return driver.doConfigureAgent(publicKey);
    }

    protected ServiceDriver getDriver(String serviceName) {
        return new ServiceDriverConnector(serviceName).getDriver();
    }
}
