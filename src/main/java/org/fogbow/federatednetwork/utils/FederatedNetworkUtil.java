package org.fogbow.federatednetwork.utils;

import org.apache.commons.net.util.SubnetUtils;

public class FederatedNetworkUtil {

    public static String getFreeIpForCompute(String federatedNetworkId) {
        throw new UnsupportedOperationException();
    }

    public static String getCidrFromNetworkId(String federatedNetworkId) {
        throw new UnsupportedOperationException();
    }

    public static SubnetUtils.SubnetInfo getSubnetInfo(String cidrNotation) {
        return new SubnetUtils(cidrNotation).getInfo();
    }

    public static boolean isSubnetValid(SubnetUtils.SubnetInfo subnetInfo) {
        int lowAddress = subnetInfo.asInteger(subnetInfo.getLowAddress());
        int highAddress = subnetInfo.asInteger(subnetInfo.getHighAddress());
        return highAddress - lowAddress > 1;
    }
}
