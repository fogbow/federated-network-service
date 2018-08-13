package org.fogbow.federatednetwork.model;

import org.fogbow.federatednetwork.datastore.DatabaseManager;
import org.fogbow.federatednetwork.datastore.StableStorage;
import org.fogbowcloud.manager.core.models.instances.InstanceState;
import org.fogbowcloud.manager.core.models.orders.OrderState;
import org.fogbowcloud.manager.core.models.tokens.FederationUser;

import java.util.*;

public class FederatedNetworkOrder {

    private String id;
    private OrderState orderState;
    private FederationUser federationUser;

    private String cidrNotation;
    private String label;
    private Set<String> allowedMembers;

    private int ipsServed = 1;
    private Queue<String> freedIps;
    private List<String> computesIp;

    private InstanceState cachedInstanceState;

    public FederatedNetworkOrder(String id, FederationUser federationUser, String cidrNotation, String label,
                                 Set<String> allowedMembers, int ipsServed, Queue<String> freedIps, List<String> computesIp) {
        this.id = id;
        this.federationUser = federationUser;
        this.cidrNotation = cidrNotation;
        this.label = label;
        this.allowedMembers = allowedMembers;
        this.ipsServed = ipsServed;
        this.freedIps = freedIps;
        this.computesIp = computesIp;
    }

    public FederatedNetworkOrder(FederationUser federationUser, String cidrNotation, String label,
                                 Set<String> allowedMembers, int ipsServed, Queue<String> freedIps, List<String> computesIp) {
        this.id = String.valueOf(UUID.randomUUID());
        this.federationUser = federationUser;
        this.cidrNotation = cidrNotation;
        this.label = label;
        this.allowedMembers = allowedMembers;
        this.ipsServed = ipsServed;
        this.freedIps = freedIps;
        this.computesIp = computesIp;
    }

    public FederatedNetworkOrder() {
        this.id = String.valueOf(UUID.randomUUID());
        this.allowedMembers = new HashSet<>();
        this.freedIps = new LinkedList<>();
        this.computesIp = new ArrayList<>();
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
        StableStorage databaseManager = new DatabaseManager();
        // Adding or updating in stable storage newly created order
        databaseManager.putFederatedNetwork(this, federationUser);
    }

    public synchronized void removeAssociatedIp(String ipToBeReleased) {
        this.computesIp.remove(ipToBeReleased);
        this.freedIps.add(ipToBeReleased);
        StableStorage databaseManager = new DatabaseManager();
        databaseManager.putFederatedNetwork(this, federationUser);
    }

    public synchronized void addAssociatedIp(String ipToBeAttached){
        if (this.freedIps.contains(ipToBeAttached)) {
            this.freedIps.remove(ipToBeAttached);
        } else {
            this.ipsServed++;
        }
        this.computesIp.add(ipToBeAttached);
        StableStorage databaseManager = new DatabaseManager();
        databaseManager.putFederatedNetwork(this, federationUser);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FederationUser getFederationUser() {
        return federationUser;
    }

    public void setFederationUser(FederationUser federationUser) {
        this.federationUser = federationUser;
    }

    public InstanceState getCachedInstanceState() {
        return cachedInstanceState;
    }

    public void setCachedInstanceState(InstanceState cachedInstanceState) {
        this.cachedInstanceState = cachedInstanceState;
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
