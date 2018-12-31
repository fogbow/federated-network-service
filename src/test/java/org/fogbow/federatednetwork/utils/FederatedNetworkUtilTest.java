package org.fogbow.federatednetwork.utils;

import org.apache.commons.net.util.SubnetUtils;
import org.fogbow.federatednetwork.BaseUnitTest;
import org.fogbow.federatednetwork.exceptions.InvalidCidrException;
import org.fogbow.federatednetwork.exceptions.SubnetAddressesCapacityReachedException;
import org.fogbow.federatednetwork.exceptions.UnexpectedException;
import org.fogbow.federatednetwork.model.FederatedNetworkOrder;
import org.fogbowcloud.ras.core.models.tokens.FederationUserToken;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.fail;
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
    public void testGetFreeIp() throws SubnetAddressesCapacityReachedException, InvalidCidrException, SQLException, UnexpectedException {
        //set up
        FederationUserToken user = mock(FederationUserToken.class);
        Set<String> allowedMembers = new HashSet<>();
        Queue<String> freedIps = new LinkedList<>();
        Map<String, String> computesIp = new HashMap<>();
        String cidr = "10.0.0.0/24";
        FederatedNetworkOrder federatedNetwork = spy(new FederatedNetworkOrder(user, MEMBER, MEMBER, cidr,
                "name", allowedMembers, freedIps, computesIp));
        doNothing().when(federatedNetwork).addAssociatedIp(anyString(), anyString());
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

    //test case: tests that if ips served is higher than the network mask allows, it must throw an exception, since this ip will be in a different network
    @Test
    public void testNetworkOverflow() throws InvalidCidrException, SQLException {
        //set up
    }

    //test case: tests that if ips served is negative, it must throw an exception, since this ip will be in a different network
    @Test
    public void testNetworkUnderflow() throws InvalidCidrException, SQLException {
        //set up
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

}
