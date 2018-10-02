package org.fogbow.federatednetwork.model;

import org.fogbow.federatednetwork.datastore.DatabaseManager;
import org.fogbow.federatednetwork.datastore.StableStorage;
import org.fogbowcloud.ras.core.models.orders.OrderState;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.sql.SQLException;
import java.util.*;

@Entity
@Table(name = "federated_network_table")
public class FederatedNetworkOrder extends FederatedOrder {

    @Column
    private String cidr;

    @Column
    private String name;

    @ElementCollection(targetClass = String.class)
    @CollectionTable(name="federated_network_allowed_members")
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<String> providers;

    @Column(length = Integer.MAX_VALUE)
    private int ipsServed = 1;

    @Transient
    private Queue<String> freedIps;

    @ElementCollection(targetClass = String.class)
    @CollectionTable(name="federated_network_computes_ip")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<String> computeIps;

    public FederatedNetworkOrder(String id, FederatedUser federatedUser, String requestingMember,
                                 String providingMember, String cidr, String name, Set<String> providers,
                                 int ipsServed, Queue<String> freedIps, List<String> computeIps) {
        super(id, federatedUser, requestingMember, providingMember);
        this.cidr = cidr;
        this.name = name;
        this.providers = providers;
        this.ipsServed = ipsServed;
        this.freedIps = freedIps;
        this.computeIps = computeIps;
    }

    public FederatedNetworkOrder(FederatedUser federatedUser, String requestingMember, String providingMember,
                                 String cidr, String name, Set<String> providers, int ipsServed,
                                 Queue<String> freedIps, List<String> computeIps) {
        super(federatedUser, requestingMember, providingMember);
        this.cidr = cidr;
        this.name = name;
        this.providers = providers;
        this.ipsServed = ipsServed;
        this.freedIps = freedIps;
        this.computeIps = computeIps;
    }

    public FederatedNetworkOrder() {
        super();
        this.providers = new HashSet<>();
        this.freedIps = new LinkedList<>();
        this.computeIps = new ArrayList<>();
    }

    public synchronized void setOrderState(OrderState state) throws SQLException {
        super.setOrderState(state);
    }

    public synchronized void removeAssociatedIp(String ipToBeReleased) throws SQLException {
        this.computeIps.remove(ipToBeReleased);
        this.freedIps.add(ipToBeReleased);
        StableStorage databaseManager = DatabaseManager.getInstance();
        databaseManager.put(this);
    }

    public synchronized void addAssociatedIp(String ipToBeAttached) throws SQLException {
        if (this.freedIps.contains(ipToBeAttached)) {
            this.freedIps.remove(ipToBeAttached);
        } else {
            this.ipsServed++;
        }
        this.computeIps.add(ipToBeAttached);
        StableStorage databaseManager = DatabaseManager.getInstance();
        databaseManager.put(this);
    }

    public String getCidr() {
        return cidr;
    }

    public void setCidr(String cidr) {
        this.cidr = cidr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getProviders() {
        return providers;
    }

    public void setProviders(Set<String> providers) {
        this.providers = providers;
    }

    public int getIpsServed() {
        return ipsServed;
    }

    // Note that 'addAssociatedIp' already updates the ipsServed, be careful using this method.
    public void setIpsServed(int ipsServed) {
        this.ipsServed = ipsServed;
    }

    public Queue<String> getFreedIps() {
        return freedIps;
    }

    public void setFreedIps(Queue<String> freedIps) {
        this.freedIps = freedIps;
    }

    public List<String> getComputeIps() {
        return computeIps;
    }

    public void setComputeIps(List<String> computeIps) {
        this.computeIps = computeIps;
    }

    @Override
    public FederatedResourceType getType() {
        return FederatedResourceType.FEDERATED_NETWORK;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FederatedNetworkOrder that = (FederatedNetworkOrder) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
