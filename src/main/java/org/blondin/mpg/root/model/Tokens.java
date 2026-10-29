package org.blondin.mpg.root.model;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Tokens {

    private static final Logger LOG = LoggerFactory.getLogger(Tokens.class);

    public Tokens() {
        // For Jackson
    }

    public Tokens(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    private long expiration = -1;

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public boolean isValid() {
        try {
            // Already calculated
            if (expiration >= 0) {
                return true;
            }
            if (StringUtils.isBlank(accessToken) || StringUtils.isBlank(refreshToken)) {
                return false;
            }
            String[] accessTokenParts = accessToken.split("\\.");
            if (accessTokenParts.length != 3) {
                return false;
            }
            String jsonData = new String(Base64.getDecoder().decode(accessTokenParts[1]), StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map = mapper.readValue(jsonData, new TypeReference<Map<String, Object>>() {
            });
            if (!map.containsKey("exp")) {
                return false;
            }
            expiration = ((Number) map.get("exp")).longValue();
            return true;
        } catch (JsonProcessingException | IllegalArgumentException e) {
            LOG.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean isExpiredInDays(int days) {
        if (expiration == -1) {
            throw new UnsupportedOperationException("Please use 'isValid()' method fist.");
        }
        boolean expired = System.currentTimeMillis() / 1000 + days * 86400 >= expiration;
        LOG.debug("Test if token is expired in {} days: {} (expiration: {})", days, expired, Instant.ofEpochSecond(expiration));
        return expired;
    }

}
