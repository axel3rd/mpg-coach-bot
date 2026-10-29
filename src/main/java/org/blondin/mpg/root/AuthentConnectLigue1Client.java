package org.blondin.mpg.root;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.blondin.mpg.AbstractClient;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.root.model.Tokens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

/**
 * Ligue1 website authentication<br/>
 * Second step of OIDC authentication<br/>
 * Required for user after February 2025.
 * 
 * Based on https://gist.github.com/ClementRoyer/d8eeaf8f05253f7618db3b49a8594af3
 */
public class AuthentConnectLigue1Client extends AbstractClient {

    private static final Logger LOG = LoggerFactory.getLogger(AuthentConnectLigue1Client.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String HEADER_LOCATION = "location";
    private static final String HEADER_COOKIE = "Cookie";
    private static final String HEADER_SET_COOKIE = "set-cookie";
    private static final String PARAM_CLIENT_ID = "client_id";

    protected AuthentConnectLigue1Client(Config config) {
        super(config);
    }

    public static AuthentConnectLigue1Client build(Config config, String urlOverride) {
        AuthentConnectLigue1Client connectLigue1Client = new AuthentConnectLigue1Client(config);
        connectLigue1Client.setUrl(Objects.toString(urlOverride, "https://connect.ligue1.fr"));
        return connectLigue1Client;
    }

    public Tokens authenticate(String clientId, String redirectUri, String login, String password) {
        LOG.debug("Authenticate with web OIDC");
        LOG.debug("{}: {}", PARAM_CLIENT_ID, clientId);
        LOG.debug("redirect_url: {}", redirectUri);
        try {
            // ---------- 1. code_verifier (43+ chars, base64url) ----------
            byte[] verifierBytes = new byte[48];
            RANDOM.nextBytes(verifierBytes);
            String codeVerifier = base64Url(verifierBytes);
            LOG.debug("code_verifier: {}", codeVerifier);

            // ---------- 2. code_challenge = BASE64URL(SHA256(verifier)) ----------
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            String codeChallenge = base64Url(digest);
            LOG.debug("code_challenge: {}", codeChallenge);

            // ---------- 3. state & nonce ----------
            byte[] stateBytes = new byte[43];
            RANDOM.nextBytes(stateBytes);
            String state = base64Url(stateBytes);
            LOG.debug("state: {}", state);

            byte[] nonceBytes = new byte[43];
            RANDOM.nextBytes(nonceBytes);
            String nonce = base64Url(nonceBytes);
            LOG.debug("nonce: {}", nonce);

            // ---------- 4. Call URL authentication ----------
            Map<String, Object> params = Map.ofEntries(Map.entry(PARAM_CLIENT_ID, clientId), Map.entry("scope", "openid profile email offline_access"), Map.entry("redirect_uri", redirectUri),
                    Map.entry("audience", "https://mpg.ligue1.fr"), Map.entry("prompt", "login"), Map.entry("ui_locales", "fr"), Map.entry("screen_hint", "signin"),
                    Map.entry("ext-amplitudeId", UUID.randomUUID().toString()), Map.entry("response_type", "code"), Map.entry("response_mode", "query"), Map.entry("state", state),
                    Map.entry("nonce", nonce), Map.entry("code_challenge", codeChallenge), Map.entry("code_challenge_method", "S256"),
                    Map.entry("auth0Client", Base64.getEncoder().encodeToString("{\"name\":\"auth0-react\",\"version\":\"2.20.0\"}".getBytes(StandardCharsets.UTF_8))));
            get("authorize", params, List.of(Response.Status.FOUND), String.class);
            if (getStatusCode() != Response.Status.FOUND.getStatusCode()) {
                throw new UnsupportedOperationException(String.format("Invalid reponse status code (not %s): %s", Response.Status.FOUND.getStatusCode(), getStatusCode()));
            }
            if (!getHeaders().containsKey(HEADER_LOCATION)) {
                throw new UnsupportedOperationException(String.format("Header '%s' is missing in response /authorize", HEADER_LOCATION));
            }
            String loginUrl = getHeaders().getFirst(HEADER_LOCATION);

            // Extract state from loginUrl
            String stateReturn = extractFirstGroup(loginUrl, "state=([^&]+)");
            if (stateReturn == null) {
                throw new UnsupportedOperationException("State parameter not found in loginUrl");
            }

            // --- Step 5: Submit credentials to Ligue1 login (POST) ---
            URI loginPostUrl = URI.create(loginUrl);

            // Build cookie header from set-cookie values of resp2
            MultivaluedMap<String, Object> headersLogin = new MultivaluedHashMap<>();
            headersLogin.add(HEADER_COOKIE, joinCookies(getHeaders().get(HEADER_SET_COOKIE)));

            // HTTP 400 when user/password is invalid
            post(loginPostUrl.getPath().substring(1), paramsMap(loginPostUrl.getQuery()), headersLogin, new Form("state", stateReturn).param("username", login).param("password", password),
                    MediaType.APPLICATION_FORM_URLENCODED_TYPE, List.of(Response.Status.FOUND, Response.Status.BAD_REQUEST), String.class);
            if (getStatusCode() == Response.Status.BAD_REQUEST.getStatusCode()) {
                throw new UnsupportedOperationException("Login/password is invalid");
            }

            if (!getHeaders().containsKey(HEADER_LOCATION)) {
                throw new UnsupportedOperationException(String.format("Header '%s' is missing in response /u/login", HEADER_LOCATION));
            }

            // --- Step 6: Get authorization code (GET resume) ---
            URI resultUrl = URI.create(getHeaders().getFirst(HEADER_LOCATION));
            MultivaluedMap<String, Object> headersResume = new MultivaluedHashMap<>();
            headersResume.add(HEADER_COOKIE, joinCookies(getHeaders().get(HEADER_SET_COOKIE)));
            get(resultUrl.getPath().substring(1), paramsMap(resultUrl.getQuery()), headersResume, List.of(Response.Status.FOUND), String.class, -1);

            if (!getHeaders().containsKey(HEADER_LOCATION)) {
                throw new UnsupportedOperationException(String.format("Header '%s' is missing in response /authorize/resume", HEADER_LOCATION));
            }
            String code = extractFirstGroup(getHeaders().getFirst(HEADER_LOCATION), "code=([^&]+)");
            LOG.debug("code: {}", code);

            // --- Step 7: Get access_token and refresh_token ---
            return post("oauth/token", Map.of("grant_type", "authorization_code", PARAM_CLIENT_ID, clientId, "code", code, "code_verifier", codeVerifier, "redirect_uri", redirectUri), Tokens.class);
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public Tokens refresh(String clientId, String refreshToken) {
        return post("oauth/token", Map.of("grant_type", "refresh_token", PARAM_CLIENT_ID, clientId, "refresh_token", refreshToken), Tokens.class);
    }

    private static String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static Map<String, Object> paramsMap(String params) {
        Map<String, Object> paramsMap = new HashMap<>();
        Arrays.stream(params.split("&")).forEach(p -> paramsMap.put(p.split("=")[0], p.split("=")[1]));
        return paramsMap;
    }

    private static String extractFirstGroup(String text, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        if (m.find())
            return m.group(1);
        return null;
    }

    private static String joinCookies(List<String> setCookieHeaders) {
        // keep only the name=value part before the first ';' and join with "; "
        List<String> parts = new ArrayList<>();
        for (String s : setCookieHeaders) {
            if (s == null || s.isEmpty())
                continue;
            String[] split = s.split(";", 2);
            parts.add(split[0]);
        }
        return String.join("; ", parts);
    }
}
