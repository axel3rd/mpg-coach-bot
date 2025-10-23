package org.blondin.mpg.root;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.blondin.mpg.AbstractClient;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.root.model.UserSignIn;

import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

/**
 * Mpg Website Authenticator<br/>
 * First step of OIDC authentication<br/>
 * Required for user after February 2025.
 * 
 * Based on https://gist.github.com/ClementRoyer/d8eeaf8f05253f7618db3b49a8594af3
 */
public class AuthentMpgWebClient extends AbstractClient {

    private AuthentConnectLigue1Client connectL1Client;

    private AuthentMpgWebClient(Config config, String urlOverride) {
        super(config);
        connectL1Client = AuthentConnectLigue1Client.build(config, urlOverride);
    }

    public static AuthentMpgWebClient build(Config config, String urlOverride) {
        AuthentMpgWebClient mpgWebClient = new AuthentMpgWebClient(config, urlOverride);
        mpgWebClient.setUrl(Objects.toString(urlOverride, "https://mpg.football"));
        return mpgWebClient;
    }

    /**
     * OIDC authentication
     * 
     * @param login      Login
     * @param password   Password
     * @param randomUUID Random UUID
     * @return Token
     */
    public UserSignIn authenticate(String login, String password, String randomUUID) {
        // --- Step 1: Initiate auth with MPG (POST form) ---
        post("auth", Map.of("_data", "routes/__home/__auth/auth", "ext-amplitudeId", randomUUID), new Form().param("email", login).param("password", password),
                MediaType.APPLICATION_FORM_URLENCODED_TYPE, String.class);
        if (!getHeaders().containsKey("x-remix-redirect")) {
            throw new UnsupportedOperationException("Header 'x-remix-redirect' is missing in first oidc authentication step");
        }
        URI redirectUri = URI.create(getHeaders().getFirst("x-remix-redirect").replace("ext-amplitudeId=", "ext-amplitudeId=" + randomUUID));

        // --- Steps 2 + 3 + 4 to connect.ligue1
        String code = connectL1Client.getMpgWebSiteCode(redirectUri.getPath(), redirectUri.getQuery(), login, password);

        // --- Step 5: Exchange code for session (POST to callback) ---
        post("auth/callback", null, null, new Form("code", code), MediaType.APPLICATION_FORM_URLENCODED_TYPE, false, String.class);

        String sessionCookieHeader = findSetCookieStartingWith(getHeaders().get("set-cookie"), "__session=");
        if (sessionCookieHeader == null) {
            throw new UnsupportedOperationException("__session cookie not found in callback response (step 5)");
        }
        String sessionValue = sessionCookieHeader.split(";", 2)[0].split("=", 2)[1];

        // --- Step 6: Extract token from dashboard ---
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.add("Cookie", "__session=" + sessionValue);
        return get("dashboard", Map.of("_data", "root"), headers, UserSignIn.class, -1);
    }

    private static String findSetCookieStartingWith(List<String> setCookieHeaders, String prefix) {
        for (String s : setCookieHeaders) {
            if (s != null && s.startsWith(prefix))
                return s;
            // sometimes whitespace or other; check after trimming
            if (s != null && s.trim().startsWith(prefix))
                return s.trim();
        }
        return null;
    }

}
