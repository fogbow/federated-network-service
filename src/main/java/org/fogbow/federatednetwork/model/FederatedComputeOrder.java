package org.fogbow.federatednetwork.model;

import org.fogbow.federatednetwork.datastore.DatabaseManager;
import org.fogbow.federatednetwork.datastore.StableStorage;
import org.fogbowcloud.ras.core.models.orders.ComputeOrder;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.SQLException;
import java.util.Objects;

@Entity
@Table(name = "federated_compute_table")
public class FederatedComputeOrder extends FederatedOrder {

    @Column
    private String federatedNetworkId;

    @Column
    private String federatedIp;

    @Embedded
    private ComputeOrder computeOrder;

    public FederatedComputeOrder() {
        super();
    }

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

    public void updateIdOnComputeCreation(String newId) throws SQLException {
        StableStorage databaseManager = DatabaseManager.getInstance();
        this.setId(newId);
        databaseManager.put(this);
    }

    public void deactivateCompute() throws SQLException {
        StableStorage databaseManager = DatabaseManager.getInstance();
        databaseManager.put(this);
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
