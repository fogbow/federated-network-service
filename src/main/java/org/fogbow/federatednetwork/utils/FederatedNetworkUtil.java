package org.fogbow.federatednetwork.utils;

import org.apache.commons.net.util.SubnetUtils;
import org.fogbow.federatednetwork.exceptions.InvalidCidrException;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class FederatedNetworkUtil {

    public static final String NO_FREE_IPS_MESSAGE = "Subnet Addresses Capacity Reached, there isn't free IPs to attach";

    public static String getFreeIpForCompute(FederatedNetworkOrder federatedNetwork) throws SubnetAddressesCapacityReachedException, InvalidCidrException {
        String freeIp = null;
        if (federatedNetwork.getFreedIps().isEmpty()) {
            SubnetUtils.SubnetInfo subnetInfo = getSubnetInfo(federatedNetwork.getCidrNotation());
            int lowAddress = subnetInfo.asInteger(subnetInfo.getLowAddress());
            int candidateIpAddress = lowAddress + federatedNetwork.getIpsServed();
            if (!subnetInfo.isInRange(candidateIpAddress)) {
                throw new SubnetAddressesCapacityReachedException(NO_FREE_IPS_MESSAGE);
            }
            freeIp = toIpAddress(candidateIpAddress);
        } else {
            freeIp = federatedNetwork.getFreedIps().element();
        }
        federatedNetwork.addAssociatedIp(freeIp);
        return freeIp;
    }

    public static SubnetUtils.SubnetInfo getSubnetInfo(String cidrNotation) throws InvalidCidrException {
        try {
            return new SubnetUtils(cidrNotation).getInfo();
        } catch (IllegalArgumentException e) {
            throw new InvalidCidrException(cidrNotation);
        }
    }

    public static boolean isSubnetValid(SubnetUtils.SubnetInfo subnetInfo) {
        int lowAddress = subnetInfo.asInteger(subnetInfo.getLowAddress());
        int highAddress = subnetInfo.asInteger(subnetInfo.getHighAddress());
        return highAddress - lowAddress > 1;
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
