package org.fogbow.federatednetwork.common.models;

import org.fogbow.federatednetwork.common.constants.FogbowConstants;
import org.fogbow.federatednetwork.common.constants.Messages;
import org.fogbow.federatednetwork.common.exceptions.UnexpectedException;

import java.util.Map;

public class FederationUser {
    private Map<String, String> attributes;

    public FederationUser(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getAttribute(String attributeKey) throws UnexpectedException {
        if (this.attributes == null) throw new UnexpectedException(Messages.Exception.INVALID_TOKEN);
        return this.attributes.get(attributeKey);
    }

    public String getTokenProvider() throws UnexpectedException {
        return getAttribute(FogbowConstants.PROVIDER_ID_KEY);
    }

    public String getUserId() throws UnexpectedException {
        return getAttribute(FogbowConstants.USER_ID_KEY);
    }

    public String getUserName() throws UnexpectedException {
        return getAttribute(FogbowConstants.USER_NAME_KEY);
    }

    public String getTokenValue() throws UnexpectedException {
        return getAttribute(FogbowConstants.TOKEN_VALUE_KEY);
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }
}
