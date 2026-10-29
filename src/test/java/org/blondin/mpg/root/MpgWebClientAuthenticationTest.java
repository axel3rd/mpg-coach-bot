package org.blondin.mpg.root;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.blondin.mpg.AbstractMockTestClient;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.root.model.Tokens;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.tomakehurst.wiremock.matching.ContainsPattern;

import jakarta.ws.rs.core.MediaType;

public class MpgWebClientAuthenticationTest extends AbstractMockTestClient {

    private static final String CLIENT_ID = "xxxFAKExCLIENTxIDxxx";

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    private Config getConfigTmp(String tokenAccess, String tokenRefresh) throws Exception {
        List<String> lines = new ArrayList<>();
        lines.add("email = firstName.lastName@gmail.com");
        lines.add("password = foobar");
        lines.add("# Comment begin");
        if (tokenAccess != null) {
            lines.add("token.access = " + tokenAccess);
        }
        if (tokenRefresh != null) {
            lines.add("token.refresh = " + tokenRefresh);
        }
        lines.add("# Comment end");
        lines.add("logs.debug = true");

        File configFile = new File(testFolder.getRoot(), "mpg.properties.test");
        FileUtils.writeLines(configFile, lines);

        return Config.build(configFile.getPath());
    }

    private void stubs(boolean mpgWeb, boolean mpgApiUser, boolean connectLigue1Authenticate, boolean connectLigue1Refresh) {
        stubs(mpgWeb, mpgApiUser, connectLigue1Authenticate, connectLigue1Refresh, TOKEN_ACCESS, TOKEN_REFRESH);
    }

