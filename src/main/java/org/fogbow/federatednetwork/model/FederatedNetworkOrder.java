package org.fogbow.federatednetwork.model;

import org.fogbow.federatednetwork.datastore.DatabaseManager;
import org.fogbow.federatednetwork.datastore.StableStorage;
import org.fogbowcloud.manager.core.models.orders.OrderState;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "federated_network_table")
public class FederatedNetworkOrder extends FederatedOrder {

    @Column
    private String cidrNotation;

    @Column
    private String label;

    @ElementCollection(targetClass = String.class)
    @CollectionTable(name="federated_network_allowed_members")
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<String> allowedMembers;

    @Transient
    private int ipsServed = 1;

    @Transient
    private Queue<String> freedIps;

    @ElementCollection(targetClass = String.class)
    @CollectionTable(name="federated_network_computes_ip")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<String> computesIp;

    public FederatedNetworkOrder(String id, FederatedUser federatedUser, String requestingMember,
                                 String providingMember, String cidrNotation, String label, Set<String> allowedMembers,
                                 int ipsServed, Queue<String> freedIps, List<String> computesIp) {
        super(id, federatedUser, requestingMember, providingMember);
        this.cidrNotation = cidrNotation;
        this.label = label;
        this.allowedMembers = allowedMembers;
        this.ipsServed = ipsServed;
        this.freedIps = freedIps;
        this.computesIp = computesIp;
    }

    public FederatedNetworkOrder(FederatedUser federatedUser, String requestingMember, String providingMember,
                                 String cidrNotation, String label, Set<String> allowedMembers, int ipsServed,
                                 Queue<String> freedIps, List<String> computesIp) {
        super(federatedUser, requestingMember, providingMember);
        this.cidrNotation = cidrNotation;
        this.label = label;
        this.allowedMembers = allowedMembers;
        this.ipsServed = ipsServed;
        this.freedIps = freedIps;
        this.computesIp = computesIp;
    }

    public FederatedNetworkOrder() {
        super();
        this.allowedMembers = new HashSet<>();
        this.freedIps = new LinkedList<>();
        this.computesIp = new ArrayList<>();
    }

    public synchronized void setOrderState(OrderState state) {
        super.setOrderState(state);
    }

    public synchronized void removeAssociatedIp(String ipToBeReleased) {
        this.computesIp.remove(ipToBeReleased);
        this.freedIps.add(ipToBeReleased);
        StableStorage databaseManager = DatabaseManager.getInstance();
        databaseManager.put(this);
    }

    public synchronized void addAssociatedIp(String ipToBeAttached){
        if (this.freedIps.contains(ipToBeAttached)) {
            this.freedIps.remove(ipToBeAttached);
        } else {
            this.ipsServed++;
        }
        this.computesIp.add(ipToBeAttached);
        StableStorage databaseManager = DatabaseManager.getInstance();
        databaseManager.put(this);
    }

    public String getCidrNotation() {
        return cidrNotation;
    }

    public void setCidrNotation(String cidrNotation) {
        this.cidrNotation = cidrNotation;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Set<String> getAllowedMembers() {
        return allowedMembers;
    }

    public void setAllowedMembers(Set<String> allowedMembers) {
        this.allowedMembers = allowedMembers;
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

    public List<String> getComputesIp() {
        return computesIp;
    }

    public void setComputesIp(List<String> computesIp) {
        this.computesIp = computesIp;
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
