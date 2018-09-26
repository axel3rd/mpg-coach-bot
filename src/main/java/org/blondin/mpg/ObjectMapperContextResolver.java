package org.blondin.mpg;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Provider
public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {
    private ObjectMapper mapper = null;

    public ObjectMapperContextResolver() {
        super();
        mapper = new ObjectMapper().enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}
