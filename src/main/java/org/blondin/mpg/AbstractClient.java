package org.blondin.mpg;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

public abstract class AbstractClient {

    protected AbstractClient() {
        super();
    }

    protected abstract String getUrl();

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
        Client client = ClientBuilder.newClient();
        if (wrapRoot) {
            client = client.register(ObjectMapperContextResolver.class);
        }
        WebTarget webTarget = client.target(getUrl()).path(path);
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON).headers(headers);
        Response response = null;
        if (entityRequest == null) {
            response = invocationBuilder.get();
        } else {
            response = invocationBuilder.post(Entity.entity(entityRequest, MediaType.APPLICATION_JSON));
        }
        if (Response.Status.OK.getStatusCode() != response.getStatus()) {
            throw new UnsupportedOperationException(String.format("Unsupported status code: %s", response.getStatus()));
        }
        return response.readEntity(entityResponse);
    }
}
