package cloud.fogbow.fns.core.model;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.models.FederationUser;
import cloud.fogbow.fns.core.datastore.DatabaseManager;
import cloud.fogbow.fns.core.datastore.StableStorage;
import cloud.fogbow.fns.core.exceptions.InvalidCidrException;
import cloud.fogbow.fns.core.exceptions.SubnetAddressesCapacityReachedException;
import cloud.fogbow.fns.utils.FederatedNetworkUtil;
import cloud.fogbow.fns.core.ComputeIdToFederatedNetworkIdMapping;
import cloud.fogbow.fns.constants.Messages;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "federated_network_table")
public class FederatedNetworkOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column
    @Id
    private String id;

    @Column
    @Enumerated(EnumType.STRING)
    private OrderState orderState;

    //@JoinColumn
    //@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @Transient
    private FederationUser user;

    @Column
    private String requestingMember;

    @Column
    private String providingMember;

    @Column
    private String cidr;

    @Column
    private String name;

    @ElementCollection(targetClass = String.class)
    @CollectionTable(name = "federated_network_allowed_members")
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<String> providers;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn
    @Column
    private Map<String, String> computeIdsAndIps;

    @Transient
    private Queue<String> cacheOfFreeIps;

    public FederatedNetworkOrder() {
        this(String.valueOf(UUID.randomUUID()));
    }

    public FederatedNetworkOrder(String id) {
        this.id = id;
        this.providers = new HashSet<>();
        this.cacheOfFreeIps = new LinkedList<>();
        this.computeIdsAndIps = new HashMap<>();
    }

    public FederatedNetworkOrder(FederationUser user, String requestingMember, String providingMember) {
        this();
        this.id = String.valueOf(UUID.randomUUID());
        this.user = user;
        this.requestingMember = requestingMember;
        this.providingMember = providingMember;
    }

    /**
     * Creating Order with predefined Id.
     */
    public FederatedNetworkOrder(String id, FederationUser user, String requestingMember, String providingMember) {
        this(id);
        this.user = user;
        this.requestingMember = requestingMember;
        this.providingMember = providingMember;
    }

    public FederatedNetworkOrder(String id, FederationUser federatedUserToken, String requestingMember,
                                 String providingMember, String cidr, String name, Set<String> providers,
                                 Queue<String> cacheOfFreeIps, Map<String, String> computeIdsAndIps) {
        this(id, federatedUserToken, requestingMember, providingMember);
        this.cidr = cidr;
        this.name = name;
        this.providers = providers;
        this.cacheOfFreeIps = cacheOfFreeIps;
        this.computeIdsAndIps = computeIdsAndIps;
    }

    public FederatedNetworkOrder(FederationUser federatedUserToken, String requestingMember, String providingMember,
                                 String cidr, String name, Set<String> providers,
                                 Queue<String> cacheOfFreeIps, Map<String, String> computeIdsAndIps) {
        this(federatedUserToken, requestingMember, providingMember);
        this.cidr = cidr;
        this.name = name;
        this.providers = providers;
        this.cacheOfFreeIps = cacheOfFreeIps;
        this.computeIdsAndIps = computeIdsAndIps;
    }

    public synchronized void addAssociatedIp(String computeId, String ipToBeAttached) {
        this.computeIdsAndIps.put(computeId, ipToBeAttached);
        StableStorage databaseManager = DatabaseManager.getInstance();
        databaseManager.put(this);
        ComputeIdToFederatedNetworkIdMapping.getInstance().put(computeId, this.getId());
    }

    public synchronized void removeAssociatedIp(String computeId) {
        if (!this.computeIdsAndIps.containsKey(computeId)) {
            throw new IllegalArgumentException();
        }
        this.computeIdsAndIps.remove(computeId);
        StableStorage databaseManager = DatabaseManager.getInstance();
        databaseManager.put(this);
        ComputeIdToFederatedNetworkIdMapping.getInstance().remove(computeId);
    }

    public synchronized String getAssociatedIp(String computeId) {
        return this.computeIdsAndIps.get(computeId);
    }

    public synchronized String getFreeIp() throws InvalidCidrException, UnexpectedException,
            SubnetAddressesCapacityReachedException {
        String ip = null;
        try {
            ip = this.cacheOfFreeIps.remove();
        } catch (NoSuchElementException e1) {
            fillCacheOfFreeIps();
            try {
                ip = this.cacheOfFreeIps.remove();
            } catch (NoSuchElementException e2) {
                // fillCacheOfFreeIps() throws a SubnetAddressesCapacityReachedException when there are no free
                // IPs left. Thus, it is not expected that the second call to this.cacheOfFreeIps.remove() throws
                // a NoSuchElementException if it ever gets called.
                throw new UnexpectedException(Messages.Exception.UNEXPECTED_EXCEPTION, e2);
            }
        }
        return ip;
    }

    public synchronized InstanceState getInstanceStateFromOrderState() {
        if (this.getOrderState().equals(OrderState.FULFILLED)) {
            return InstanceState.READY;
        } else {
            return InstanceState.FAILED;
        }
    }

    private void fillCacheOfFreeIps() throws InvalidCidrException, SubnetAddressesCapacityReachedException {
        FederatedNetworkUtil.fillCacheOfFreeIps(this);
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

    public FederatedNetworkInstance getInstance() {
        return new FederatedNetworkInstance(this.id, this.name, this.requestingMember, this.providingMember,
                this.cidr, this.providers, this.getComputeIdsAndIps(),
                (this.orderState == OrderState.FULFILLED ? InstanceState.READY : InstanceState.FAILED));
    }

    public FederationUser getUser() {
        return user;
    }

    public void setUser(FederationUser user) {
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

    public boolean isProviderLocal(String localMemberId) {
        return this.providingMember.equals(localMemberId);
    }

    public boolean isRequesterRemote(String localMemberId) {
        return !this.requestingMember.equals(localMemberId);
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

    public Queue<String> getCacheOfFreeIps() {
        return cacheOfFreeIps;
    }

    public void setCacheOfFreeIps(Queue<String> cacheOfFreeIps) {
        this.cacheOfFreeIps = cacheOfFreeIps;
    }

    public Map<String, String> getComputeIdsAndIps() {
        return computeIdsAndIps;
    }

    public void setComputeIdsAndIps(Map<String, String> computeIdsAndIps) {
        this.computeIdsAndIps = computeIdsAndIps;
    }

    public ResourceType getType() {
        return ResourceType.FEDERATED_NETWORK;
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

    @Override
    public String toString() {
        return "Order [id=" + this.id + ", orderState=" + this.orderState + ", requestingMember=" +
                this.requestingMember + ", providingMember=" + this.providingMember + "]";
    }
}
