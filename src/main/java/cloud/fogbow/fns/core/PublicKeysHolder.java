package cloud.fogbow.fns.core;

import cloud.fogbow.common.exceptions.UnavailableProviderException;
import cloud.fogbow.common.exceptions.UnexpectedException;
import cloud.fogbow.common.models.CloudToken;
import cloud.fogbow.common.util.RSAUtil;
import cloud.fogbow.common.util.connectivity.HttpRequestClientUtil;
import cloud.fogbow.fns.core.constants.ConfigurationConstants;
import cloud.fogbow.fns.core.constants.DefaultConfigurationConstants;
import cloud.fogbow.fns.core.constants.Messages;
import cloud.fogbow.fns.utils.RedirectUtil;
import cloud.fogbow.ras.api.http.PublicKey;
import org.apache.http.client.HttpResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPublicKey;

public class PublicKeysHolder {
    private HttpRequestClientUtil client;
    private RSAPublicKey asPublicKey;
    private RSAPublicKey rasPublicKey;

    private static PublicKeysHolder instance;

    private PublicKeysHolder() {
        String timeoutStr = PropertiesHolder.getInstance().getProperty(ConfigurationConstants.HTTP_REQUEST_TIMEOUT_KEY,
                DefaultConfigurationConstants.HTTP_REQUEST_TIMEOUT);
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

    public RSAPublicKey getAsPublicKey() throws UnavailableProviderException, UnexpectedException, URISyntaxException {
        if (this.asPublicKey == null) {
            String asAddress = PropertiesHolder.getInstance().getProperty(ConfigurationConstants.AS_URL_KEY);
            String asPort = PropertiesHolder.getInstance().getProperty(ConfigurationConstants.AS_PORT_KEY);
            this.asPublicKey = getPublicKey(asAddress, asPort, cloud.fogbow.as.api.http.PublicKey.PUBLIC_KEY_ENDPOINT);
        }
        return this.asPublicKey;
    }

    public RSAPublicKey getRasPublicKey() throws UnavailableProviderException, UnexpectedException, URISyntaxException {
        if (this.rasPublicKey == null) {
            String rasAddress = PropertiesHolder.getInstance().getProperty(ConfigurationConstants.RAS_URL_KEY);
            String rasPort = PropertiesHolder.getInstance().getProperty(ConfigurationConstants.RAS_PORT_KEY);
            this.rasPublicKey = getPublicKey(rasAddress, rasPort, PublicKey.PUBLIC_KEY_ENDPOINT);
        }
        return this.rasPublicKey;
    }

    private RSAPublicKey getPublicKey(String serviceAddress, String servicePort, String suffix)
            throws UnavailableProviderException, UnexpectedException, URISyntaxException {
        RSAPublicKey publicKey = null;

        URI uri = new URI(serviceAddress);
        uri = UriComponentsBuilder.fromUri(uri).port(servicePort).path(suffix).build(true).toUri();

        String responseStr = null;
        try {
            responseStr = this.client.doGetRequest(uri.toString(), new CloudToken("", "", ""));
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
