package cloud.fogbow.fns.api.parameters;

import cloud.fogbow.fns.constants.ApiDocumentation;
import cloud.fogbow.ras.api.parameters.Compute;
import cloud.fogbow.ras.core.models.UserData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel
public class FederatedCompute {
    // This is the class that represents a parameter for requests that will not actually become an order at the
    // FNS, but just at the underlying RAS. Therefore, it does not need to implement the OrderApiParameter interface.
    @ApiModelProperty(position = 0, example = ApiDocumentation.Model.INSTANCE_ID)
    private String federatedNetworkId;
    @ApiModelProperty(position = 1, required = true)
    private Compute compute;

    public String getFederatedNetworkId() {
        return federatedNetworkId;
    }

    public Compute getCompute() {
        return compute;
    }

    public void setCompute(Compute compute) {
        this.compute = compute;
    }

    public void setFederatedNetworkId(String federatedNetworkId) {
        this.federatedNetworkId = federatedNetworkId;
    }

    public void addUserData(UserData userData) {
        Compute rasCompute = this.getCompute();
        List<UserData> userDataList = rasCompute.getUserData();

        if (userDataList == null) {
            userDataList = new ArrayList<>();
            rasCompute.setUserData((ArrayList<UserData>) userDataList);
        }

        userDataList.add(userData);
    }
}
