package org.fogbow.federatednetwork.model;

import org.fogbow.federatednetwork.datastore.DatabaseManager;
import org.fogbowcloud.manager.core.models.instances.InstanceState;
import org.fogbowcloud.manager.core.models.orders.OrderState;

import javax.persistence.*;
import java.io.Serializable;
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
    private String requestingMember;

    @Column
    private String providingMember;

    @Column
    private String instanceId;

    @Column
    private InstanceState cachedInstanceState;

    public FederatedOrder() {

    }

    public FederatedOrder(String id) {
        this.id = id;
    }

    /** Creating Order with predefined Id. */
    public FederatedOrder(String id, FederatedUser user, String requestingMember, String providingMember) {
        this(id);
        this.user = user;
        this.requestingMember = requestingMember;
        this.providingMember = providingMember;
    }

    public FederatedOrder(FederatedUser user, String requestingMember, String providingMember) {
        this.id = String.valueOf(UUID.randomUUID());
        this.user = user;
        this.requestingMember = requestingMember;
        this.providingMember = providingMember;
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
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        databaseManager.put(this);
    }

    public FederatedUser getUser() {
        return user;
    }

    public void setUser(FederatedUser user) {
        this.user = user;
    }

    public String getRequestingMember() {
        return this.requestingMember;
    }

    public void setRequestingMember(String requestingMember) {
        this.requestingMember = requestingMember;
    }

    public String getProvidingMember() {
        return this.providingMember;
    }

    public void setProvidingMember(String providingMember) {
        this.providingMember = providingMember;
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
        return this.providingMember.equals(localMemberId);
    }

    public boolean isRequesterRemote(String localMemberId) {
        return !this.requestingMember.equals(localMemberId);
    }

    public abstract FederatedResourceType getType();

    @Override
    public String toString() {
        return "Order [id=" + this.id + ", orderState=" + this.orderState + ", requestingMember=" +
                this.requestingMember + ", providingMember=" + this.providingMember
                + ", instanceId=" + this.instanceId + "]";
    }
}
