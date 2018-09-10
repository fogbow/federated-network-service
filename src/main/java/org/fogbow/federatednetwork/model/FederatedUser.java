package org.fogbow.federatednetwork.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.fogbowcloud.ras.core.models.tokens.FederationUserToken;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class FederatedUser extends FederationUserToken {

    @Column
    private String federatedUserId;

    @Column
    private String federatedUserName;

    public FederatedUser() { }

    public FederatedUser(String federatedUserId, String federatedUserName) {
        this.federatedUserId = federatedUserId;
        this.federatedUserName = federatedUserName;
    }

    public FederatedUser(String tokenProvider, String federationUserTokenValue, String federatedUserId, String federatedUserName) {
        super(tokenProvider, federationUserTokenValue, federatedUserId, federatedUserName);
    }

    public void setFederatedUserId(String federatedUserId) {
        this.federatedUserId = federatedUserId;
    }

    public void setFederatedUserName(String federatedUserName) {
        this.federatedUserName = federatedUserName;
    }

    public String getFederatedUserId() {
        return federatedUserId;
    }

    public String getFederatedUserName() {
        return federatedUserName;
    }

    @Override
    @JsonIgnore
    public String getTokenProvider() {
        return super.getTokenProvider();
    }

    @Override
    @JsonIgnore
    public String getTokenValue() {
        return super.getTokenValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FederatedUser that = (FederatedUser) o;
        return Objects.equals(getFederatedUserId(), that.getFederatedUserId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId());
    }
}