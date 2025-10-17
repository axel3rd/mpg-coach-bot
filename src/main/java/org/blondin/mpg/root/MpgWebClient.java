package org.blondin.mpg.root;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.blondin.mpg.AbstractClient;
import org.blondin.mpg.config.Config;

import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;

/**
 * Mpg Website Authenticator<br/>
 * First step of OIDC authentication<br/>
 * Required for user after February 2025.
 * 
 * Based on https://gist.github.com/ClementRoyer/d8eeaf8f05253f7618db3b49a8594af3
 */
public class MpgWebClient extends AbstractClient {

    private MpgWebClient(Config config) {
        super(config);
    }

    public static MpgWebClient build(Config config, String urlOverride) {
        MpgWebClient mpgWebClient = new MpgWebClient(config);
        mpgWebClient.setUrl(Objects.toString(urlOverride, "https://mpg.football"));
        return mpgWebClient;
    }

    /**
     * OIDC authentication
     * 
     * @param login    Login
     * @param password Password
     * @return Token
     */
    public String authenticate(String login, String password) {
        try {
            String amplitudeId = UUID.randomUUID().toString();

            // --- Step 1: Initiate auth with MPG (POST form) ---
            final Form entity = new Form();
            entity.param("email", login);
            entity.param("password", password);

            post("auth", Map.of("_data", "routes/__home/__auth/auth", "ext-amplitudeId", amplitudeId), entity, MediaType.APPLICATION_FORM_URLENCODED_TYPE, String.class);
            if (!getHeaders().containsKey("x-remix-redirect")) {
                throw new UnsupportedOperationException("Header 'x-remix-redirect' is missing in previous step");
            }
            URI redirectUri = new URI(getHeaders().getFirst("x-remix-redirect").replace("ext-amplitudeId=", "ext-amplitudeId=" + amplitudeId));

            // --- Step 2: Follow redirect to Ligue1 OAuth (GET) ---
            System.out.println(redirectUri.getScheme());
            System.out.println(redirectUri.getHost());
            System.out.println(redirectUri.getPort());
            System.out.println(redirectUri.getPath());
            System.out.println(redirectUri.getQuery());

        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new UnsupportedOperationException("Not Yet Implemented");
    }

}
