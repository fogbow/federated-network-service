package cloud.fogbow.fns.utils;

import cloud.fogbow.fns.api.parameters.Compute;
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
        cloud.fogbow.ras.api.parameters.Compute rasCompute = new cloud.fogbow.ras.api.parameters.Compute();
        fnsCompute.setCompute(rasCompute);
        int userDataSize = rasCompute.getUserData() == null ? 0 : rasCompute.getUserData().size();

        //exercise
        FederatedComputeUtil.addUserData(fnsCompute, federatedComputeIp, agentPublicIp, cidr, preSharedKey);

        //verify
        Assert.assertEquals(userDataSize + 1, fnsCompute.getCompute().getUserData().size());
    }
}
