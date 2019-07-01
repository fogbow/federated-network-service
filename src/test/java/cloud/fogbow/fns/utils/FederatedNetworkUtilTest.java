package cloud.fogbow.fns.utils;

import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.fns.MockedFederatedNetworkUnitTests;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.fns.api.http.response.AssignedIp;
import cloud.fogbow.fns.core.exceptions.InvalidCidrException;
import cloud.fogbow.fns.core.exceptions.SubnetAddressesCapacityReachedException;
import cloud.fogbow.fns.core.model.FederatedNetworkOrder;
import cloud.fogbow.fns.core.model.MemberConfigurationState;
import org.apache.commons.net.util.SubnetUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class FederatedNetworkUtilTest extends MockedFederatedNetworkUnitTests {
    public static final int MAX_CIDR_SUFFIX = 32;
    Properties properties;

    private final String MEMBER = "fake-member";

    @Before
    public void setUp() {
        properties = super.setProperties();
    }

    //test case: Tests if networks are correctly returned, to a given ips served amount.
    @Test
    public void testGetFreeIp() throws SubnetAddressesCapacityReachedException, InvalidCidrException, UnexpectedException {
        //set up
        SystemUser user = Mockito.mock(SystemUser.class);
        HashMap<String, MemberConfigurationState> allowedMembers = new HashMap<>();
        Queue<String> freedIps = new LinkedList<>();
        ArrayList<AssignedIp> computesIp = new ArrayList<>();
        String cidr = "10.0.0.0/24";
        FederatedNetworkOrder federatedNetwork = Mockito.spy(new FederatedNetworkOrder(user, MEMBER, MEMBER, cidr,
                "name", allowedMembers, freedIps, computesIp));

        //exercise
        String freeIp = federatedNetwork.getFreeIp();
        //verify
        Assert.assertEquals("10.0.0.2", freeIp);

        //exercise
        freeIp = federatedNetwork.getFreeIp();
        //verify
        Assert.assertEquals("10.0.0.3", freeIp);

        //exercise
        freeIp = federatedNetwork.getFreeIp();
        //verify
        Assert.assertEquals("10.0.0.4", freeIp);
    }

    //test case: if a malformed cidr was given, then, we should throw an exception, warning the cause
    @Test
    public void testInvalidNetworkCidr() {
        //set up
        String malformedCidr = "10..0.0/24";
        //exercise
        try {
            SubnetUtils.SubnetInfo subnetInfo = FederatedNetworkUtil.getSubnetInfo(malformedCidr);
            fail();
        } catch (InvalidCidrException e) {
            //verify
        }
    }

    //test case: if a network is already filled in, it should throw an exception when trying to fill cache of free ips
    @Test
    public void testFillCacheOfFreeIpsWithNoFreeIps() throws InvalidCidrException, UnexpectedException,
            SubnetAddressesCapacityReachedException {
        //set up
        mockOnlyDatabase();
        SystemUser user = mock(SystemUser.class);
        HashMap<String, MemberConfigurationState> allowedMembers = new HashMap<>();
        Queue<String> freedIps = new LinkedList<>();
        ArrayList<AssignedIp> computesIp = new ArrayList<>();
        int mask = getMaskForCacheSize();

        String cidr = "10.0.0.0/" + mask;
        FederatedNetworkOrder federatedNetwork = new FederatedNetworkOrder(user, MEMBER, MEMBER, cidr,
                "name", allowedMembers, freedIps, computesIp);

        //exercise
        fillInFederatedNetwork(federatedNetwork, mask);
        try {
            federatedNetwork.fillCacheOfFreeIps();
            //verify()
            fail();
        } catch (SubnetAddressesCapacityReachedException e) {
        }
    }

    //test case: when calling fillCacheOfFreeIps, this should fill queue with FREE_IP_CACHE_MAX_SIZE elements
    @Test
    public void testFillCacheOfFreeIps() throws SubnetAddressesCapacityReachedException, InvalidCidrException {
        //set up
        mockOnlyDatabase();
        SystemUser user = mock(SystemUser.class);
        HashMap<String, MemberConfigurationState> allowedMembers = new HashMap<>();
        Queue<String> freedIps = new LinkedList<>();
        ArrayList<AssignedIp> computesIp = new ArrayList<>();
        int mask = getMaskForCacheSize();

        String cidr = "10.0.0.0/" + mask;
        FederatedNetworkOrder federatedNetwork = new FederatedNetworkOrder(user, MEMBER, MEMBER, cidr,
                "name", allowedMembers, freedIps, computesIp);
        //exercise
        federatedNetwork.fillCacheOfFreeIps();
        //verify
        Assert.assertEquals(FederatedNetworkUtil.FREE_IP_CACHE_MAX_SIZE, federatedNetwork.getCacheOfFreeIps().size());
    }

    //test case: networks should have at least 2 free ips
    @Test
    public void testIsSubnetValid() throws InvalidCidrException {
        String cidr = "10.0.0.0/";
        for (int freeBits = 0; freeBits < MAX_CIDR_SUFFIX; freeBits++) {
            //set up
            double ipsInMask = Math.pow(2, freeBits);
            double freeIps = (ipsInMask < FederatedNetworkUtil.RESERVED_IPS) ?
                    ipsInMask : (ipsInMask - FederatedNetworkUtil.RESERVED_IPS);
            SubnetUtils.SubnetInfo subnetInfo = FederatedNetworkUtil.getSubnetInfo(cidr + (MAX_CIDR_SUFFIX - freeBits));
            //verify
            if (freeIps >= FederatedNetworkUtil.RESERVED_IPS) {
                Assert.assertTrue(FederatedNetworkUtil.isSubnetValid(subnetInfo));
            } else {
                Assert.assertFalse(FederatedNetworkUtil.isSubnetValid(subnetInfo));
            }
        }
    }

    private int getMaskForCacheSize() {
        int currentFreeIps = 1;
        for (int i = MAX_CIDR_SUFFIX; i > 0; i--) {
            if (currentFreeIps > FederatedNetworkUtil.FREE_IP_CACHE_MAX_SIZE) {
                return i;
            }
            currentFreeIps = currentFreeIps * 2;
        }
        return 0;
    }

    private void fillInFederatedNetwork(FederatedNetworkOrder federatedNetwork, int mask) throws InvalidCidrException,
            UnexpectedException, SubnetAddressesCapacityReachedException {
        double freeIps = Math.pow(2, MAX_CIDR_SUFFIX - mask) - FederatedNetworkUtil.RESERVED_IPS;
        // getFreeIp will give the second valid ip, because the first one is set to the agent,
        // so we need to decrement our freeIps variable.
        freeIps -= 1;
        String computeId = "id-";
        for (int i = 0; i < freeIps; i++) {
            String ip = federatedNetwork.getFreeIp();
            federatedNetwork.addAssociatedIp(computeId + i, ip);
        }
    }
}
