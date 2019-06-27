package cloud.fogbow.fns.core;

import cloud.fogbow.common.exceptions.FogbowException;
import cloud.fogbow.fns.constants.ConfigurationPropertyKeys;
import cloud.fogbow.ras.api.http.request.PublicKey;

import java.security.interfaces.RSAPublicKey;

import static cloud.fogbow.common.util.PublicKeysHolder.getPublicKey;

public class FnsPublicKeysHolder {
    private RSAPublicKey asPublicKey;
    private RSAPublicKey rasPublicKey;

    private static FnsPublicKeysHolder instance;

    private FnsPublicKeysHolder() {
        this.asPublicKey = null;
        this.rasPublicKey = null;
    }

    public static synchronized FnsPublicKeysHolder getInstance() {
        if (instance == null) {
            instance = new FnsPublicKeysHolder();
        }
        return instance;
    }

    public RSAPublicKey getAsPublicKey() throws FogbowException {
        if (this.asPublicKey == null) {
            String asAddress = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.AS_URL_KEY);
            String asPort = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.AS_PORT_KEY);
            this.asPublicKey = getPublicKey(asAddress, asPort, cloud.fogbow.as.api.http.request.PublicKey.PUBLIC_KEY_ENDPOINT);
        }
        return this.asPublicKey;
    }

    public RSAPublicKey getRasPublicKey() throws FogbowException {
        if (this.rasPublicKey == null) {
            String rasAddress = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.RAS_URL_KEY);
            String rasPort = PropertiesHolder.getInstance().getProperty(ConfigurationPropertyKeys.RAS_PORT_KEY);
            this.rasPublicKey = getPublicKey(rasAddress, rasPort, PublicKey.PUBLIC_KEY_ENDPOINT);
        }
        return this.rasPublicKey;
    }
}
