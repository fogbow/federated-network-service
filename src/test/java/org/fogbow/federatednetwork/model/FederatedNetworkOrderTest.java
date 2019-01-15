package org.fogbow.federatednetwork.model;

import org.fogbow.federatednetwork.MockedFederatedNetworkUnitTests;
import org.fogbow.federatednetwork.constants.SystemConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class FederatedNetworkOrderTest extends MockedFederatedNetworkUnitTests {
    private static final String FAKE_IP = "fake-ip";
    private static final String FAKE_COMPUTE_ID = "fake-compute-id";
    @Test
    public void testAddAssociatedIp(){
        // setup
        super.mockSingletons();
        Map <String, String> fakeAssociatedIps;
        fakeAssociatedIps = new HashMap<>();

        FederatedNetworkOrder fakeFederatedNetworkOrder = new FederatedNetworkOrder();
        fakeFederatedNetworkOrder.setComputeIdsAndIps(fakeAssociatedIps);

        // exercise
        fakeFederatedNetworkOrder.addAssociatedIp(FAKE_COMPUTE_ID, FAKE_IP);

        // verify
        Map fednetAssociatedIps = fakeFederatedNetworkOrder.getComputeIdsAndIps();
        assertEquals(1, fednetAssociatedIps.size());
    }
}
