package cloud.fogbow.fns.api.parameters;

public class Compute {
    // This is the class that represents a parameter for requests that will not actually become an order at the
    // FNS, but just at the underlying RAS. Therefore, it does not need to implement the OrderApiParameter interface.
    private String federatedNetworkId;
    private org.fogbowcloud.ras.api.parameters.Compute compute;

    public String getFederatedNetworkId() {
        return federatedNetworkId;
    }

    public org.fogbowcloud.ras.api.parameters.Compute getCompute() {
        return compute;
    }

    public void setCompute(org.fogbowcloud.ras.api.parameters.Compute compute) {
        this.compute = compute;
    }
}
