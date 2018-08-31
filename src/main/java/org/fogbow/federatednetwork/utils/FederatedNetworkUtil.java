package org.fogbow.federatednetwork.utils;

import org.apache.commons.net.util.SubnetUtils;
import org.fogbow.federatednetwork.exceptions.InvalidCidrException;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

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

    /**
     * This method must be used only for recovery from database
     * @param federatedNetwork {@link FederatedNetworkOrder}
     */
    public static void fillFreedIpsList(FederatedNetworkOrder federatedNetwork) throws InvalidCidrException, SubnetAddressesCapacityReachedException {
        SubnetUtils.SubnetInfo subnetInfo = getSubnetInfo(federatedNetwork.getCidrNotation());
        int tempIpsServed = 1;
        Queue<String> freedIps = new LinkedList();
        for (; tempIpsServed < federatedNetwork.getIpsServed(); tempIpsServed ++) {
            int lowAddress = subnetInfo.asInteger(subnetInfo.getLowAddress());
            int candidateIp = lowAddress + tempIpsServed;
            if (!subnetInfo.isInRange(candidateIp)) {
                throw new SubnetAddressesCapacityReachedException(NO_FREE_IPS_MESSAGE);
            }
            String candidateIpAddress = toIpAddress(candidateIp);
            if (!federatedNetwork.getComputesIp().contains(candidateIpAddress)) {
                freedIps.add(candidateIpAddress);
            }
        }
        federatedNetwork.setFreedIps(freedIps);
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
