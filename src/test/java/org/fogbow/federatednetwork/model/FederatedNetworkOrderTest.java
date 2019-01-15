package org.fogbow.federatednetwork.model;

import org.fogbow.federatednetwork.MockedFederatedNetworkUnitTests;
import org.fogbow.federatednetwork.exceptions.FogbowFnsException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FederatedNetworkOrderTest extends MockedFederatedNetworkUnitTests {
    private static final String FAKE_IP = "fake-ip";
    private static final String FAKE_COMPUTE_ID = "fake-compute-id";

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
}

