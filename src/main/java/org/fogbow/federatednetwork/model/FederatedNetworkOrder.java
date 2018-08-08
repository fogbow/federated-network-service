package org.fogbow.federatednetwork.model;

import org.fogbowcloud.manager.core.models.tokens.FederationUser;

import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public class FederatedNetworkOrder extends FederatedOrder {

    private String cidrNotation;
    private String label;
    private Set<String> allowedMembers;

    private int ipsServed = 1;
    private Queue<String> freedIps;
    private List<String> computesIp;

    public FederatedNetworkOrder(String id, FederationUser federationUser, String cidrNotation, String label,
                                 Set<String> allowedMembers, int ipsServed, Queue<String> freedIps, List<String> computesIp) {
        super(id, federationUser);
        this.cidrNotation = cidrNotation;
        this.label = label;
        this.allowedMembers = allowedMembers;
        this.ipsServed = ipsServed;
        this.freedIps = freedIps;
        this.computesIp = computesIp;
    }

    public FederatedNetworkOrder(FederationUser federationUser, String cidrNotation, String label,
                                 Set<String> allowedMembers, int ipsServed, Queue<String> freedIps, List<String> computesIp) {
        super(String.valueOf(UUID.randomUUID()), federationUser);
        this.cidrNotation = cidrNotation;
        this.label = label;
        this.allowedMembers = allowedMembers;
        this.ipsServed = ipsServed;
        this.freedIps = freedIps;
        this.computesIp = computesIp;
    }

    @Override
    public FederatedResourceType getType() {
        return FederatedResourceType.FEDERATED_NETWORK;
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
}
