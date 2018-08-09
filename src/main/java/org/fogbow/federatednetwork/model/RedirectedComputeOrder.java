package org.fogbow.federatednetwork.model;

import org.fogbow.federatednetwork.datastore.StableStorage;
import org.fogbowcloud.manager.core.models.orders.ComputeOrder;
import org.fogbowcloud.manager.core.models.orders.UserData;
import org.fogbowcloud.manager.core.models.tokens.FederationUser;

import java.util.List;
import java.util.Objects;

public class RedirectedComputeOrder {

    private String federatedNetworkId;
    private String federatedIp;
    private ComputeOrder computeOrder;

    public RedirectedComputeOrder(String federatedNetworkId, String federatedIp, ComputeOrder computeOrder) {
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

    public void setUserDataInComputeOrder(UserData userData) {
        ComputeOrder computeOrderWithUserData = new ComputeOrder(computeOrder.getId(), computeOrder.getFederationUser(),
                computeOrder.getRequestingMember(), computeOrder.getProvidingMember(), computeOrder.getvCPU(),
                computeOrder.getMemory(), computeOrder.getDisk(), computeOrder.getImageId(), userData,
                computeOrder.getPublicKey(), computeOrder.getNetworksId());
        setComputeOrder(computeOrderWithUserData);
    }

    public void updateIdOnComputeCreation(String newId){
        StableStorage databaseManager = null;
        this.computeOrder.setId(newId);
        databaseManager.addRedirectedCompute(this);
    }

    public void deactivateCompute() {
        StableStorage databaseManager = null;
        databaseManager.deleteRedirectedCompute(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RedirectedComputeOrder that = (RedirectedComputeOrder) o;
        return Objects.equals(getFederatedIp(), that.getFederatedIp()) &&
                Objects.equals(getComputeOrder(), that.getComputeOrder());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFederatedIp(), getComputeOrder());
    }
}
