package org.blondin.mpg;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.blondin.mpg.config.Proxy;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractClient {

    protected static final long TIME_HOUR_IN_MILLI_SECOND = 3600000;

    private Proxy proxy;
    private String url;

    protected AbstractClient() {
        super();
    }

    public void setUrl(String url) {
        this.url = url;
    }

    protected void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    protected <T> T get(String path, Class<T> entityResponse) {
        return get(path, null, entityResponse, false);
    }

    protected <T> T get(String path, Class<T> entityResponse, long cacheTimeMilliSecond) {
        return get(path, null, entityResponse, false, cacheTimeMilliSecond);
    }

    protected <T> T get(String path, MultivaluedMap<String, Object> headers, Class<T> entityResponse) {
        return get(path, headers, entityResponse, false, -1);
    }

    protected <T> T get(String path, MultivaluedMap<String, Object> headers, Class<T> entityResponse, long cacheTimeMilliSecond) {
        return get(path, headers, entityResponse, false, cacheTimeMilliSecond);
    }

    protected <T> T get(String path, MultivaluedMap<String, Object> headers, Class<T> entityResponse, boolean wrapRoot) {
        return call(path, headers, null, entityResponse, wrapRoot, -1);
    }

    protected <T> T get(String path, MultivaluedMap<String, Object> headers, Class<T> entityResponse, boolean wrapRoot, long cacheTimeMilliSecond) {
        return call(path, headers, null, entityResponse, wrapRoot, cacheTimeMilliSecond);
    }

    protected <T> T post(String path, Object entityRequest, Class<T> entityResponse) {
        return post(path, null, entityRequest, entityResponse, false);
    }

    protected <T> T post(String path, MultivaluedMap<String, Object> headers, Object entityRequest, Class<T> entityResponse) {
        return post(path, headers, entityRequest, entityResponse, false);
    }

    protected <T> T post(String path, MultivaluedMap<String, Object> headers, Object entityRequest, Class<T> entityResponse, boolean wrapRoot) {
        return call(path, headers, entityRequest, entityResponse, wrapRoot, -1);
    }

    private <T> T call(String path, MultivaluedMap<String, Object> headers, Object entityRequest, Class<T> entityResponse, boolean wrapRoot,
            long cacheTimeMilliSecond) {
        try {
            if (StringUtils.isBlank(url)) {
                throw new UnsupportedOperationException("Please use 'setUrl(...)' before using this client");
            }
            File cacheFile = null;
            if (cacheTimeMilliSecond >= 0) {
                cacheFile = getCacheFile(url, path);
                if (cacheFile.exists()
                        && (cacheTimeMilliSecond == 0 || cacheFile.lastModified() > System.currentTimeMillis() - cacheTimeMilliSecond)) {
                    return readEntityFromFile(cacheFile, entityResponse);
                }
            }

            Client client = ClientBuilder.newClient(createConfigWithPotentialProxy());
            if (wrapRoot) {
                client = client.register(ObjectMapperContextResolver.class);
            }
            WebTarget webTarget = client.target(url).path(path);
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON).headers(headers);
            Response response = null;
            if (entityRequest == null) {
                response = invocationBuilder.get();
            } else {
                response = invocationBuilder.post(Entity.entity(entityRequest, MediaType.APPLICATION_JSON));
            }
            if (Response.Status.OK.getStatusCode() != response.getStatus()) {
                throw new UnsupportedOperationException(
                        String.format("Unsupported status code: %s %s", response.getStatus(), response.getStatusInfo().getReasonPhrase()));
            }
            if (cacheFile != null) {
                Files.copy((InputStream) response.getEntity(), cacheFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return readEntityFromFile(cacheFile, entityResponse);
            }
            return response.readEntity(entityResponse);
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T readEntityFromFile(File file, Class<T> entityResponse) throws IOException {
        if (entityResponse.equals(String.class)) {
            return (T) FileUtils.readFileToString(file);
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
}
