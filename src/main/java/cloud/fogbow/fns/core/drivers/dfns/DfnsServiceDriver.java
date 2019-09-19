package cloud.fogbow.fns.core.drivers.dfns;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.fns.api.parameters.FederatedCompute;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.PropertiesHolder;
import cloud.fogbow.fns.core.drivers.ServiceDriver;
import cloud.fogbow.fns.core.model.ConfigurationMode;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import cloud.fogbow.fns.core.serviceconnector.ServiceConnector;
import cloud.fogbow.fns.core.serviceconnector.ServiceConnectorFactory;
import cloud.fogbow.ras.core.models.UserData;
import org.apache.log4j.Logger;

public class DfnsServiceDriver implements ServiceDriver {

    private static final Logger LOGGER = Logger.getLogger(DfnsServiceDriver.class);

    private final String LOCAL_MEMBER_NAME = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.LOCAL_MEMBER_NAME_KEY);
    private ServiceConnector dfnsServiceConnetor;

    public DfnsServiceDriver() {
        this.dfnsServiceConnetor = ServiceConnectorFactory.getInstance().getServiceConnector(ConfigurationMode.DFNS, LOCAL_MEMBER_NAME);
    }

    public DfnsServiceDriver(String memberName) {
        this.dfnsServiceConnetor = ServiceConnectorFactory.getInstance().getServiceConnector(ConfigurationMode.DFNS, memberName);
    }

    @Override
    public void processOpen(FederatedNetworkOrder order) throws FogbowException {
        try {
            order.setVlanId(dfnsServiceConnetor.acquireVlanId());
        } catch(FogbowException ex) {
            LOGGER.error(Messages.Exception.NO_MORE_VLAN_IDS_AVAILABLE);
            throw new FogbowException(ex.getMessage());
        }
    }

    @Override
    public void processSpawning(FederatedNetworkOrder order) throws FogbowException {
        for (String provider : order.getProviders().keySet()) {
            MemberConfigurationState memberState = dfnsServiceConnetor.configure(order);
            order.getProviders().put(provider, memberState);
        }
    }

    @Override
    public void processClosed(FederatedNetworkOrder order) throws FogbowException {
        if (order.getOrderState().equals(OrderState.FAILED)) {
            for (String provider : federatedNetwork.getProviders().keySet()) {
                ServiceConnector connector = ServiceConnectorFactory.getInstance().getServiceConnector(
                        federatedNetwork.getConfigurationMode(), provider);
                if (!federatedNetwork.getProviders().get(provider).equals(MemberConfigurationState.REMOVED)) {
                    if (connector.remove(federatedNetwork)) {
                        federatedNetwork.getProviders().put(provider, MemberConfigurationState.REMOVED);
                    }
                }
            }

            boolean providersRemovedTheConfiguration = allProvidersRemovedTheConfiguration(federatedNetwork.getProviders().values());
            if (!providersRemovedTheConfiguration) {
                LOGGER.info(String.format(Messages.Info.DELETED_FEDERATED_NETWORK, federatedNetwork.toString()));
                throw new UnexpectedException(Messages.Exception.UNABLE_TO_REMOVE_FEDERATED_NETWORK, new AgentCommucationException());
            }

            ServiceConnector connector = ServiceConnectorFactory.getInstance().getServiceConnector(
                    federatedNetwork.getConfigurationMode(), LOCAL_MEMBER_NAME);
            connector.releaseVlanId(federatedNetwork.getVlanId());
            federatedNetwork.setVlanId(-1);
        }
    }

    @Override
    public UserData getComputeUserData(FederatedCompute compute, FederatedNetworkOrder order, String instanceIp) throws FogbowException {

    }

    @Override
    public void cleanup(FederatedNetworkOrder order, String hostIp) throws FogbowException {

    }
}