    private void stubs(boolean mpgWeb, boolean mpgApiUser, boolean connectLigue1Authenticate, boolean connectLigue1Refresh, String tokenAccess, String tokenRefresh) {
        if (mpgWeb) {
            stubFor(get("/").willReturn(aResponse().withBodyFile("mpg.index.html")));
            stubFor(get("/_expo/static/js/web/index-cb200a50f570999bd4127e334625e04f.js").willReturn(aResponse().withBodyFile("mpg.index.js")));
        }
        if (mpgApiUser) {
            stubFor(get("/user").withHeader(MpgClient.HEADER_AUTHORIZATION_NAME, new ContainsPattern(MpgClient.HEADER_AUTHORIZATION_BEARER + tokenAccess))
                    .willReturn(aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON).withBodyFile("mpg.user.json")));
        }
        if (connectLigue1Authenticate) {
            stubFor(get(urlMatching("/authorize.*")).willReturn(aResponse().withStatus(302).withHeader("location", "/u/login?state=xxxFAKExSTATExxx").withHeader("set-cookie", "key=value; Path=/;")));
            stubFor(post(urlMatching("/u/login?.*")).withQueryParams(Map.of("state", new ContainsPattern("xxxFAKExSTATExxx"))).withFormParam("state", new ContainsPattern("xxxFAKExSTATExxx"))
                    .withFormParam("username", new ContainsPattern(getConfig().getLogin())).withFormParam("password", new ContainsPattern(getConfig().getPassword()))
                    .willReturn(aResponse().withStatus(302).withHeader("location", "/resume?dont=care").withHeader("set-cookie", "key=value; Path=/;")));
            stubFor(get(urlMatching("/resume.*")).willReturn(aResponse().withStatus(302).withHeader("location", "/?code=xxxFAKExCODExxx").withHeader("set-cookie", "key=value; Path=/;")));
            stubFor(post("/oauth/token").withHeader("content-type", new ContainsPattern(MediaType.APPLICATION_JSON))
                    .withRequestBody(new ContainsPattern("\"client_id\":\"" + CLIENT_ID + "\"").and(new ContainsPattern("\"code\":\"xxxFAKExCODExxx\""))
                            .and(new ContainsPattern("\"grant_type\":\"authorization_code\"")
                                    .and(new ContainsPattern("\"redirect_uri\":\"https://mpg.football\"").and(new ContainsPattern("\"code_verifier\":")))))
                    .willReturn(aResponse().withHeader("content-type", MediaType.APPLICATION_JSON).withBody("{\"access_token\":\"" + tokenAccess + "\",\"refresh_token\":\"" + tokenRefresh + "\"}")));
        }
        if (connectLigue1Refresh) {
            stubFor(post("/oauth/token").withHeader("content-type", new ContainsPattern(MediaType.APPLICATION_JSON))
                    .withRequestBody(new ContainsPattern("\"client_id\":\"" + CLIENT_ID + "\"").and(new ContainsPattern("\"refresh_token\":\"" + tokenRefresh + "\""))
                            .and(new ContainsPattern("\"grant_type\":\"refresh_token\"")))
                    .willReturn(
                            aResponse().withHeader("content-type", MediaType.APPLICATION_JSON).withBody("{\"access_token\":\"" + tokenAccess + "\",\"refresh_token\":\"" + tokenRefresh + "next\"}")));
        }
    }

    @Test
    public void testSignInTokenSuccessReuse() throws Exception {
        Config config = getConfigTmp(TOKEN_ACCESS, TOKEN_REFRESH);
        String url = "http://localhost:" + server.port();

        stubs(true, true, true, false);

        MpgClient client = MpgClient.build(config, url);
        client.signIn(config, AuthentMpgWebClient.build(config, url));

        Assert.assertFalse(getLogOut(), getLogOut().contains("Authenticate with login/password on web OIDC"));

        Properties props = new Properties();
        try (InputStream is = new FileInputStream(config.getFileLocation())) {
            props.load(is);
            Assert.assertEquals(TOKEN_ACCESS, props.getProperty("token.access"));
            Assert.assertEquals(TOKEN_REFRESH, props.getProperty("token.refresh"));
        }
        String configFileContent = Files.readString(Path.of(config.getFileLocation().getAbsolutePath()));
        Assert.assertTrue(configFileContent, configFileContent.contains("# Comment begin"));
        Assert.assertTrue(configFileContent, configFileContent.contains("# Comment end"));
        Assert.assertTrue(configFileContent, configFileContent.contains("logs.debug = true"));
    }

    @Test
    public void testSignInTokenEmpty() throws Exception {
        Config config = getConfigTmp(null, null);
        String url = "http://localhost:" + server.port();

        stubs(true, true, true, false);

        MpgClient client = MpgClient.build(config, url);
        client.signIn(config, AuthentMpgWebClient.build(config, url));

        Assert.assertTrue(getLogOut(), getLogOut().contains("Authenticate with login/password on web OIDC"));

        Properties props = new Properties();
        try (InputStream is = new FileInputStream(config.getFileLocation())) {
            props.load(is);
            Assert.assertEquals(TOKEN_ACCESS, props.getProperty("token.access"));
            Assert.assertEquals(TOKEN_REFRESH, props.getProperty("token.refresh"));
        }
        String configFileContent = Files.readString(Path.of(config.getFileLocation().getAbsolutePath()));
        Assert.assertTrue(configFileContent, configFileContent.contains("# Comment begin"));
        Assert.assertTrue(configFileContent, configFileContent.contains("# Comment end"));
        Assert.assertTrue(configFileContent, configFileContent.contains("logs.debug = true"));
        Assert.assertTrue(configFileContent, configFileContent.endsWith("token.refresh = " + TOKEN_REFRESH + System.lineSeparator()));
    }

    @Test
    public void testSignInTokenBlank() throws Exception {
        Config config = getConfigTmp("", "");
        String url = "http://localhost:" + server.port();

        stubs(true, true, true, false);

        MpgClient client = MpgClient.build(config, url);
        client.signIn(config, AuthentMpgWebClient.build(config, url));

        Assert.assertTrue(getLogOut(), getLogOut().contains("Authenticate with login/password on web OIDC"));

        Properties props = new Properties();
        try (InputStream is = new FileInputStream(config.getFileLocation())) {
            props.load(is);
            Assert.assertEquals(TOKEN_ACCESS, props.getProperty("token.access"));
            Assert.assertEquals(TOKEN_REFRESH, props.getProperty("token.refresh"));
        }
        String configFileContent = Files.readString(Path.of(config.getFileLocation().getAbsolutePath()));
        Assert.assertTrue(configFileContent, configFileContent.contains("# Comment begin"));
        Assert.assertTrue(configFileContent, configFileContent.contains("# Comment end"));
        Assert.assertTrue(configFileContent, configFileContent.endsWith("logs.debug = true" + System.lineSeparator()));
    }

    private void testSignInTokenInvalid(String accessToken) throws Exception {
        Config config = getConfigTmp(accessToken, TOKEN_REFRESH);
        String url = "http://localhost:" + server.port();

        stubs(true, true, true, false);

        MpgClient client = MpgClient.build(config, url);
        client.signIn(config, AuthentMpgWebClient.build(config, url));

        Assert.assertTrue(getLogOut(), getLogOut().contains("Authenticate with login/password on web OIDC"));

        Properties props = new Properties();
        try (InputStream is = new FileInputStream(config.getFileLocation())) {
            props.load(is);
            Assert.assertEquals(TOKEN_ACCESS, props.getProperty("token.access"));
            Assert.assertEquals(TOKEN_REFRESH, props.getProperty("token.refresh"));
        }
        String configFileContent = Files.readString(Path.of(config.getFileLocation().getAbsolutePath()));
        Assert.assertTrue(configFileContent, configFileContent.contains("# Comment begin"));
        Assert.assertTrue(configFileContent, configFileContent.contains("# Comment end"));
        Assert.assertTrue(configFileContent, configFileContent.endsWith("logs.debug = true" + System.lineSeparator()));
    }

    @Test
    public void testSignInTokenInvalid1() throws Exception {
        testSignInTokenInvalid("ThisIsAnInvalidToken");
    }

    @Test
    public void testSignInTokenInvalid2() throws Exception {
        testSignInTokenInvalid("ThisIs.AnInvalidToken");
    }

    @Test
    public void testSignInTokenInvalid3() throws Exception {
        testSignInTokenInvalid("ThisIs.AnInvalid.Token");
    }

    @Test
    public void testSignInTokenExpired() throws Exception {
        // Set an expiration of one second in past
        String[] accessTokenParts = TOKEN_ACCESS.split("\\.");
        String jsonData = new String(Base64.getDecoder().decode(accessTokenParts[1]), StandardCharsets.UTF_8);
        jsonData = jsonData.replace("9999999999", String.valueOf(System.currentTimeMillis() / 1000 - 1));
        jsonData = Base64.getEncoder().encodeToString(jsonData.getBytes());

        Config config = getConfigTmp(accessTokenParts[0] + "." + jsonData + "." + accessTokenParts[0], TOKEN_REFRESH);
        String url = "http://localhost:" + server.port();

        stubs(true, true, true, false);

        MpgClient client = MpgClient.build(config, url);
        client.signIn(config, AuthentMpgWebClient.build(config, url));

        Assert.assertTrue(getLogOut(), getLogOut().contains("Authenticate with login/password on web OIDC"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("Test if token is expired in 0 days: true"));

        Properties props = new Properties();
        try (InputStream is = new FileInputStream(config.getFileLocation())) {
            props.load(is);
            Assert.assertEquals(TOKEN_ACCESS, props.getProperty("token.access"));
            Assert.assertEquals(TOKEN_REFRESH, props.getProperty("token.refresh"));
        }
        String configFileContent = Files.readString(Path.of(config.getFileLocation().getAbsolutePath()));
        Assert.assertTrue(configFileContent, configFileContent.contains("# Comment begin"));
        Assert.assertTrue(configFileContent, configFileContent.contains("# Comment end"));
        Assert.assertTrue(configFileContent, configFileContent.endsWith("logs.debug = true" + System.lineSeparator()));
    }

    @Test
    public void testSignInTokenShouldRefresh() throws Exception {
        // Set an expiration in 2 days so to renew
        String[] accessTokenParts = TOKEN_ACCESS.split("\\.");
        String jsonData = new String(Base64.getDecoder().decode(accessTokenParts[1]), StandardCharsets.UTF_8);
        jsonData = jsonData.replace("9999999999", String.valueOf(System.currentTimeMillis() / 1000 + 2 * 86400));
        jsonData = Base64.getEncoder().encodeToString(jsonData.getBytes());

        Config config = getConfigTmp(accessTokenParts[0] + "." + jsonData + "." + accessTokenParts[0], TOKEN_REFRESH);
        String url = "http://localhost:" + server.port();

        stubs(true, true, false, true, TOKEN_ACCESS + "reNeWed", TOKEN_REFRESH);

        MpgClient client = MpgClient.build(config, url);
        client.signIn(config, AuthentMpgWebClient.build(config, url));

        Assert.assertFalse(getLogOut(), getLogOut().contains("Authenticate with login/password on web OIDC"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("Test if token is expired in 0 days: false"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("Test if token is expired in 7 days: true"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("Refreshing tokens"));

        Properties props = new Properties();
        try (InputStream is = new FileInputStream(config.getFileLocation())) {
            props.load(is);
            Assert.assertEquals(TOKEN_ACCESS + "reNeWed", props.getProperty("token.access"));
            Assert.assertEquals(TOKEN_REFRESH + "next", props.getProperty("token.refresh"));
        }

        String configFileContent = Files.readString(Path.of(config.getFileLocation().getAbsolutePath()));
        Assert.assertTrue(configFileContent, configFileContent.contains("# Comment begin"));
        Assert.assertTrue(configFileContent, configFileContent.contains("# Comment end"));
        Assert.assertTrue(configFileContent, configFileContent.endsWith("logs.debug = true" + System.lineSeparator()));
    }

    @Test
    public void testSignInTokenShouldRefreshForbidden() throws Exception {
        // Set an expiration in 2 days so to renew
        String[] accessTokenParts = TOKEN_ACCESS.split("\\.");
        String jsonData = new String(Base64.getDecoder().decode(accessTokenParts[1]), StandardCharsets.UTF_8);
        jsonData = jsonData.replace("9999999999", String.valueOf(System.currentTimeMillis() / 1000 + 2 * 86400));
        jsonData = Base64.getEncoder().encodeToString(jsonData.getBytes());

        Config config = getConfigTmp(accessTokenParts[0] + "." + jsonData + "." + accessTokenParts[0], TOKEN_REFRESH);
        String url = "http://localhost:" + server.port();

        // refreshToken endpoint return 403 in this case
        stubs(true, true, false, false, TOKEN_ACCESS + "reNeWed", TOKEN_REFRESH);
        stubFor(post("/oauth/token").willReturn(aResponse().withStatus(403)));

        try {
            MpgClient.build(config, url);
            Assert.fail("Should have failed with 403");
        } catch (UnsupportedOperationException e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().contains("Forbidden URL"));
        }

        Assert.assertFalse(getLogOut(), getLogOut().contains("Authenticate with login/password on web OIDC"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("Test if token is expired in 0 days: false"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("Test if token is expired in 7 days: true"));
        Assert.assertTrue(getLogOut(), getLogOut().contains("Refreshing tokens"));
        Assert.assertTrue(getLogOut(), getLogOut().contains(
                "There is a problem with Bearer token. It has been removed from configuration file. Please retry to run program to enforce renew authentication process with login/password"));

        Properties props = new Properties();
        try (InputStream is = new FileInputStream(config.getFileLocation())) {
            props.load(is);
            Assert.assertEquals("", props.getProperty("token.access"));
            Assert.assertEquals("", props.getProperty("token.refresh"));
        }

        String configFileContent = Files.readString(Path.of(config.getFileLocation().getAbsolutePath()));
        Assert.assertTrue(configFileContent, configFileContent.contains("# Comment begin"));
        Assert.assertTrue(configFileContent, configFileContent.contains("# Comment end"));
        Assert.assertTrue(configFileContent, configFileContent.endsWith("logs.debug = true" + System.lineSeparator()));
    }

    @Test
    public void testSignInTokenFromConfigBad() throws Exception {
        stubFor(get("/user").willReturn(aResponse().withStatus(401)));
        Config config = getConfigTmp(TOKEN_ACCESS, TOKEN_REFRESH);
        String url = "http://localhost:" + server.port();
        try {
            MpgClient.build(config, url);
            Assert.fail("Should have failed with 401");
        } catch (UnsupportedOperationException e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().contains("401"));
        }
        Assert.assertFalse(getLogOut(), getLogOut().contains("Authenticate with login/password on web OIDC"));
        Assert.assertTrue(getLogOut(), getLogOut().contains(
                "There is a problem with Bearer token. It has been removed from configuration file. Please retry to run program to enforce renew authentication process with login/password"));

        Properties props = new Properties();
        try (InputStream is = new FileInputStream(config.getFileLocation())) {
            props.load(is);
            Assert.assertEquals("", props.getProperty("token.access"));
            Assert.assertEquals("", props.getProperty("token.refresh"));
        }
        String configFileContent = Files.readString(Path.of(config.getFileLocation().getAbsolutePath()));
        Assert.assertTrue(configFileContent, configFileContent.contains("# Comment begin"));
        Assert.assertTrue(configFileContent, configFileContent.contains("# Comment end"));
        Assert.assertTrue(configFileContent, configFileContent.endsWith("logs.debug = true" + System.lineSeparator()));
    }

    @Test
    public void testGetClientId() {
        Config config = getConfig();
        String url = "http://localhost:" + server.port();

        stubs(true, false, false, false);

        AuthentMpgWebClient client = AuthentMpgWebClient.build(config, url);
        Assert.assertEquals(CLIENT_ID, client.getClientId());
    }

    @Test
    public void testOidcSuccess() {
        Config config = getConfig();
        String url = "http://localhost:" + server.port();

        stubs(false, false, true, false);

        AuthentConnectLigue1Client client = AuthentConnectLigue1Client.build(config, url);
        Tokens us = client.authenticate(CLIENT_ID, "https://mpg.football", config.getLogin(), config.getPassword());
        Assert.assertNotNull(us);
        Assert.assertEquals(getConfig().getTokenAccess(), us.getAccessToken());
        Assert.assertEquals(getConfig().getTokenRefresh(), us.getRefreshToken());
    }

    @Test
    public void testOidcInvalidPassword() {
        Config config = getConfig();
        String url = "http://localhost:" + server.port();

        stubFor(get(urlMatching("/authorize.*")).willReturn(aResponse().withStatus(302).withHeader("location", "/u/login?state=xxxFAKExSTATExxx").withHeader("set-cookie", "key=value; Path=/;")));
        stubFor(post(urlMatching("/u/login?.*")).withQueryParams(Map.of("state", new ContainsPattern("xxxFAKExSTATExxx"))).withFormParam("state", new ContainsPattern("xxxFAKExSTATExxx"))
                .withFormParam("username", new ContainsPattern("foo")).withFormParam("password", new ContainsPattern("bar"))
                .willReturn(aResponse().withStatus(400).withHeader("location", "/resume?dont=care").withHeader("set-cookie", "key=value; Path=/;")));

        AuthentConnectLigue1Client client = AuthentConnectLigue1Client.build(config, url);

        try {
            client.authenticate(CLIENT_ID, "https://mpg.football", "foo", "bar");
            Assert.fail("Unsupported auth type");
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals("Login/password is invalid", e.getMessage());
        }
    }

    @Test
    public void testOidcInvalidAuthorizeRedirection() {
        stubFor(get(urlMatching("/authorize.*")).willReturn(aResponse().withStatus(200).withHeader("location", "/u/login?state=xxxFAKExSTATExxx").withHeader("set-cookie", "key=value; Path=/;")));
        AuthentConnectLigue1Client client = AuthentConnectLigue1Client.build(getConfig(), "http://localhost:" + server.port());
        try {
            client.authenticate(CLIENT_ID, "https://mpg.football", "foo", "bar");
            Assert.fail("Want 302 redirection but got 200");
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals("Invalid reponse status code (not 302): 200", e.getMessage());
        }
    }

    @Test
    public void testOidcInvalidAuthorizeLocation() {
        stubFor(get(urlMatching("/authorize.*")).willReturn(aResponse().withStatus(302)));
        AuthentConnectLigue1Client client = AuthentConnectLigue1Client.build(getConfig(), "http://localhost:" + server.port());
        try {
            client.authenticate(CLIENT_ID, "https://mpg.football", "foo", "bar");
            Assert.fail("Want header location in response but not found");
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals("Header 'location' is missing in response /authorize", e.getMessage());
        }
    }

    @Test
    public void testOidcInvalidAuthorizeState() {
        stubFor(get(urlMatching("/authorize.*")).willReturn(aResponse().withStatus(302).withHeader("location", "/u/login?stateBad=xxxFAKExSTATExxx").withHeader("set-cookie", "key=value; Path=/;")));
        AuthentConnectLigue1Client client = AuthentConnectLigue1Client.build(getConfig(), "http://localhost:" + server.port());
        try {
            client.authenticate(CLIENT_ID, "https://mpg.football", "foo", "bar");
            Assert.fail("Want state parameter in loginUrl but not found");
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals("State parameter not found in loginUrl", e.getMessage());
        }
    }

    @Test
    public void testOidcInvalidLoginLocation() {
        Config config = getConfig();
        String url = "http://localhost:" + server.port();

        stubFor(get(urlMatching("/authorize.*")).willReturn(aResponse().withStatus(302).withHeader("location", "/u/login?state=xxxFAKExSTATExxx").withHeader("set-cookie", "key=value; Path=/;")));
        stubFor(post(urlMatching("/u/login?.*")).withQueryParams(Map.of("state", new ContainsPattern("xxxFAKExSTATExxx"))).withFormParam("state", new ContainsPattern("xxxFAKExSTATExxx"))
                .withFormParam("username", new ContainsPattern("foo")).withFormParam("password", new ContainsPattern("bar")).willReturn(aResponse().withStatus(302)));

        AuthentConnectLigue1Client client = AuthentConnectLigue1Client.build(config, url);
        try {
            client.authenticate(CLIENT_ID, "https://mpg.football", "foo", "bar");
            Assert.fail("Want state location in loginUrl but not found");
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals("Header 'location' is missing in response /u/login", e.getMessage());
        }
    }

    @Test
    public void testOidcInvalidResumeLocation() {
        Config config = getConfig();
        String url = "http://localhost:" + server.port();

        stubFor(get(urlMatching("/authorize.*")).willReturn(aResponse().withStatus(302).withHeader("location", "/u/login?state=xxxFAKExSTATExxx").withHeader("set-cookie", "key=value; Path=/;")));
        stubFor(post(urlMatching("/u/login?.*")).withQueryParams(Map.of("state", new ContainsPattern("xxxFAKExSTATExxx"))).withFormParam("state", new ContainsPattern("xxxFAKExSTATExxx"))
                .withFormParam("username", new ContainsPattern(config.getLogin())).withFormParam("password", new ContainsPattern(config.getPassword()))
                .willReturn(aResponse().withStatus(302).withHeader("location", "/resume?dont=care").withHeader("set-cookie", "key=value; Path=/;")));
        stubFor(get(urlMatching("/resume.*")).willReturn(aResponse().withStatus(302)));

        AuthentConnectLigue1Client client = AuthentConnectLigue1Client.build(config, url);
        String login = config.getLogin();
        String password = config.getPassword();
        try {
            client.authenticate(CLIENT_ID, "https://mpg.football", login, password);
            Assert.fail("Want location in resume URL but not found");
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals("Header 'location' is missing in response /authorize/resume", e.getMessage());
        }
    }
}
