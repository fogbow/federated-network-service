package org.fogbow.federatednetwork.model;

import org.fogbow.federatednetwork.datastore.DatabaseManager;
import org.fogbow.federatednetwork.datastore.StableStorage;
import org.fogbowcloud.manager.core.models.orders.ComputeOrder;

import java.util.Objects;

public class FederatedComputeOrder extends FederatedOrder {

    private String federatedNetworkId;
    private String federatedIp;
    private ComputeOrder computeOrder;

    public FederatedComputeOrder(String federatedNetworkId, String federatedIp, ComputeOrder computeOrder) {
        this.federatedNetworkId = federatedNetworkId;
        this.federatedIp = federatedIp;
        this.computeOrder = computeOrder;
    }

    public String getFederatedNetworkId() {
        return federatedNetworkId;
    }

    public void setFederatedNetworkId(String federatedNetworkId) {
        this.federatedNetworkId = federatedNetworkId;
    }

    public String getFederatedIp() {
        return federatedIp;
    }

    public void setFederatedIp(String federatedIp) {
        this.federatedIp = federatedIp;
    }

    public ComputeOrder getComputeOrder() {
        return computeOrder;
    }

    public void setComputeOrder(ComputeOrder computeOrder) {
        this.computeOrder = computeOrder;
    }

    public void updateIdOnComputeCreation(String newId){
        StableStorage databaseManager = DatabaseManager.getInstance();
        this.computeOrder.setId(newId);
        databaseManager.put(this);
    }

    public void deactivateCompute() {
        StableStorage databaseManager = DatabaseManager.getInstance();
        databaseManager.delete(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FederatedComputeOrder that = (FederatedComputeOrder) o;
        return Objects.equals(getFederatedIp(), that.getFederatedIp()) &&
                Objects.equals(getComputeOrder(), that.getComputeOrder());
    }

    @Override
    public FederatedResourceType getType() {
        return FederatedResourceType.FEDERATED_COMPUTE;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFederatedIp(), getComputeOrder());
    }
}
