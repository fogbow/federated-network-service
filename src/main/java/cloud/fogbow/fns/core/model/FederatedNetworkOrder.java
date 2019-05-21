package cloud.fogbow.fns.core.model;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.fns.api.http.response.AssignedIp;
import cloud.fogbow.fns.api.http.response.FederatedNetworkInstance;
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

    // TODO: check if this is correct; we need to save the systemUser.
    @Transient
    private SystemUser systemUser;

    @Column
    private String requester;

    @Column
    private String provider;

    @Column
    private String cidr;

    @Column
    private String name;

//    @ElementCollection(fetch = FetchType.EAGER)
    @Column
    private ArrayList<AssignedIp> assignedIps;

    @ElementCollection(targetClass = String.class)
    @CollectionTable(name = "federated_network_allowed_members")
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<String> providers;

    @Transient
    private Queue<String> cacheOfFreeIps;

    public FederatedNetworkOrder() {
        this(String.valueOf(UUID.randomUUID()));
    }

    public FederatedNetworkOrder(String id) {
        this.id = id;
        this.providers = new HashSet<>();
        this.cacheOfFreeIps = new LinkedList<>();
        this.assignedIps = new ArrayList<>();
    }

    public FederatedNetworkOrder(SystemUser systemUser, String requester, String provider) {
        this();
        this.id = String.valueOf(UUID.randomUUID());
        this.systemUser = systemUser;
        this.requester = requester;
        this.provider = provider;
    }

    /**
     * Creating Order with predefined Id.
     */
    public FederatedNetworkOrder(String id, SystemUser systemUser, String requester, String provider) {
        this(id);
        this.systemUser = systemUser;
        this.requester = requester;
        this.provider = provider;
    }

    public FederatedNetworkOrder(String id, SystemUser systemUser, String requester,
                                 String provider, String cidr, String name, Set<String> providers,
                                 Queue<String> cacheOfFreeIps, ArrayList<AssignedIp> assignedIps, OrderState orderState) {
        this(id, systemUser, requester, provider);
        this.cidr = cidr;
        this.name = name;
        this.providers = providers;
        this.cacheOfFreeIps = cacheOfFreeIps;
        this.assignedIps = assignedIps;
        this.orderState = orderState;
    }

    public FederatedNetworkOrder(SystemUser systemUser, String requester, String provider,
                                 String cidr, String name, Set<String> providers,
                                 Queue<String> cacheOfFreeIps, ArrayList<AssignedIp> assignedIps) {
        this(systemUser, requester, provider);
        this.cidr = cidr;
        this.name = name;
        this.providers = providers;
        this.cacheOfFreeIps = cacheOfFreeIps;
        this.assignedIps = assignedIps;
    }

    public synchronized void addAssociatedIp(String computeId, String ipToBeAttached) throws UnexpectedException {
        this.assignedIps.add(new AssignedIp(computeId, ipToBeAttached));
        StableStorage databaseManager = DatabaseManager.getInstance();
        databaseManager.put(this);
        ComputeIdToFederatedNetworkIdMapping.getInstance().put(computeId, this.getId());
    }

    public synchronized void removeAssociatedIp(String computeId) throws UnexpectedException {
        if (!containsKey(computeId)) {
            throw new IllegalArgumentException();
        }
        this.assignedIps.remove(computeId);
        StableStorage databaseManager = DatabaseManager.getInstance();
        databaseManager.put(this);
        ComputeIdToFederatedNetworkIdMapping.getInstance().remove(computeId);
    }

    private boolean containsKey(String computeId) {
        Iterator<AssignedIp> iterator = this.assignedIps.iterator();
        while (iterator.hasNext()) {
            AssignedIp assignedIp = iterator.next();
            if (assignedIp.getComputeId().equals(computeId)) {
                return true;
            }
        }
        return false;
    }

    public synchronized String getAssociatedIp(String computeId) {
        Iterator<AssignedIp> iterator = this.assignedIps.iterator();
        while (iterator.hasNext()) {
            AssignedIp assignedIp = iterator.next();
            if (assignedIp.getComputeId().equals(computeId)) {
                return assignedIp.getIp();
            }
        }
        return null;
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
        switch (this.getOrderState()) {
            case OPEN:
                return InstanceState.OPEN;
            case FAILED:
                return InstanceState.FAILED;
            case FULFILLED:
                return InstanceState.READY;
            default:
                return null;
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

    public synchronized void setOrderState(OrderState state) throws UnexpectedException {
        this.orderState = state;
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        databaseManager.put(this);
    }

    public FederatedNetworkInstance getInstance() {
        return new FederatedNetworkInstance(this.id, this.name, this.requester, this.provider,
                this.cidr, this.providers, this.getAssignedIps(),
                (this.orderState == OrderState.FULFILLED ? InstanceState.READY : InstanceState.FAILED));
    }

    public SystemUser getSystemUser() {
        return systemUser;
    }

    public void setSystemUser(SystemUser systemUser) {
        this.systemUser = systemUser;
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

    public boolean isProviderLocal(String localMemberId) {
        return this.provider.equals(localMemberId);
    }

    public boolean isRequesterRemote(String localMemberId) {
        return !this.requester.equals(localMemberId);
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

    public List<AssignedIp> getAssignedIps() {
        return assignedIps;
    }

    public void setAssignedIps(ArrayList<AssignedIp> assignedIps) {
        this.assignedIps = assignedIps;
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
        return "Order [id=" + this.id + ", orderState=" + this.orderState + ", requester=" +
                this.requester + ", provider=" + this.provider + "]";
    }
}
