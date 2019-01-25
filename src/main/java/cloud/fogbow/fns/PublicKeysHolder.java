package cloud.fogbow.fns;

import cloud.fogbow.fns.common.exceptions.UnavailableProviderException;
import cloud.fogbow.fns.common.exceptions.UnexpectedException;
import cloud.fogbow.fns.common.util.RSAUtil;
import cloud.fogbow.fns.common.util.connectivity.HttpRequestClientUtil;
import cloud.fogbow.fns.constants.ConfigurationConstants;
import cloud.fogbow.fns.constants.Messages;
import org.apache.http.client.HttpResponseException;

import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPublicKey;

public class PublicKeysHolder {
    private HttpRequestClientUtil client;
    private RSAPublicKey asPublicKey;
    private RSAPublicKey rasPublicKey;

    private static PublicKeysHolder instance;

    private PublicKeysHolder() {
        String timeoutStr = PropertiesHolder.getInstance().getProperty(ConfigurationConstants.HTTP_REQUEST_TIMEOUT);
        this.client = new HttpRequestClientUtil(new Integer(timeoutStr));
        this.asPublicKey = null;
        this.rasPublicKey = null;
    }

    public static synchronized PublicKeysHolder getInstance() {
        if (instance == null) {
            instance = new PublicKeysHolder();
        }
        return instance;
    }

    public RSAPublicKey getAsPublicKey() throws UnavailableProviderException, UnexpectedException {
        if (this.asPublicKey == null) {
            String asAddress = PropertiesHolder.getInstance().getProperty(ConfigurationConstants.AS_URL);
            String asPort = PropertiesHolder.getInstance().getProperty(ConfigurationConstants.AS_PORT);
            // ToDo import dependency of AS
            //this.asPublicKey = getPublicKey(asAddress, asPort, org.fogbowcloud.as.api.http.PublicKey.PUBLIC_KEY_ENDPOINT);
            this.asPublicKey = getPublicKey(asAddress, asPort, "publicKey");
        }
        return this.asPublicKey;
    }

    public RSAPublicKey getRasPublicKey() throws UnavailableProviderException, UnexpectedException {
        if (this.rasPublicKey == null) {
            String rasAddress = PropertiesHolder.getInstance().getProperty(ConfigurationConstants.RAS_URL);
            String rasPort = PropertiesHolder.getInstance().getProperty(ConfigurationConstants.RAS_PORT);
            // ToDo implement this endpoint in RAS
            //this.rasPublicKey = getPublicKey(rasAddress, rasPort, org.fogbowcloud.ras.api.http.PublicKey.PUBLIC_KEY_ENDPOINT);
            this.rasPublicKey = getPublicKey(rasAddress, rasPort, "publicKey");
        }
        return this.rasPublicKey;
    }

    private RSAPublicKey getPublicKey(String serviceAddress, String servicePort, String suffix)
            throws UnavailableProviderException, UnexpectedException {
        RSAPublicKey publicKey = null;
        String endpoint = serviceAddress + ":" + servicePort + "/" + suffix;
        String responseStr = null;
        try {
            responseStr = this.client.doGetRequest(endpoint, null);
        } catch (HttpResponseException e) {
            throw new UnavailableProviderException(e.getMessage(), e);
        }
        try {
            publicKey = RSAUtil.getPublicKeyFromString(responseStr);
        } catch (GeneralSecurityException e) {
            throw new UnexpectedException(Messages.Exception.INVALID_PUBLIC_KEY);
        }
        return publicKey;
    }
}
