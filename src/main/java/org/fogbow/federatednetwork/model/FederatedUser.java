package org.fogbow.federatednetwork.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.fogbowcloud.manager.core.models.tokens.FederationUserToken;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class FederatedUser extends FederationUserToken {

    @Column
    @JsonIgnore
    private String userId;

    @Column
    private String userName;

    public FederatedUser() { }

    public FederatedUser(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    public FederatedUser(String tokenProvider, String federationUserTokenValue, String userId, String userName) {
        super(tokenProvider, federationUserTokenValue, userId, userName);
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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
        return Objects.equals(getUserId(), that.getUserId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId());
    }
}