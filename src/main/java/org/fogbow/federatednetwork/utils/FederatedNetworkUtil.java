package org.fogbow.federatednetwork.utils;

import org.apache.commons.net.util.SubnetUtils;
import org.fogbow.federatednetwork.constants.Messages;
import org.fogbow.federatednetwork.exceptions.InvalidCidrException;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

public class FederatedNetworkUtil {

    public static String getFreeIpForCompute(FederatedNetworkOrder federatedNetwork) throws
            SubnetAddressesCapacityReachedException, InvalidCidrException, SQLException {
        String freeIp = null;
        if (federatedNetwork.getFreedIps().isEmpty()) {
            SubnetUtils.SubnetInfo subnetInfo = getSubnetInfo(federatedNetwork.getCidr());
            int lowAddress = subnetInfo.asInteger(subnetInfo.getLowAddress());
            int candidateIpAddress = lowAddress + federatedNetwork.getIpsServed();
            if (!subnetInfo.isInRange(candidateIpAddress)) {
                throw new SubnetAddressesCapacityReachedException(Messages.Exception.NO_FREE_IPS_LEFT);
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
        SubnetUtils.SubnetInfo subnetInfo = getSubnetInfo(federatedNetwork.getCidr());
        int tempIpsServed = 1;
        Queue<String> freedIps = new LinkedList();
        for (; tempIpsServed < federatedNetwork.getIpsServed(); tempIpsServed ++) {
            int lowAddress = subnetInfo.asInteger(subnetInfo.getLowAddress());
            int candidateIp = lowAddress + tempIpsServed;
            if (!subnetInfo.isInRange(candidateIp)) {
                throw new SubnetAddressesCapacityReachedException(Messages.Exception.NO_FREE_IPS_LEFT);
            }
            String candidateIpAddress = toIpAddress(candidateIp);
            if (!federatedNetwork.getComputeIps().contains(candidateIpAddress)) {
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
