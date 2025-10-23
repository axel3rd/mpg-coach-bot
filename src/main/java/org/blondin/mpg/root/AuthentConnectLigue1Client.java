package org.blondin.mpg.root;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.blondin.mpg.AbstractClient;
import org.blondin.mpg.config.Config;

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

    private static final String HEADER_LOCATION = "location";

    protected AuthentConnectLigue1Client(Config config) {
        super(config);
    }

    public static AuthentConnectLigue1Client build(Config config, String urlOverride) {
        AuthentConnectLigue1Client connectLigue1Client = new AuthentConnectLigue1Client(config);
        connectLigue1Client.setUrl(Objects.toString(urlOverride, "https://connect.ligue1.fr"));
        return connectLigue1Client;
    }

    public String getMpgWebSiteCode(String path, String params, String login, String password) {
        // --- Step 2: Follow redirect to Ligue1 OAuth (GET) ---
        get(path, paramsMap(params), false, String.class);
        if (getStatusCode() != Response.Status.FOUND.getStatusCode()) {
            throw new UnsupportedOperationException(String.format("Invalid reponse status code (not %s): %s", Response.Status.FOUND.getStatusCode(), getStatusCode()));
        }
        if (!getHeaders().containsKey(HEADER_LOCATION)) {
            throw new UnsupportedOperationException(String.format("Header '%s' is missing in step 2 oidc authentication step", HEADER_LOCATION));
        }
        String loginUrl = getHeaders().getFirst(HEADER_LOCATION);

        // Extract state from loginUrl
        String state = extractFirstGroup(loginUrl, "state=([^&]+)");
        if (state == null) {
            throw new UnsupportedOperationException("State parameter not found in loginUrl");
        }

        // --- Step 3: Submit credentials to Ligue1 login (POST) ---
        URI loginPostUrl = URI.create(loginUrl);

        // Build cookie header from set-cookie values of resp2
        MultivaluedMap<String, Object> headersLogin = new MultivaluedHashMap<>();
        headersLogin.add("Cookie", joinCookies(getHeaders().get("set-cookie")));

        post(loginPostUrl.getPath(), paramsMap(loginPostUrl.getQuery()), headersLogin, new Form("state", state).param("username", login).param("password", password),
                MediaType.APPLICATION_FORM_URLENCODED_TYPE, false, String.class);

        if (!getHeaders().containsKey(HEADER_LOCATION)) {
            throw new UnsupportedOperationException(String.format("Header '%s' is missing in step 3 oidc authentication step", HEADER_LOCATION));
        }

        // --- Step 4: Get authorization code (GET resume) ---
        URI resultUrl = URI.create(getHeaders().getFirst(HEADER_LOCATION));
        MultivaluedMap<String, Object> headersResume = new MultivaluedHashMap<>();
        headersResume.add("Cookie", joinCookies(getHeaders().get("set-cookie")));
        String bodyResume = get(resultUrl.getPath(), paramsMap(resultUrl.getQuery()), headersResume, String.class, -1);

        // Extract code from HTML input name="code" value="..."
        String code = extractFirstGroup(bodyResume, "name=\"code\"\\s+value=\"([^\"]+)\"");
        if (StringUtils.isBlank(code)) {
            throw new UnsupportedOperationException("Authorization code not found on resume page (step 4)");
        }
        return code;

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
