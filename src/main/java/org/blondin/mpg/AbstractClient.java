package org.blondin.mpg;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.blondin.mpg.config.Proxy;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

public abstract class AbstractClient {

    private Proxy proxy;
    private String url;

    protected AbstractClient() {
        super();
    }

    protected void setUrl(String url) {
        this.url = url;
    }

    protected void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    protected <T> T get(String path, Class<T> entityResponse) {
        return get(path, null, entityResponse, false);
    }

    protected <T> T get(String path, MultivaluedMap<String, Object> headers, Class<T> entityResponse) {
        return get(path, headers, entityResponse, false);
    }

    protected <T> T get(String path, MultivaluedMap<String, Object> headers, Class<T> entityResponse, boolean wrapRoot) {
        return call(path, headers, null, entityResponse, wrapRoot);
    }

    protected <T> T post(String path, Object entityRequest, Class<T> entityResponse) {
        return post(path, null, entityRequest, entityResponse, false);
    }

    protected <T> T post(String path, MultivaluedMap<String, Object> headers, Object entityRequest, Class<T> entityResponse) {
        return post(path, headers, entityRequest, entityResponse, false);
    }

    protected <T> T post(String path, MultivaluedMap<String, Object> headers, Object entityRequest, Class<T> entityResponse, boolean wrapRoot) {
        return call(path, headers, entityRequest, entityResponse, wrapRoot);
    }

    private <T> T call(String path, MultivaluedMap<String, Object> headers, Object entityRequest, Class<T> entityResponse, boolean wrapRoot) {
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
        Client client = ClientBuilder.newClient(config);

        if (wrapRoot) {
            client = client.register(ObjectMapperContextResolver.class);
        }
        if (StringUtils.isBlank(url)) {
            throw new UnsupportedOperationException("Please use 'setUrl(...)' before using this client");
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
        return response.readEntity(entityResponse);
    }
}
