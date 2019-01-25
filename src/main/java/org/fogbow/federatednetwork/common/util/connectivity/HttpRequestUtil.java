package org.fogbow.federatednetwork.common.util.connectivity;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.fogbow.federatednetwork.common.exceptions.FatalErrorException;

public class HttpRequestUtil {
    private static final Logger LOGGER = Logger.getLogger(HttpRequestUtil.class);

    public static final String CONTENT_TYPE_KEY = "Content-Type";
    public static final String ACCEPT_KEY = "Accept";
    public static final String JSON_CONTENT_TYPE_KEY = "application/json";
    public static final String X_AUTH_TOKEN_KEY = "X-Auth-Token";
    private static Integer timeoutHttpRequest;

    public static CloseableHttpClient createHttpClient(Integer timeout) throws FatalErrorException {
        return createHttpClient(timeout, null, null);
    }

    public static CloseableHttpClient createHttpClient(SSLConnectionSocketFactory sslsf) throws FatalErrorException {
        return createHttpClient(null, sslsf, null);
    }

    public static CloseableHttpClient createHttpClient(HttpClientConnectionManager connManager) throws FatalErrorException {
        return createHttpClient(null, null, connManager);
    }

    public static CloseableHttpClient createHttpClient(Integer timeout, SSLConnectionSocketFactory sslsf,
                                                       HttpClientConnectionManager connManager) throws FatalErrorException {

        HttpClientBuilder builder = HttpClientBuilder.create();
        setDefaultResquestConfig(timeout, builder);
        setSSLConnection(sslsf, builder);
        setConnectionManager(connManager, builder);

        return builder.build();
    }

    protected static void setDefaultResquestConfig(Integer timeout, HttpClientBuilder builder) {

        RequestConfig.Builder requestBuilder = RequestConfig.custom();

        if (timeout == null) {
            timeout = timeoutHttpRequest;
        }
        requestBuilder = requestBuilder.setSocketTimeout(timeout);
        builder.setDefaultRequestConfig(requestBuilder.build());
    }

    protected static void setConnectionManager(HttpClientConnectionManager connManager, HttpClientBuilder builder) {
        if (connManager != null) {
            builder.setConnectionManager(connManager);
        }
    }

    protected static void setSSLConnection(SSLConnectionSocketFactory sslsf, HttpClientBuilder builder) {
        if (sslsf != null) {
            builder.setSSLSocketFactory(sslsf);
        }
    }
}
