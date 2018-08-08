package org.fogbow.federatednetwork.model;

import org.fogbow.federatednetwork.datastore.StableStorage;
import org.fogbowcloud.manager.core.models.orders.OrderState;
import org.fogbowcloud.manager.core.models.tokens.FederationUser;

import java.util.Objects;

public abstract class FederatedOrder {
    private String id;

    private OrderState orderState;

    private FederationUser federationUser;

    public FederatedOrder(String id) {
        this.id = id;
    }

    /** Creating FederatedOrder with predefined Id. */
    public FederatedOrder(String id, FederationUser federationUser) {
        this(id);
        this.federationUser = federationUser;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public synchronized OrderState getOrderState() {
        return this.orderState;
    }

    public synchronized void setOrderStateInRecoveryMode(OrderState state) {
        this.orderState = state;
    }

    public synchronized void setOrderStateInTestMode(OrderState state) {
        this.orderState = state;
    }

    public synchronized void setOrderState(OrderState state) {
        this.orderState = state;
        StableStorage databaseManager = null;
        if (state.equals(OrderState.OPEN)) {
            // Adding in stable storage newly created order
            databaseManager.add(this);
        } else {
            // Updating in stable storage already existing order
            databaseManager.update(this);
        }
    }

    public FederationUser getFederationUser() {
        return this.federationUser;
    }

    public void setFederationUser(FederationUser federationUser) {
        this.federationUser = federationUser;
    }

    public abstract FederatedResourceType getType();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FederatedOrder that = (FederatedOrder) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "FederatedOrder [id=" + this.id + ", orderState=" + this.orderState + ", federationUser="
                + this.federationUser + "]";
    }
}
