package cloud.fogbow.fns.core;

import cloud.fogbow.common.exceptions.UnauthorizedRequestException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.fns.api.http.response.InstanceStatus;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.exceptions.FederatedNetworkNotFoundException;
import cloud.fogbow.fns.core.exceptions.InvalidCidrException;
import cloud.fogbow.fns.core.exceptions.NotEmptyFederatedNetworkException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.InstanceState;
import cloud.fogbow.fns.core.model.OrderState;
import cloud.fogbow.fns.utils.FederatedNetworkUtil;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FederatedNetworkOrderController {
    private static final Logger LOGGER = Logger.getLogger(FederatedNetworkOrderController.class);
    public static final String RAS_NAME = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.LOCAL_MEMBER_ID_KEY);

    // Federated Network methods

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

    public void deleteFederatedNetwork(String federatedNetworkId, SystemUser systemUser)
            throws NotEmptyFederatedNetworkException, FederatedNetworkNotFoundException,
            UnauthorizedRequestException, UnexpectedException {
        LOGGER.info(String.format(Messages.Info.INITIALIZING_DELETE_METHOD, systemUser, federatedNetworkId));
        FederatedNetworkOrder federatedNetwork = this.getFederatedNetwork(federatedNetworkId, systemUser);

        if (federatedNetwork == null) {
            throw new IllegalArgumentException(
                    String.format(Messages.Exception.UNABLE_TO_FIND_FEDERATED_NETWORK, federatedNetworkId));
        } else if (!federatedNetwork.getComputeIdsAndIps().isEmpty()) {
            throw new NotEmptyFederatedNetworkException();
        }

        synchronized (federatedNetwork) {
            OrderStateTransitioner.transition(federatedNetwork, OrderState.CLOSED);
        }
    }

    public FederatedNetworkOrder getFederatedNetwork(String federatedNetworkId, SystemUser systemUser)
            throws FederatedNetworkNotFoundException, UnauthorizedRequestException {

        FederatedNetworkOrder federatedNetworkOrder = FederatedNetworkOrdersHolder.getInstance().getOrder(federatedNetworkId);

        if (federatedNetworkOrder != null) {
            if (federatedNetworkOrder.getSystemUser().equals(systemUser)) {
                return federatedNetworkOrder;
            }
            throw new UnauthorizedRequestException();
        }
        throw new FederatedNetworkNotFoundException(federatedNetworkId);
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
            InstanceStatus instanceStatus = new InstanceStatus(order.getId(), order.getName(), RAS_NAME, status);
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
