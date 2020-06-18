package cloud.fogbow.fns.utils;

import cloud.fogbow.common.exceptions.InvalidParameterException;
import cloud.fogbow.fns.constants.Messages;
import org.apache.commons.net.util.SubnetUtils;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class FederatedNetworkUtil {

    public static final int FREE_IP_CACHE_MAX_SIZE = 16;
    public static final int RESERVED_IPS = 2;

    public static SubnetUtils.SubnetInfo getSubnetInfo(String cidrNotation) throws InvalidParameterException {
        try {
            return new SubnetUtils(cidrNotation).getInfo();
        } catch (IllegalArgumentException e) {
            throw new InvalidParameterException(String.format(Messages.Exception.INVALID_CIDR_S, cidrNotation));
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

    public static String toIpAddress(int value) {
        byte[] bytes = BigInteger.valueOf(value).toByteArray();
        try {
            InetAddress address = InetAddress.getByAddress(bytes);
            return address.toString().replaceAll("/", "");
        } catch (UnknownHostException e) {
            return null;
        }
    }
}
