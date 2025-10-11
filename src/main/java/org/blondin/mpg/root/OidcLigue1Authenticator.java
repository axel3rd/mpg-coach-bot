package org.blondin.mpg.root;

import java.util.Objects;

import org.blondin.mpg.AbstractClient;
import org.blondin.mpg.config.Config;

/**
 * Ligue1 Authenticator (required for user after February 2025).
 * 
 * Based on https://gist.github.com/ClementRoyer/d8eeaf8f05253f7618db3b49a8594af3
 */
public class OidcLigue1Authenticator extends AbstractClient {

    private OidcLigue1Authenticator(Config config) {
        super(config);
    }

    public static OidcLigue1Authenticator build(Config config, String urlOverride) {
        OidcLigue1Authenticator authenticator = new OidcLigue1Authenticator(config);
        authenticator.setUrl(Objects.toString(urlOverride, "https://mpg.football"));
        return authenticator;
    }

    /**
     * OIDC authentication
     * 
     * @param login    Login
     * @param password Password
     * @return Token
     */
    public String authenticate(String login, String password) {
        return "NYI";
    }

}
