package org.blondin.mpg;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.config.Proxy;
import org.blondin.mpg.root.exception.UrlForbiddenException;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

public abstract class AbstractClient {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractClient.class);

    protected static final long TIME_HOUR_IN_MILLI_SECOND = 3600000;

    private String url;
    private final Proxy proxy;
    private final boolean sslCertificatesCheck;
    private final long requestWaitTimeMilliSecond;
    private final List<String> requestWaitUrls;
    private boolean requestHasToWaitTime = false;
    private long requestLastCall = 0;

    protected AbstractClient(Config config) {
        super();
        if (config != null) {
            this.proxy = config.getProxy();
            this.sslCertificatesCheck = config.isSslCertificatesCheck();
            this.requestWaitTimeMilliSecond = config.getRequestWaitTime() * (long) 1000;
            this.requestWaitUrls = config.getRequestWaitUrls();
        } else {
            this.proxy = null;
            this.sslCertificatesCheck = true;
            this.requestWaitTimeMilliSecond = 0;
            this.requestWaitUrls = null;
        }
    }

    protected void setUrl(String url) {
        this.url = url;
        if (requestWaitUrls != null) {
            for (String u : requestWaitUrls) {
                if (url.startsWith(u)) {
                    requestHasToWaitTime = true;
                    return;
                }
            }
        }
    }

    protected <T> T get(String path, Class<T> entityResponse) {
        return get(path, null, entityResponse, -1);
    }

    protected <T> T get(String path, Class<T> entityResponse, long cacheTimeMilliSecond) {
        return get(path, null, entityResponse, cacheTimeMilliSecond);
    }

    protected <T> T get(String path, MultivaluedMap<String, Object> headers, Class<T> entityResponse) {
        return get(path, headers, entityResponse, -1);
    }

    protected <T> T get(String path, MultivaluedMap<String, Object> headers, Class<T> entityResponse, long cacheTimeMilliSecond) {
        return call(path, headers, null, entityResponse, cacheTimeMilliSecond, false);
    }

    protected <T> T post(String path, Object entityRequest, Class<T> entityResponse) {
        return post(path, null, entityRequest, entityResponse);
    }

    protected <T> T post(String path, MultivaluedMap<String, Object> headers, Object entityRequest, Class<T> entityResponse) {
        return call(path, headers, entityRequest, entityResponse, -1, false);
    }

    protected <T> T put(String path, MultivaluedMap<String, Object> headers, Object entityRequest, Class<T> entityResponse) {
        return call(path, headers, entityRequest, entityResponse, -1, true);
    }

    private <T> T call(String path, MultivaluedMap<String, Object> headers, Object entityRequest, Class<T> entityResponse, long cacheTimeMilliSecond,
            boolean entityRequestPut) {
        long start = System.currentTimeMillis();
        try {
            LOG.debug("Call URL: {}/{} (cache duration ms: {})", url, path, cacheTimeMilliSecond);
            if (StringUtils.isBlank(url)) {
                throw new UnsupportedOperationException("Please use 'setUrl(...)' before using this client");
            }
            File cacheFile = null;
            if (cacheTimeMilliSecond >= 0) {
                cacheFile = getCacheFile(url, path);
                if (cacheFile.exists()
                        && (cacheTimeMilliSecond == 0 || cacheFile.lastModified() > System.currentTimeMillis() - cacheTimeMilliSecond)) {
                    LOG.debug("Read cache file: {}", cacheFile.getAbsolutePath());
                    return readEntityFromFile(cacheFile, entityResponse);
                }
            }

            ClientBuilder clientBuilder = ClientBuilder.newBuilder().withConfig(createConfigWithPotentialProxy());
            if (!this.sslCertificatesCheck) {
                final TrustManager[] trustManagerArray = { new NullX509TrustManager() };
                SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                sslContext.init(null, trustManagerArray, new java.security.SecureRandom());
                clientBuilder.sslContext(sslContext).hostnameVerifier(new NullHostnameVerifier());
            }
            Client client = clientBuilder.build();

            WebTarget webTarget = client.target(url).path(path);
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON).headers(headers);

            waitBeforeNextRequest();
            Response response = invokeWithRetry(invocationBuilder, entityRequest, entityRequestPut, url, path, 0);
            if (Response.Status.FORBIDDEN.getStatusCode() == response.getStatus()) {
                throw new UrlForbiddenException(String.format("Forbidden URL: %s", url));
            }
            if (Response.Status.SERVICE_UNAVAILABLE.getStatusCode() == response.getStatus()) {
                throw new ServiceUnavailableException(String.format("Service Unavailable URL: %s", url));
            }
            if (Response.Status.OK.getStatusCode() != response.getStatus()) {
                String content = IOUtils.toString((InputStream) response.getEntity(), StandardCharsets.UTF_8);
                if (StringUtils.isNoneBlank(content)) {
                    content = " / Content: " + content;
                }
                throw new UnsupportedOperationException(
                        String.format("Unsupported status code: %s %s%s", response.getStatus(), response.getStatusInfo().getReasonPhrase(), content));
            }
            if (cacheFile != null) {
                LOG.debug("Write cache file: {}", cacheFile.getAbsolutePath());
                Files.copy((InputStream) response.getEntity(), cacheFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return readEntityFromFile(cacheFile, entityResponse);
            }
            return response.readEntity(entityResponse);
        } catch (IOException | KeyManagementException | NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e);
        } finally {
            LOG.debug("Call URL time elaps ms: {}", System.currentTimeMillis() - start);
        }
    }

    private static Response invokeWithRetry(Invocation.Builder invocationBuilder, Object entityRequest, boolean entityRequestPut, final String url,
            final String path, int retryCount) {
        Response response = null;
        try {
            if (entityRequest == null) {
                response = invocationBuilder.get();
            } else if (entityRequestPut) {
                response = invocationBuilder.put(Entity.entity(entityRequest, MediaType.APPLICATION_JSON));
            } else {
                response = invocationBuilder.post(Entity.entity(entityRequest, MediaType.APPLICATION_JSON));
            }
        } catch (ProcessingException e) {
            if (e.getCause() instanceof SocketException && retryCount < 10) {
                LOG.debug("Retrying ('{}' on '{}/{}')...", e.getMessage(), url, path);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) { // NOSONAR : Sleep wanted
                    throw new UnsupportedOperationException(e1);
                }
                return invokeWithRetry(invocationBuilder, entityRequest, entityRequestPut, url, path, ++retryCount);
            }
            throw e;
        }
        return response;
    }

    private void waitBeforeNextRequest() {
        if (!requestHasToWaitTime) {
            return;
        }
        if (System.currentTimeMillis() < requestWaitTimeMilliSecond + requestLastCall) {
            try {
                Thread.sleep(requestWaitTimeMilliSecond);
            } catch (InterruptedException e) { // NOSONAR : Sleep wanted
                throw new UnsupportedOperationException(e);
            }
        }
        requestLastCall = System.currentTimeMillis();
    }

    @SuppressWarnings("unchecked")
    private static <T> T readEntityFromFile(File file, Class<T> entityResponse) throws IOException {
        if (entityResponse.equals(String.class)) {
            return (T) FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        }
        // Perhaps 'enable(DeserializationFeature.UNWRAP_ROOT_VALUE)' to set depending wrapRoot
        return new ObjectMapper().readValue(file, entityResponse);
    }

    public static File getCacheFile(String url, String path) {
        return new File(System.getProperty("java.io.tmpdir"), "mpg-coach-bot-" + url.replaceAll("\\W+", "") + path.replaceAll("\\W+", ""));
    }

    private ClientConfig createConfigWithPotentialProxy() {
        ClientConfig config = new ClientConfig();
        if (proxy != null && proxy.isConfigured()) {
            config.connectorProvider(new ApacheConnectorProvider());
            config.property(ClientProperties.PROXY_URI, proxy.getUri());
            if (StringUtils.isNotBlank(proxy.getUser())) {
                config.property(ClientProperties.PROXY_USERNAME, proxy.getUser());
                if (StringUtils.isNoneBlank(proxy.getPassword())) {
                    config.property(ClientProperties.PROXY_PASSWORD, proxy.getPassword());
                }
            }
        }
        return config;
    }

    /**
     * Host name verifier that does not perform nay checks.
     */
    private static class NullHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return hostname.equalsIgnoreCase(session.getPeerHost());
        }
    }

    /**
     * Trust manager that does not perform nay checks.
     */
    private static class NullX509TrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) { // NOSONAR : Wanted for corner case
            // No checks, so nothing
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) { // NOSONAR : Wanted for corner case
            // No checks, so nothing
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
