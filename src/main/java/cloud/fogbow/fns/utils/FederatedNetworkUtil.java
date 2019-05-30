package cloud.fogbow.fns.utils;

import cloud.fogbow.fns.api.http.response.AssignedIp;
import cloud.fogbow.fns.core.exceptions.InvalidCidrException;
import cloud.fogbow.fns.core.exceptions.SubnetAddressesCapacityReachedException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import org.apache.commons.net.util.SubnetUtils;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class FederatedNetworkUtil {

    public static final int FREE_IP_CACHE_MAX_SIZE = 16;
    public static final int RESERVED_IPS = 2;

    public synchronized static void fillCacheOfFreeIps(FederatedNetworkOrder federatedNetwork) throws InvalidCidrException,
            SubnetAddressesCapacityReachedException {
        int index = 1;
        String freeIp = null;
        List<String> usedIPs = getUsedIps(federatedNetwork);
        SubnetUtils.SubnetInfo subnetInfo = getSubnetInfo(federatedNetwork.getCidr());
        int lowAddress = subnetInfo.asInteger(subnetInfo.getLowAddress());
        Queue<String> cache = federatedNetwork.getCacheOfFreeIps();
        while (subnetInfo.isInRange(lowAddress + index) && cache.size() < FREE_IP_CACHE_MAX_SIZE) {
            freeIp = toIpAddress(lowAddress + index);
            if (!usedIPs.contains(freeIp)) {
                federatedNetwork.getCacheOfFreeIps().add(freeIp);
            }
            index++;
        }
        if (cache.isEmpty()) throw new SubnetAddressesCapacityReachedException(federatedNetwork.getCidr());
    }

    private synchronized static List<String> getUsedIps(FederatedNetworkOrder federatedNetworkOrder) {
        List<AssignedIp> assignedIps = federatedNetworkOrder.getAssignedIps();
        List<String> usedIps = new ArrayList<>();
        Iterator<AssignedIp> iterator = assignedIps.iterator();
        while (iterator.hasNext()) {
            usedIps.add(iterator.next().getIp());
        }
        return usedIps;
    }

    public static SubnetUtils.SubnetInfo getSubnetInfo(String cidrNotation) throws InvalidCidrException {
        try {
            return new SubnetUtils(cidrNotation).getInfo();
        } catch (IllegalArgumentException e) {
            throw new InvalidCidrException(cidrNotation, e);
        }
    }

    public static boolean isSubnetValid(SubnetUtils.SubnetInfo subnetInfo) {
        int lowAddress = subnetInfo.asInteger(subnetInfo.getLowAddress());
        int highAddress = subnetInfo.asInteger(subnetInfo.getHighAddress());
        int freeIps = highAddress - lowAddress;
        // This is a closed range, so we need to increment this variable to match this requirement.
        freeIps++;
        return freeIps >= RESERVED_IPS;
    }

    public static HashMap<String, MemberConfigurationState> initializeMemberConfigurationMap(Collection<String> providingMembers) {
        HashMap<String, MemberConfigurationState> providers = new HashMap<>();
        for (String member : providingMembers) {
            providers.put(member, MemberConfigurationState.UNDEFINED);
        }
        return providers;
    }

    private static String toIpAddress(int value) {
        byte[] bytes = BigInteger.valueOf(value).toByteArray();
        try {
            InetAddress address = InetAddress.getByAddress(bytes);
            return address.toString().replaceAll("/", "");
        } catch (UnknownHostException e) {
            return null;
        }
    }
}
