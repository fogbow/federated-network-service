package org.fogbow.federatednetwork.utils;

import org.apache.commons.net.util.SubnetUtils;
import org.fogbow.federatednetwork.BaseUnitTest;
import org.fogbow.federatednetwork.exceptions.InvalidCidrException;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.fogbow.federatednetwork.model.FederatedUser;
import org.fogbowcloud.manager.core.models.tokens.FederationUserToken;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class FederatedNetworkUtilTest extends BaseUnitTest {

    Properties properties;

    private final String MEMBER = "fake-member";

    @Before
    public void setUp() {
        properties = super.setProperties();
    }

    //test case: Tests if networks are correctly returned, to a given ips served amount.
    @Test
    public void testGetFreeIp() throws SubnetAddressesCapacityReachedException, InvalidCidrException, SQLException {
        //set up
        FederatedUser user = mock(FederatedUser.class);
        Set<String> allowedMembers = new HashSet<>();
        Queue<String> freedIps = new LinkedList<>();
        List<String> computesIp = new ArrayList<>();
        String cidr = "10.0.0.0/24";
        int ipsServed = 1;
        FederatedNetworkOrder federatedNetwork = spy(new FederatedNetworkOrder(user, MEMBER, MEMBER, cidr,
                "label", allowedMembers, ipsServed, freedIps, computesIp));
        doNothing().when(federatedNetwork).addAssociatedIp(anyString());
        //exercise
        String freeIp = FederatedNetworkUtil.getFreeIpForCompute(federatedNetwork);
        //verify
        Assert.assertEquals("10.0.0.2", freeIp);
        //exercise
        federatedNetwork.setIpsServed(++ipsServed);
        freeIp = FederatedNetworkUtil.getFreeIpForCompute(federatedNetwork);
        //verify
        Assert.assertEquals("10.0.0.3", freeIp);
        //exercise
        federatedNetwork.setIpsServed(++ipsServed);
        freeIp = FederatedNetworkUtil.getFreeIpForCompute(federatedNetwork);
        //verify
        Assert.assertEquals("10.0.0.4", freeIp);
    }

    //test case: freedIp will be given, if the queue is not empty, instead of searching a new ip
    @Test
    public void testFreedIpsComesFirst() throws SubnetAddressesCapacityReachedException, InvalidCidrException, SQLException {
        FederatedUser user = mock(FederatedUser.class);
        Set<String> allowedMembers = new HashSet<>();
        Queue<String> freedIps = new LinkedList<>();
        freedIps.add("10.0.0.5");
        List<String> computesIp = new ArrayList<>();
        String cidr = "10.0.0.0/24";
        int ipsServed = 5;
        FederatedNetworkOrder federatedNetwork = spy(new FederatedNetworkOrder(user, MEMBER, MEMBER, cidr,
                "label", allowedMembers, ipsServed, freedIps, computesIp));
        doNothing().when(federatedNetwork).addAssociatedIp(anyString());doNothing().when(federatedNetwork).addAssociatedIp(anyString());
        //exercise
        String freeIp = FederatedNetworkUtil.getFreeIpForCompute(federatedNetwork);
        //verify
        Assert.assertEquals("10.0.0.5", freeIp);
    }

    //test case: tests that if ips served is higher than the network mask allows, it must throw an exception, since this ip will be in a different network
    @Test
    public void testNetworkOverflow() throws InvalidCidrException, SQLException {
        //set up
        FederatedUser user = mock(FederatedUser.class);
        Set<String> allowedMembers = new HashSet<>();
        Queue<String> freedIps = new LinkedList<>();
        List<String> computesIp = new ArrayList<>();
        String cidr = "10.0.0.0/24";
        int ipsServed = 254;
        FederatedNetworkOrder federatedNetwork = new FederatedNetworkOrder(user, MEMBER, MEMBER, cidr,
                "label", allowedMembers, ipsServed, freedIps, computesIp);
        //exercise
        try {
            FederatedNetworkUtil.getFreeIpForCompute(federatedNetwork);
            fail();
        } catch (SubnetAddressesCapacityReachedException e) {
            //verify
        }
    }

    //test case: tests that if ips served is negative, it must throw an exception, since this ip will be in a different network
    @Test
    public void testNetworkUnderflow() throws InvalidCidrException, SQLException {
        //set up
        FederatedUser user = mock(FederatedUser.class);
        Set<String> allowedMembers = new HashSet<>();
        Queue<String> freedIps = new LinkedList<>();
        List<String> computesIp = new ArrayList<>();
        String cidr = "10.0.0.0/24";
        int ipsServed = -1;
        FederatedNetworkOrder federatedNetwork = new FederatedNetworkOrder(user, MEMBER, MEMBER, cidr,
                "label", allowedMembers, ipsServed, freedIps, computesIp);
        //exercise
        try {
            FederatedNetworkUtil.getFreeIpForCompute(federatedNetwork);
            fail();
        } catch (SubnetAddressesCapacityReachedException e) {
            //verify
        }
    }

    //test case: if a malformed cidr was given, then, we should throw an exception, warning the cause
    @Test
    public void testInvalidNetworkCidr() {
        //set up
        FederatedUser user = mock(FederatedUser.class);
        Set<String> allowedMembers = new HashSet<>();
        Queue<String> freedIps = new LinkedList<>();
        List<String> computesIp = new ArrayList<>();
        String malformedCidr = "10..0.0/24";
        int ipsServed = -1;
        FederatedNetworkOrder federatedNetwork = new FederatedNetworkOrder(user, MEMBER, MEMBER, malformedCidr,
                "label", allowedMembers, ipsServed, freedIps, computesIp);
        SubnetUtils.SubnetInfo subnetInfo = null;
        //exercise
        try {
            subnetInfo = FederatedNetworkUtil.getSubnetInfo(malformedCidr);
            fail();
        } catch (InvalidCidrException e) {
            //verify
        }
    }

}
