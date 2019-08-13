package cloud.fogbow.fns.core;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.fns.api.http.response.InstanceStatus;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.exceptions.AgentCommucationException;
import cloud.fogbow.fns.core.exceptions.FederatedNetworkNotFoundException;
import cloud.fogbow.fns.core.exceptions.InvalidCidrException;
import cloud.fogbow.fns.core.exceptions.NotEmptyFederatedNetworkException;
import cloud.fogbow.fns.core.model.*;
import cloud.fogbow.fns.core.serviceconnector.ServiceConnector;
import cloud.fogbow.fns.core.serviceconnector.ServiceConnectorFactory;
import cloud.fogbow.fns.utils.FederatedNetworkUtil;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FederatedNetworkOrderController {
    private static final Logger LOGGER = Logger.getLogger(FederatedNetworkOrderController.class);

    public static final String LOCAL_MEMBER_NAME = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.LOCAL_MEMBER_NAME_KEY);

    // Federated Network methods
    public FederatedNetworkOrder getFederatedNetwork(String orderId) throws FederatedNetworkNotFoundException {
        FederatedNetworkOrder requestedOrder = FederatedNetworkOrdersHolder.getInstance().getOrder(orderId);
        if (requestedOrder == null) {
            throw new FederatedNetworkNotFoundException(orderId);
        }
        return requestedOrder;
    }

    public void addFederatedNetwork(FederatedNetworkOrder federatedNetwork, SystemUser systemUser)
            throws InvalidCidrException, UnexpectedException {
        synchronized (federatedNetwork) {
            federatedNetwork.setSystemUser(systemUser);

            SubnetUtils.SubnetInfo subnetInfo = FederatedNetworkUtil.getSubnetInfo(federatedNetwork.getCidr());

            if (!FederatedNetworkUtil.isSubnetValid(subnetInfo)) {
                LOGGER.error(String.format(Messages.Exception.INVALID_CIDR, federatedNetwork.getCidr()));
                throw new InvalidCidrException(String.format(Messages.Exception.INVALID_CIDR, federatedNetwork.getCidr()));
            }

            this.activateOrder(federatedNetwork);
        }
    }

    public void deleteFederatedNetwork(FederatedNetworkOrder federatedNetwork)
            throws NotEmptyFederatedNetworkException, FogbowException {
        synchronized (federatedNetwork) {
            LOGGER.info(String.format(Messages.Info.INITIALIZING_DELETE_METHOD, federatedNetwork.getId()));

            if (!federatedNetwork.isAssignedIpsEmpty()) {
                throw new NotEmptyFederatedNetworkException();
            }

            LOGGER.info(String.format(Messages.Info.DELETING_FEDERATED_NETWORK, federatedNetwork.toString()));

            if (federatedNetwork.getOrderState() != OrderState.FAILED) {
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

            // If the state of the order is still FAILED, this is because in the creation, it was not possible to
            // connect to the Agent. Thus, there is nothing to remove at the Agent, and an exception does not
            // need to be thrown.
            OrderStateTransitioner.transition(federatedNetwork, OrderState.CLOSED);
        }
    }

    private boolean allProvidersRemovedTheConfiguration(Collection<MemberConfigurationState> values) {
        for (MemberConfigurationState state : values) {
            if (!state.equals(MemberConfigurationState.REMOVED)) {
                return false;
            }
        }
        return true;
    }

    public Collection<InstanceStatus> getFederatedNetworksStatusByUser(SystemUser systemUser) {
        Collection<FederatedNetworkOrder> orders = FederatedNetworkOrdersHolder.getInstance().getActiveOrders().values();

        // Filter all orders of resourceType from systemUser that are not closed (closed orders have been deleted by
        // the user and should not be seen; they will disappear from the system).
        return orders.stream()
                .filter(order -> order.getSystemUser().equals(systemUser))
                .filter(order -> !order.getOrderState().equals(OrderState.DEACTIVATED))
                .map(orderToInstanceStatus())
                .collect(Collectors.toList());
    }

    public static Function<FederatedNetworkOrder, InstanceStatus> orderToInstanceStatus() {
        return order -> {
            InstanceState status = order.getInstanceStateFromOrderState();
            InstanceStatus instanceStatus = new InstanceStatus(order.getId(), order.getName(), LOCAL_MEMBER_NAME, status);
            return instanceStatus;
        };
    }

    public void activateOrder(FederatedNetworkOrder order) throws UnexpectedException {
        synchronized (order) {
            order.setOrderState(OrderState.OPEN);
            FederatedNetworkOrdersHolder.getInstance().insertNewOrder(order);
        }
    }

    public void deactivateOrder(FederatedNetworkOrder order) throws UnexpectedException {
        synchronized (order) {
            if (!order.getOrderState().equals(OrderState.CLOSED)) {
                String message = Messages.Exception.ORDER_SHOULD_BE_CLOSED_BEFORE_DEACTIVATED;
                throw new RuntimeException(String.format(message, order.getId()));
            }
            FederatedNetworkOrdersHolder.getInstance().removeOrder(order);
        }
    }
}
