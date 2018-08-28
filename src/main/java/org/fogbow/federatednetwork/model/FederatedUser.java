package org.fogbow.federatednetwork.model;

import org.fogbowcloud.manager.core.models.tokens.FederationUserToken;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class FederatedUser extends FederationUserToken {

    @Column
    private String userId;

    @Column
    private String userName;

    public FederatedUser(String tokenProvider, String federationUserTokenValue, String userId, String userName) {
        super(tokenProvider, federationUserTokenValue, userId, userName);
    }

    public FederatedUser(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    public FederatedUser() { }


    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}