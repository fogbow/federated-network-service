package cloud.fogbow.fns.core.model;

import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.common.util.GsonHolder;
import cloud.fogbow.common.util.SerializedEntityHolder;
import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.fns.api.http.response.AssignedIp;
import cloud.fogbow.fns.api.http.response.FederatedNetworkInstance;
import cloud.fogbow.fns.constants.Messages;
import cloud.fogbow.fns.core.ComputeIdToFederatedNetworkIdMapping;
import cloud.fogbow.fns.core.datastore.DatabaseManager;
import cloud.fogbow.fns.core.datastore.StableStorage;
import cloud.fogbow.fns.core.exceptions.InvalidCidrException;
import cloud.fogbow.fns.core.exceptions.SubnetAddressesCapacityReachedException;
import cloud.fogbow.fns.utils.FederatedNetworkUtil;
import org.apache.commons.net.util.SubnetUtils;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "federated_network_table")
public class FederatedNetworkOrder implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final int FIELDS_MAX_SIZE = 255;
    public static final int FREE_IP_CACHE_MAX_SIZE = 16;

    @Column
    @Id
    private String id;

    @Column
    @Enumerated(EnumType.STRING)
    private OrderState orderState;

    @Transient
    private SystemUser systemUser;

    @Column
    @Size(max = SystemUser.SERIALIZED_SYSTEM_USER_MAX_SIZE)
    private String serializedSystemUser;

    @Column
    @Size(max = FIELDS_MAX_SIZE)
    private String userId;

    @Column
    @Size(max = FIELDS_MAX_SIZE)
    private String identityProviderId;

    @Column
    private String requester;

    @Column
    private String provider;

    @Column
    private String cidr;

    @Column
    private String name;

    @Column
    private Integer vlanId;

    @Column
    @Enumerated(EnumType.STRING)
    private ConfigurationMode configurationMode;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn
    @Column
    @CollectionTable
    private Map<String, MemberConfigurationState> providers;

    @Embedded
    @AttributeOverrides(value = {
        @AttributeOverride(name = "compute_id", column = @Column(name = "compute_id")),
        @AttributeOverride(name = "ip", column = @Column(name = "ip"))
    })
    @ElementCollection(fetch = FetchType.EAGER)
    private List<AssignedIp> assignedIps;

    @Column
    private String tst;

    @Transient
    private Queue<String> cacheOfFreeIps;

    public FederatedNetworkOrder() {
        this(String.valueOf(UUID.randomUUID()));
    }

    public FederatedNetworkOrder(String id) {
        this.id = id;
        this.providers = new HashMap<>();
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
                                 String provider, String cidr, String name, HashMap<String, MemberConfigurationState> providers,
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
                                 String cidr, String name, HashMap<String, MemberConfigurationState> providers,
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
        int associatedIpIndex = containsKey(computeId);
        if (associatedIpIndex == -1) {
            throw new IllegalArgumentException();
        }
        this.assignedIps.remove(associatedIpIndex);
        StableStorage databaseManager = DatabaseManager.getInstance();
        databaseManager.put(this);
        ComputeIdToFederatedNetworkIdMapping.getInstance().remove(computeId);
    }

    private int containsKey(String computeId) {
        Iterator<AssignedIp> iterator = this.assignedIps.iterator();
        int associatedIpIndex = 0;
        while (iterator.hasNext()) {
            AssignedIp assignedIp = iterator.next();
            if (assignedIp.getComputeId().equals(computeId)) {
                return associatedIpIndex;
            }
            associatedIpIndex++;
        }
        return -1;
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
            this.fillCacheOfFreeIps();
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
            case PARTIALLY_FULFILLED:
                return InstanceState.PARTIALLY_FULFILLED;
            case SPAWNING:
                return InstanceState.SPAWNING;
            case CLOSED:
                return InstanceState.CLOSED;
            default:
                return null;
        }
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
        InstanceState instanceState = this.orderState == OrderState.FULFILLED ? InstanceState.READY : InstanceState.FAILED;
        FederatedNetworkInstance instance = new FederatedNetworkInstance(this.id, this.name, this.requester, this.provider,
                this.cidr, this.providers.keySet(), this.assignedIps, instanceState);
        return instance;
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

    public Map<String, MemberConfigurationState> getProviders() {
        return providers;
    }

    public void setProviders(Map<String, MemberConfigurationState> providers) {
        this.providers = providers;
    }

    public Queue<String> getCacheOfFreeIps() {
        return cacheOfFreeIps;
    }

    public void setCacheOfFreeIps(Queue<String> cacheOfFreeIps) {
        this.cacheOfFreeIps = cacheOfFreeIps;
    }

    // NOTE(pauloewerton): Used for tests only
    public List<AssignedIp> getAssignedIps() {
        return assignedIps;
    }

    public void setAssignedIps(ArrayList<AssignedIp> assignedIps) {
        this.assignedIps = assignedIps;
    }

    public ConfigurationMode getConfigurationMode() {
        return configurationMode;
    }

    public void setConfigurationMode(ConfigurationMode configurationMode) {
        this.configurationMode = configurationMode;
    }

    public int getVlanId() {
        return this.configurationMode == ConfigurationMode.DFNS ? this.vlanId : -1;
    }

    public void setVlanId(int vlanId) {
        this.vlanId = vlanId;
    }

    private String getSerializedSystemUser() {
        return this.serializedSystemUser;
    }

    private void setSerializedSystemUser(String serializedSystemUser) {
        this.serializedSystemUser = serializedSystemUser;
    }

    private void setUserId(String userId) {
        this.userId = userId;
    }

    private void setIdentityProviderId(String identityProviderId) {
        this.identityProviderId = identityProviderId;
    }

    public synchronized boolean isAssignedIpsEmpty() {
        return this.assignedIps.isEmpty();
    }

    // Cannot be called at @PrePersist because the transient field systemUser is set to null at this stage
    // Instead, the systemUser is explicitly serialized before being save by RecoveryService.save().
    public void serializeSystemUser() {
        SerializedEntityHolder<SystemUser> serializedSystemUserHolder = new SerializedEntityHolder<>(this.getSystemUser());
        this.setSerializedSystemUser(GsonHolder.getInstance().toJson(serializedSystemUserHolder));
        this.setUserId(this.getSystemUser().getId());
        this.setIdentityProviderId(this.getSystemUser().getIdentityProviderId());
    }

    @PostLoad
    private void deserializeSystemUser() throws UnexpectedException {
        try {
            SerializedEntityHolder serializedSystemUserHolder = GsonHolder.getInstance().fromJson(
                    this.getSerializedSystemUser(), SerializedEntityHolder.class);
            this.setSystemUser((SystemUser) serializedSystemUserHolder.getSerializedEntity());
        } catch(ClassNotFoundException exception) {
            throw new UnexpectedException(Messages.Exception.UNABLE_TO_DESERIALIZE_SYSTEM_USER);
        }
    }

    public synchronized void fillCacheOfFreeIps() throws InvalidCidrException,
            SubnetAddressesCapacityReachedException {
        int index = 1;
        String freeIp = null;
        List<String> usedIPs = this.getUsedIps();
        SubnetUtils.SubnetInfo subnetInfo = FederatedNetworkUtil.getSubnetInfo(this.getCidr());
        int lowAddress = subnetInfo.asInteger(subnetInfo.getLowAddress());
        Queue<String> cache = this.getCacheOfFreeIps();

        while (subnetInfo.isInRange(lowAddress + index) && cache.size() < FREE_IP_CACHE_MAX_SIZE) {
            freeIp = FederatedNetworkUtil.toIpAddress(lowAddress + index);
            if (!usedIPs.contains(freeIp)) {
                this.getCacheOfFreeIps().add(freeIp);
            }
            index++;
        }

        if (cache.isEmpty()) throw new SubnetAddressesCapacityReachedException(this.getCidr());
    }

    private synchronized List<String> getUsedIps() {
        List<AssignedIp> assignedIps = this.assignedIps;
        List<String> usedIps = new ArrayList<>();
        Iterator<AssignedIp> iterator = assignedIps.iterator();

        while (iterator.hasNext()) {
            usedIps.add(iterator.next().getIp());
        }

        return usedIps;
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
