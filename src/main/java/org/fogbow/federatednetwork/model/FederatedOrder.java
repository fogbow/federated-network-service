package org.fogbow.federatednetwork.model;

import org.fogbow.federatednetwork.datastore.DatabaseManager;
import org.fogbowcloud.ras.core.models.instances.InstanceState;
import org.fogbowcloud.ras.core.models.orders.OrderState;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "order_table")
public abstract class FederatedOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column
    @Id
    private String id;

    @Column
    @Enumerated(EnumType.STRING)
    private OrderState orderState;

    @Embedded
    private FederatedUser user;

    @Column
    private String requester;

    @Column
    private String provider;

    @Column
    private String instanceId;

    @Column
    @Enumerated(EnumType.STRING)
    private InstanceState cachedInstanceState;

    public FederatedOrder() {
        this.id = String.valueOf(UUID.randomUUID());
    }

    public FederatedOrder(String id) {
        this.id = id;
    }

    /** Creating Order with predefined Id. */
    public FederatedOrder(String id, FederatedUser user, String requester, String provider) {
        this(id);
        this.user = user;
        this.requester = requester;
        this.provider = provider;
    }

    public FederatedOrder(FederatedUser user, String requester, String provider) {
        this.id = String.valueOf(UUID.randomUUID());
        this.user = user;
        this.requester = requester;
        this.provider = provider;
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

    public synchronized void setOrderState(OrderState state) throws SQLException {
        this.orderState = state;
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        databaseManager.put(this);
    }

    public FederatedUser getUser() {
        return user;
    }

    public void setUser(FederatedUser user) {
        this.user = user;
    }

    public String getRequester() {
        return this.requester;
    }

    public void setRequester(String requester) {
        this.requester = requester;
    }

    public String getProvider() {
        return this.provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getInstanceId() {
        return this.instanceId;
    }

    public synchronized void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public InstanceState getCachedInstanceState() {
        return this.cachedInstanceState;
    }

    public void setCachedInstanceState(InstanceState cachedInstanceState) {
        this.cachedInstanceState = cachedInstanceState;
    }

    public boolean isProviderLocal(String localMemberId) {
        return this.provider.equals(localMemberId);
    }

    public boolean isRequesterRemote(String localMemberId) {
        return !this.requester.equals(localMemberId);
    }

    public abstract FederatedResourceType getType();

    @Override
    public String toString() {
        return "Order [id=" + this.id + ", orderState=" + this.orderState + ", requester=" +
                this.requester + ", provider=" + this.provider
                + ", instanceId=" + this.instanceId + "]";
    }
}
