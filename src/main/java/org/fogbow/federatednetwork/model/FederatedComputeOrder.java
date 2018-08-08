package org.fogbow.federatednetwork.model;

import org.fogbowcloud.manager.core.models.orders.UserData;
import org.fogbowcloud.manager.core.models.tokens.FederationUser;

import java.util.List;

public class FederatedComputeOrder extends FederatedOrder {

    private int vCPU;
    private int memory;
    private int disk;
    private String imageId;
    private UserData userData;
    private String publicKey;
    private List<String> networksId;

    private String federatedNetworkId;
    private String federatedIp;

    public FederatedComputeOrder(String id, FederationUser federationUser, int vCPU, int memory, int disk,
                                 String imageId, UserData userData, String publicKey, List<String> networksId,
                                 String federatedNetworkId, String federatedIp) {
        super(id, federationUser);
        this.vCPU = vCPU;
        this.memory = memory;
        this.disk = disk;
        this.imageId = imageId;
        this.userData = userData;
        this.publicKey = publicKey;
        this.networksId = networksId;
        this.federatedNetworkId = federatedNetworkId;
        this.federatedIp = federatedIp;
    }

    @Override
    public FederatedResourceType getType() {
        return FederatedResourceType.FEDERATED_COMPUTE;
    }

    public int getvCPU() {
        return vCPU;
    }

    public void setvCPU(int vCPU) {
        this.vCPU = vCPU;
    }

    public int getMemory() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public int getDisk() {
        return disk;
    }

    public void setDisk(int disk) {
        this.disk = disk;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public UserData getUserData() {
        return userData;
    }

    public void setUserData(UserData userData) {
        this.userData = userData;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public List<String> getNetworksId() {
        return networksId;
    }

    public void setNetworksId(List<String> networksId) {
        this.networksId = networksId;
    }

    public String getFederatedNetworkId() {
        return federatedNetworkId;
    }

    public void setFederatedNetworkId(String federatedNetworkId) {
        this.federatedNetworkId = federatedNetworkId;
    }

    public String getFederatedIp() {
        return federatedIp;
    }

    public void setFederatedIp(String federatedIp) {
        this.federatedIp = federatedIp;
    }
}
