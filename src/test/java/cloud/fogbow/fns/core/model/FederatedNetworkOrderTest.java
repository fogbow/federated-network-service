package cloud.fogbow.fns.core.model;

import cloud.fogbow.common.models.linkedlists.SynchronizedDoublyLinkedList;
import cloud.fogbow.fns.MockedFederatedNetworkUnitTests;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.models.SystemUser;
import cloud.fogbow.fns.api.http.response.AssignedIp;
import cloud.fogbow.fns.core.exceptions.InvalidCidrException;
import cloud.fogbow.fns.core.exceptions.SubnetAddressesCapacityReachedException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FederatedNetworkOrderTest extends MockedFederatedNetworkUnitTests {

    private static final String FAKE_IP = "10.0.1.4";
    private static final String FAKE_COMPUTE_ID = "fake-compute-id";
    private static final String FAKE_PROVIDER_ID = "fake-provider-id";
    public static final String CIDR_EXAMPLE = "10.0.0.0/29";

    @Test
    public void testAddAssociatedIp() throws UnexpectedException {
        // setup
        super.mockSingletons();
        ArrayList<AssignedIp> fakeAssociatedIps;
        fakeAssociatedIps = new ArrayList<>();

        FederatedNetworkOrder fakeFederatedNetworkOrder = new FederatedNetworkOrder();
        fakeFederatedNetworkOrder.setAssignedIps(fakeAssociatedIps);

        // exercise
        fakeFederatedNetworkOrder.addAssociatedIp(new AssignedIp(FAKE_COMPUTE_ID, FAKE_PROVIDER_ID, FAKE_IP));

        // verify
        List<AssignedIp> fednetAssociatedIps = fakeFederatedNetworkOrder.getAssignedIps();
        assertEquals(1, fednetAssociatedIps.size());
        assertTrue(fednetAssociatedIps.get(0).getComputeId().equals(FAKE_COMPUTE_ID));
        assertTrue(fednetAssociatedIps.get(0).getIp().equals(FAKE_IP));
    }

    @Test
    public void testRemoveAssociatedIp() throws UnexpectedException {
        // setup
        super.mockSingletons();
        ArrayList<AssignedIp> fakeAssociatedIps;
        fakeAssociatedIps = new ArrayList<>();
        fakeAssociatedIps.add(new AssignedIp(FAKE_COMPUTE_ID, FAKE_PROVIDER_ID, FAKE_IP));

        FederatedNetworkOrder fakeFederatedNetworkOrder = new FederatedNetworkOrder();
        fakeFederatedNetworkOrder.setAssignedIps(fakeAssociatedIps);

        // exercise
        fakeFederatedNetworkOrder.removeAssociatedIp(FAKE_COMPUTE_ID);

        // verify
        List<AssignedIp> fednetAssociatedIps = fakeFederatedNetworkOrder.getAssignedIps();
        assertEquals(0, fednetAssociatedIps.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveAssociatedIpWithNoAssociatedIps() throws UnexpectedException {
        // setup
        super.mockSingletons();
        ArrayList<AssignedIp> fakeAssociatedIps;
        fakeAssociatedIps = new ArrayList<>();

        FederatedNetworkOrder fakeFederatedNetworkOrder = new FederatedNetworkOrder();
        fakeFederatedNetworkOrder.setAssignedIps(fakeAssociatedIps);

        // exercise
        fakeFederatedNetworkOrder.removeAssociatedIp(FAKE_COMPUTE_ID);
    }

    //test case: tests that if ips served is higher than the network mask allows, it must throw an exception, since this ip will be in a different network
    @Test
    public void testGetIpForNetworkWithNoFreeIps() throws InvalidCidrException, UnexpectedException, SubnetAddressesCapacityReachedException {
        //set up
        mockDatabase(new SynchronizedDoublyLinkedList<>());
        FederatedNetworkOrder federatedNetwork = createFederatedNetworkOrder(CIDR_EXAMPLE);

        // use the whole network
        int freeIps = (int) (Math.pow(2, 3) - 3); // 2^freeBits - bitsForNetBroadcastAndAgent
        for (int i = 0; i < freeIps; i++) {
            // associating an IP address to a VM is a two step process in the order
            String freeIp = federatedNetwork.getFreeIp();
            String uniqueComputeId = FAKE_COMPUTE_ID + i;
            federatedNetwork.addAssociatedIp(new AssignedIp(uniqueComputeId, FAKE_PROVIDER_ID, FAKE_IP));
        }

        //exercise
        try {
            federatedNetwork.getFreeIp();
        } catch (SubnetAddressesCapacityReachedException e) {
            //verify
        }
    }

    @Test
    public void testAddingAndRemovingAssociatedIps() throws UnexpectedException {
        // set up
        mockDatabase(new SynchronizedDoublyLinkedList<>());
        FederatedNetworkOrder federatedNetwork = createFederatedNetworkOrder(CIDR_EXAMPLE);

        String fakeComputeId = "fake-compute-id";
        String fakeIpAddress = "10.0.1.4";

        Assert.assertEquals(null, federatedNetwork.getAssociatedIp(fakeComputeId));

        // exercise
        federatedNetwork.addAssociatedIp(new AssignedIp(fakeComputeId, FAKE_PROVIDER_ID, FAKE_IP));

        // verify
        Assert.assertEquals(fakeIpAddress, federatedNetwork.getAssociatedIp(fakeComputeId));

        // exercise
        federatedNetwork.removeAssociatedIp(fakeComputeId);

        // verify
        Assert.assertEquals(null, federatedNetwork.getAssociatedIp(fakeComputeId));
    }

    private FederatedNetworkOrder createFederatedNetworkOrder(String cidr) {
        SystemUser systemUser = Mockito.mock(SystemUser.class);
        Queue<String> cacheOfFreeIps = new LinkedList<>();
        ArrayList<AssignedIp> computeIdsAndIps = new ArrayList<>();
        return new FederatedNetworkOrder(systemUser, null, null, cidr,
                null, cacheOfFreeIps, computeIdsAndIps, "vanilla");
    }
}