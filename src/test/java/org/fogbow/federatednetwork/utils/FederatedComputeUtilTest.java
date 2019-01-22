package org.fogbow.federatednetwork.utils;

import org.fogbow.federatednetwork.api.parameters.Compute;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class FederatedComputeUtilTest {

    private String federatedComputeIp = "fake-federatedComputeIp";
    private String agentPublicIp = "fake-agentPublicIp";
    private String cidr = "fake-cidr";
    private String preSharedKey = "fake-preSharedKey";

    //test case: tests if compute's userData list was incremented by 1, after adding federated network user data.
    @Test
    public void testAddUserData() throws IOException {
        //set up
        Compute fnsCompute = new Compute();
        org.fogbowcloud.ras.api.parameters.Compute rasCompute = new org.fogbowcloud.ras.api.parameters.Compute();
        fnsCompute.setCompute(rasCompute);
        int userDataSize = rasCompute.getUserData() == null ? 0 : rasCompute.getUserData().size();

        //exercise
        FederatedComputeUtil.addUserData(fnsCompute, federatedComputeIp, agentPublicIp, cidr, preSharedKey);

        //verify
        Assert.assertEquals(userDataSize + 1, fnsCompute.getCompute().getUserData().size());
    }
}