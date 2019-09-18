package cloud.fogbow.fns.core.drivers.vanilla;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.fns.api.parameters.FederatedCompute;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.drivers.ServiceDriver;
import cloud.fogbow.fns.core.exceptions.AgentCommunicationException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import cloud.fogbow.fns.core.model.OrderState;
import cloud.fogbow.fns.core.serviceconnector.ServiceConnector;
import cloud.fogbow.fns.core.serviceconnector.ServiceConnectorFactory;
import cloud.fogbow.fns.utils.AgentCommunicatorUtil;
import cloud.fogbow.fns.utils.FederatedNetworkUtil;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.log4j.Logger;

public class VanillaServiceDriver implements ServiceDriver {

    private static final Logger LOGGER = Logger.getLogger(VanillaServiceDriver.class);

    @Override
    public void processOpen(FederatedNetworkOrder order) throws FogbowException {
        order.setVlanId(-1);
    }

    @Override
    public void processSpawningOrder(FederatedNetworkOrder order) throws FogbowException {
        try {
            SubnetUtils.SubnetInfo subnetInfo = FederatedNetworkUtil.getSubnetInfo(order.getCidr());
            AgentCommunicatorUtil.createFederatedNetwork(order.getCidr(), subnetInfo.getLowAddress());
        } catch (FogbowException e) {
            LOGGER.error(e.getMessage(), e);
            throw new FogbowException(e.getMessage());
        }
    }

    @Override
    public void processClosingOrder(FederatedNetworkOrder order) throws FogbowException {
        if (order.getOrderState() != OrderState.FAILED) {
            for (String provider : order.getProviders().keySet()) {
                if (!order.getProviders().get(provider).equals(MemberConfigurationState.REMOVED)) {
                   remove(order, provider);
                }
            }

            ServiceConnector connector = ServiceConnectorFactory.getInstance().getServiceConnector(
                    federatedNetwork.getConfigurationMode(), LOCAL_MEMBER_NAME);
            connector.releaseVlanId(federatedNetwork.getVlanId());
            order.setVlanId(-1);
        }
    }

    private void remove(FederatedNetworkOrder order, String provider) throws FogbowException{
        try {
            AgentCommunicatorUtil.deleteFederatedNetwork(order.getCidr());
            order.getProviders().put(provider, MemberConfigurationState.REMOVED);
        } catch(FogbowException ex) {
            throw new UnexpectedException(Messages.Exception.UNABLE_TO_REMOVE_FEDERATED_NETWORK, new AgentCommunicationException());
        }
    }

    @Override
    public void setupCompute(FederatedCompute compute, FederatedNetworkOrder order) throws FogbowException {

    }

    @Override
    public void terminateCompute(FederatedCompute compute, FederatedNetworkOrder order) throws FogbowException{

    }
}
