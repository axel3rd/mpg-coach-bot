package org.blondin.mpg.root;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.blondin.mpg.AbstractClient;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.root.model.Tokens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mpg Website Authenticator<br/>
 * First step of OIDC authentication<br/>
 * Required for user after February 2025.
 * 
 * Based on https://gist.github.com/ClementRoyer/d8eeaf8f05253f7618db3b49a8594af3
 */
public class AuthentMpgWebClient extends AbstractClient {

    private static final Logger LOG = LoggerFactory.getLogger(AuthentMpgWebClient.class);
    private final AuthentConnectLigue1Client connectL1Client;

    private AuthentMpgWebClient(Config config, String urlOverride) {
        super(config);
        connectL1Client = AuthentConnectLigue1Client.build(config, urlOverride);
    }

    public static AuthentMpgWebClient build(Config config, String urlOverride) {
        AuthentMpgWebClient mpgWebClient = new AuthentMpgWebClient(config, urlOverride);
        mpgWebClient.setUrl(Objects.toString(urlOverride, "https://mpg.football"));
        return mpgWebClient;
    }

    protected String getClientId() {
        String home = get("", String.class);
        String indexValue = StringUtils.substringBetween(home, "/_expo/static/js/web/index-", ".js");
        String index = get("_expo/static/js/web/index-" + indexValue + ".js", String.class);
        return StringUtils.substringBetween(index, "AUTH0_WEB_CLIENT_ID\\\":\\\"", "\\\",\\\"AUTH0_AUDIENCE");
    }

    /**
     * OIDC authentication
     * 
     * @param login    Login
     * @param password Password
     * @return access_token and refresh_token
     */
    public Tokens authenticate(String login, String password) {
        LOG.debug("Authenticate with login/password on web OIDC");
        return connectL1Client.authenticate(getClientId(), "https://mpg.football", login, password);
    }

    public Tokens refresh(String refreshToken) {
        LOG.debug("Refreshing tokens");
        return connectL1Client.refresh(getClientId(), refreshToken);
    }

}
