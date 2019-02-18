package cloud.fogbow.fns.core.model;

import cloud.fogbow.fns.MockedFederatedNetworkUnitTests;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.models.FederationUser;
import cloud.fogbow.fns.core.exceptions.InvalidCidrException;
import cloud.fogbow.fns.core.exceptions.SubnetAddressesCapacityReachedException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FederatedNetworkOrderTest extends MockedFederatedNetworkUnitTests {

    private static final String FAKE_IP = "fake-ip";
    private static final String FAKE_COMPUTE_ID = "fake-compute-id";
    public static final String CIDR_EXAMPLE = "10.0.0.0/29";

    @Test
    public void testAddAssociatedIp() {
        // setup
        super.mockSingletons();
        Map<String, String> fakeAssociatedIps;
        fakeAssociatedIps = new HashMap<>();

        FederatedNetworkOrder fakeFederatedNetworkOrder = new FederatedNetworkOrder();
        fakeFederatedNetworkOrder.setComputeIdsAndIps(fakeAssociatedIps);

        // exercise
        fakeFederatedNetworkOrder.addAssociatedIp(FAKE_COMPUTE_ID, FAKE_IP);

        // verify
        Map fednetAssociatedIps = fakeFederatedNetworkOrder.getComputeIdsAndIps();
        assertEquals(1, fednetAssociatedIps.size());
        assertTrue(fednetAssociatedIps.containsKey(FAKE_COMPUTE_ID));
        assertTrue(fednetAssociatedIps.containsValue(FAKE_IP));
    }

    @Test
    public void testRemoveAssociatedIp(){
        // setup
        super.mockSingletons();
        Map<String, String> fakeAssociatedIps;
        fakeAssociatedIps = new HashMap<>();
        fakeAssociatedIps.put(FAKE_COMPUTE_ID, FAKE_IP);

        FederatedNetworkOrder fakeFederatedNetworkOrder = new FederatedNetworkOrder();
        fakeFederatedNetworkOrder.setComputeIdsAndIps(fakeAssociatedIps);

        // exercise
        fakeFederatedNetworkOrder.removeAssociatedIp(FAKE_COMPUTE_ID);

        // verify
        Map fednetAssociatedIps = fakeFederatedNetworkOrder.getComputeIdsAndIps();
        assertEquals(0, fednetAssociatedIps.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveAssociatedIpWithNoAssociatedIps(){
        // setup
        super.mockSingletons();
        Map<String, String> fakeAssociatedIps;
        fakeAssociatedIps = new HashMap<>();

        FederatedNetworkOrder fakeFederatedNetworkOrder = new FederatedNetworkOrder();
        fakeFederatedNetworkOrder.setComputeIdsAndIps(fakeAssociatedIps);

        // exercise
        fakeFederatedNetworkOrder.removeAssociatedIp(FAKE_COMPUTE_ID);
    }

    //test case: tests that if ips served is higher than the network mask allows, it must throw an exception, since this ip will be in a different network
    @Test
    public void testGetIpForNetworkWithNoFreeIps() throws InvalidCidrException, UnexpectedException, SubnetAddressesCapacityReachedException {
        //set up
        mockDatabase(new HashMap<>());
        FederatedNetworkOrder federatedNetwork = createFederatedNetworkOrder(CIDR_EXAMPLE);

        // use the whole network
        int freeIps = (int) (Math.pow(2, 3) - 3); // 2^freeBits - bitsForNetBroadcastAndAgent
        for (int i = 0; i < freeIps; i++) {
            // associating an IP address to a VM is a two step process in the order
            String freeIp = federatedNetwork.getFreeIp();
            String uniqueComputeId = FAKE_COMPUTE_ID + i;
            federatedNetwork.addAssociatedIp(uniqueComputeId, freeIp);
        }

        //exercise
        try {
            String freeIp = federatedNetwork.getFreeIp();
            fail();
        } catch (SubnetAddressesCapacityReachedException e) {
            //verify
        }
    }

    @Test
    public void testAddingAndRemovingAssociatedIps() {
        // set up
        mockDatabase(new HashMap<>());
        FederatedNetworkOrder federatedNetwork = createFederatedNetworkOrder(CIDR_EXAMPLE);

        String fakeComputeId = "fake-compute-id";
        String fakeIpAddress = "10.0.1.4";

        Assert.assertEquals(null, federatedNetwork.getAssociatedIp(fakeComputeId));

        // exercise
        federatedNetwork.addAssociatedIp(fakeComputeId, fakeIpAddress);

        // verify
        Assert.assertEquals(fakeIpAddress, federatedNetwork.getAssociatedIp(fakeComputeId));

        // exercise
        federatedNetwork.removeAssociatedIp(fakeComputeId);

        // verify
        Assert.assertEquals(null, federatedNetwork.getAssociatedIp(fakeComputeId));
    }

    private FederatedNetworkOrder createFederatedNetworkOrder(String cidr) {
        FederationUser federationUser = Mockito.mock(FederationUser.class);
        Set<String> providers = new HashSet<>();
        Queue<String> cacheOfFreeIps = new LinkedList<>();
        Map<String, String> computeIdsAndIps = new HashMap<>();
        return new FederatedNetworkOrder(federationUser, null, null, cidr,
                null, providers, cacheOfFreeIps, computeIdsAndIps);
    }
}