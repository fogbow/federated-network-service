package org.fogbow.federatednetwork.utils;

import org.apache.commons.net.util.SubnetUtils;
import org.fogbow.federatednetwork.MockedFederatedNetworkUnitTests;
import org.fogbow.federatednetwork.exceptions.InvalidCidrException;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbow.federatednetwork.exceptions.UnexpectedException;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.fogbowcloud.ras.core.models.tokens.FederationUserToken;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class FederatedNetworkUtilTest extends MockedFederatedNetworkUnitTests {

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
        FederationUserToken user = Mockito.mock(FederationUserToken.class);
        Set<String> allowedMembers = new HashSet<>();
        Queue<String> freedIps = new LinkedList<>();
        Map<String, String> computesIp = new HashMap<>();
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
        FederationUserToken user = mock(FederationUserToken.class);
        Set<String> allowedMembers = new HashSet<>();
        Queue<String> freedIps = new LinkedList<>();
        Map<String, String> computesIp = new HashMap<>();
        String malformedCidr = "10..0.0/24";
        FederatedNetworkOrder federatedNetwork = new FederatedNetworkOrder(user, MEMBER, MEMBER, malformedCidr,
                "name", allowedMembers, freedIps, computesIp);
        SubnetUtils.SubnetInfo subnetInfo = null;
        //exercise
        try {
            subnetInfo = FederatedNetworkUtil.getSubnetInfo(malformedCidr);
            fail();
        } catch (InvalidCidrException e) {
            //verify
        }
    }

    @Test
    public void testFillCacheOfFreeIpsWithNoFreeIps() {
        // FIXME FNS_TEST
        // Create network
        // Use all ips
        // Call fillCacheOfFreeIps
        // Assert Exception was thrown
    }

    @Test
    public void testFillCacheOfFreeIps() {
        // FIXME FNS_TEST
        // Create network (mask should assure that network has more than FREE_IP_CACHE_MAX_SIZE free ips)
        // Call fillCacheOfFreeIps
        // Assert cache size is FREE_IP_CACHE_MAX_SIZE
    }

    @Test
    public void testIsSubnetValid() {
        // FIXME FNS_TEST
    }

}
