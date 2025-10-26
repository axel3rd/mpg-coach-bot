package org.blondin.mpg.root;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.UUID;

import org.blondin.mpg.AbstractMockTestClient;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.root.model.UserSignIn;
import org.junit.Assert;
import org.junit.Test;

import com.github.tomakehurst.wiremock.matching.ContainsPattern;

public class MpgWebClientTest extends AbstractMockTestClient {

    @Test
    public void testSinIn() {
        Config config = getConfig();
        String url = "http://localhost:" + server.port();
        String randomUUID = UUID.randomUUID().toString();

        // --- Step 1: Initiate auth with MPG (POST form) ---
        stubFor(post(urlMatching("/auth?.*")).withQueryParams(Map.of("_data", new ContainsPattern("routes/__home/__auth/auth"), "ext-amplitudeId", new ContainsPattern(randomUUID)))
                .withFormParam("email", new ContainsPattern(config.getLogin())).withFormParam("password", new ContainsPattern(config.getPassword()))
                .willReturn(aResponse().withStatus(204).withHeader("x-remix-redirect", url
                        + "/authorize?redirect_uri=https%3A%2F%2Fmpg.football%2Fauth%2Fcallback&response_type=code&response_mode=form_post&client_id=xxxFAKExCLIENTxIDxxx&scope=offline_access+openid+profile+email&audience=https%3A%2F%2Fmpg.ligue1.fr&ext-amplitudeId=tharick-test&ui_locales=fr")));

        // --- Step 2: Follow redirect to Ligue1 OAuth (GET) ---
        stubFor(get(urlMatching("/authorize?.*")).willReturn(aResponse().withStatus(302).withHeader("location", "/u/login?state=xxxFAKExSTATExxx").withHeader("set-cookie",
                "did=xxFAKExDIDxxx; Path=/; Expires=Sun, 25 Oct 2026 23:23:00 GMT; HttpOnly; Secure; SameSite=None, auth0=xxxFAKExAUTHxxx; Path=/; Expires=Tue, 28 Oct 2025 17:23:00 GMT; HttpOnly; Secure; SameSite=None, did_compat=xxFAKExDIDcOMPATxxx; Path=/; Expires=Sun, 25 Oct 2026 23:23:00 GMT; HttpOnly; Secure, auth0_compat=xxFAKExAUTHxCOMPATxxx; Path=/; Expires=Tue, 28 Oct 2025 17:23:00 GMT; HttpOnly; Secure, __cf_bm=xxxFAKExBMxxx; path=/; expires=Sat, 25-Oct-25 17:53:00 GMT; domain=.connect.ligue1.fr; HttpOnly; Secure; SameSite=None")));

        // --- Step 3: Submit credentials to Ligue1 login (POST) ---
        stubFor(post(urlMatching("/u/login?.*")).withQueryParams(Map.of("state", new ContainsPattern("xxxFAKExSTATExxx"))).withFormParam("state", new ContainsPattern("xxxFAKExSTATExxx"))
                .withFormParam("username", new ContainsPattern(config.getLogin())).withFormParam("password", new ContainsPattern(config.getPassword()))
                .willReturn(aResponse().withStatus(302).withHeader("location", "/authorize/resume?state=xxxFAKExSTATExSECONDxxx").withHeader("set-cookie",
                        "did=xxxDICxxx; Path=/; Expires=Sun, 25 Oct 2026 23:23:00 GMT; HttpOnly; Secure; SameSite=None, auth0=xxxAUTH0xxx; Path=/; Expires=Mon, 02 Feb 2026 17:23:00 GMT; HttpOnly; Secure; SameSite=None, did_compat=xxFAKExDIDcOMPATxxx; Path=/; Expires=Sun, 25 Oct 2026 23:23:00 GMT; HttpOnly; Secure, auth0_compat=xxFAKExAUTHxCOMPATxxx; Path=/; Expires=Mon, 02 Feb 2026 17:23:00 GMT; HttpOnly; Secure")));

        // --- Step 4: Get authorization code (GET resume) ---
        stubFor(get(urlMatching("/u/login?.*")).willReturn(aResponse().withStatus(200).withBody(
                "<html><head><title>Submit This Form</title><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"></head><body onload=\"javascript:document.forms[0].submit()\"><form method=\"post\" action=\"https://mpg.football/auth/callback\"><input type=\"hidden\" name=\"code\" value=\"xxxFAKExCODExxx\"/></form></body></html>")));

        // --- Step 5: Exchange code for session (POST to callback) ---
        stubFor(post("/auth/callback").withFormParam("code", new ContainsPattern("xxxFAKExCODExxx"))
                .willReturn(aResponse().withStatus(302).withHeader("location", "/dashboard?auth=success").withHeader("set-cookie", "__session=xxxSESSIONxIDxxx; Path=/; HttpOnly; Secure")));

        // --- Step 6: Extract token from dashboard ---
        stubFor(get(urlMatching("/dashboard?.*")).withQueryParam("_data", new ContainsPattern("root")).willReturn(aResponse().withStatus(200).withHeader("content-type", "application/json").withBody(
                "{\"locale\":\"fr\",\"title\":\"Défie tes amis avec ta propre équipe de football\",\"description\":\"Chaque week-end, tu défies un ami dans un match MPG dont le résultat dépend des VRAIES performances de vos VRAIS joueurs du championnat de football. Le meilleur jeu de fantasy football qui existe.\",\"token\":\"xxxTOKENxxx\",\"appEnv\":\"production\",\"apiUrl\":\"https://api.mpg.football\",\"chatApiUrl\":\"https://chat.api.mpg.football\",\"trackingUrl\":\"https://europe-west1-mpg-workers.cloudfunctions.net\",\"fanbaseUrl\":\"https://is-fans-prod-fanbase-api.azurewebsites.net\",\"stripePublicKey\":\"xxstripePublicKeyxxx\",\"giphyApiKey\":\"xxxgiphyApiKeyxxx\",\"profileDomainUrl\":\"https://profile.ligue1.fr\",\"websiteBaseUrl\":\"https://mpg.football\",\"adjustToken\":\"xxxadjustTokenxxx\",\"permissions\":[]}")));

        AuthentMpgWebClient client = AuthentMpgWebClient.build(config, url);
        UserSignIn usi = client.authenticate(config.getLogin(), config.getPassword(), randomUUID);
        Assert.assertNotNull(usi);
        Assert.assertNotNull(usi.getToken());
        Assert.assertEquals("xxxTOKENxxx", usi.getToken());
    }

    @Test
    public void testBadStep1() {
        Config config = getConfig();
        String url = "http://localhost:" + server.port();
        String randomUUID = UUID.randomUUID().toString();

        // --- Step 1: Initiate auth with MPG (POST form) ---
        stubFor(post(urlMatching("/auth?.*")).withQueryParams(Map.of("_data", new ContainsPattern("routes/__home/__auth/auth"), "ext-amplitudeId", new ContainsPattern(randomUUID)))
                .withFormParam("email", new ContainsPattern(config.getLogin())).withFormParam("password", new ContainsPattern(config.getPassword())).willReturn(aResponse().withStatus(204)));

        String login = config.getLogin();
        String password = config.getPassword();
        AuthentMpgWebClient client = AuthentMpgWebClient.build(config, url);
        try {
            client.authenticate(login, password, randomUUID);
            Assert.fail("Incorrect Header");
        } catch (UnsupportedOperationException e) {
            assertEquals("Header 'x-remix-redirect' is missing in first oidc authentication step", e.getMessage());
        }
    }

    @Test
    public void testBadStep2SatusCode() {
        Config config = getConfig();
        String url = "http://localhost:" + server.port();
        String randomUUID = UUID.randomUUID().toString();

        // --- Step 1: Initiate auth with MPG (POST form) ---
        stubFor(post(urlMatching("/auth?.*")).withQueryParams(Map.of("_data", new ContainsPattern("routes/__home/__auth/auth"), "ext-amplitudeId", new ContainsPattern(randomUUID)))
                .withFormParam("email", new ContainsPattern(config.getLogin())).withFormParam("password", new ContainsPattern(config.getPassword()))
                .willReturn(aResponse().withStatus(204).withHeader("x-remix-redirect", url
                        + "/authorize?redirect_uri=https%3A%2F%2Fmpg.football%2Fauth%2Fcallback&response_type=code&response_mode=form_post&client_id=xxxFAKExCLIENTxIDxxx&scope=offline_access+openid+profile+email&audience=https%3A%2F%2Fmpg.ligue1.fr&ext-amplitudeId=tharick-test&ui_locales=fr")));

        // --- Step 2: Follow redirect to Ligue1 OAuth (GET) ---
        stubFor(get(urlMatching("/authorize?.*")).willReturn(aResponse().withStatus(200)));

        String login = config.getLogin();
        String password = config.getPassword();
        AuthentMpgWebClient client = AuthentMpgWebClient.build(config, url);
        try {
            client.authenticate(login, password, randomUUID);
            Assert.fail("Incorrect status code");
        } catch (UnsupportedOperationException e) {
            assertEquals("Invalid reponse status code (not 302): 200", e.getMessage());
        }
    }

    @Test
    public void testBadStep2Headerlocation() {
        Config config = getConfig();
        String url = "http://localhost:" + server.port();
        String randomUUID = UUID.randomUUID().toString();

        // --- Step 1: Initiate auth with MPG (POST form) ---
        stubFor(post(urlMatching("/auth?.*")).withQueryParams(Map.of("_data", new ContainsPattern("routes/__home/__auth/auth"), "ext-amplitudeId", new ContainsPattern(randomUUID)))
                .withFormParam("email", new ContainsPattern(config.getLogin())).withFormParam("password", new ContainsPattern(config.getPassword()))
                .willReturn(aResponse().withStatus(204).withHeader("x-remix-redirect", url
                        + "/authorize?redirect_uri=https%3A%2F%2Fmpg.football%2Fauth%2Fcallback&response_type=code&response_mode=form_post&client_id=xxxFAKExCLIENTxIDxxx&scope=offline_access+openid+profile+email&audience=https%3A%2F%2Fmpg.ligue1.fr&ext-amplitudeId=tharick-test&ui_locales=fr")));

        // --- Step 2: Follow redirect to Ligue1 OAuth (GET) ---
        stubFor(get(urlMatching("/authorize?.*")).willReturn(aResponse().withStatus(302)));

        String login = config.getLogin();
        String password = config.getPassword();
        AuthentMpgWebClient client = AuthentMpgWebClient.build(config, url);
        try {
            client.authenticate(login, password, randomUUID);
            Assert.fail("Incorrect header location");
        } catch (UnsupportedOperationException e) {
            assertEquals("Header 'location' is missing in step 2 oidc authentication step", e.getMessage());
        }
    }

    @Test
    public void testBadStep2State() {
        Config config = getConfig();
        String url = "http://localhost:" + server.port();
        String randomUUID = UUID.randomUUID().toString();

        // --- Step 1: Initiate auth with MPG (POST form) ---
        stubFor(post(urlMatching("/auth?.*")).withQueryParams(Map.of("_data", new ContainsPattern("routes/__home/__auth/auth"), "ext-amplitudeId", new ContainsPattern(randomUUID)))
                .withFormParam("email", new ContainsPattern(config.getLogin())).withFormParam("password", new ContainsPattern(config.getPassword()))
                .willReturn(aResponse().withStatus(204).withHeader("x-remix-redirect", url
                        + "/authorize?redirect_uri=https%3A%2F%2Fmpg.football%2Fauth%2Fcallback&response_type=code&response_mode=form_post&client_id=xxxFAKExCLIENTxIDxxx&scope=offline_access+openid+profile+email&audience=https%3A%2F%2Fmpg.ligue1.fr&ext-amplitudeId=tharick-test&ui_locales=fr")));

        // --- Step 2: Follow redirect to Ligue1 OAuth (GET) ---
        stubFor(get(urlMatching("/authorize?.*")).willReturn(aResponse().withStatus(302).withHeader("location", "/u/login?NoState=Value")));

        String login = config.getLogin();
        String password = config.getPassword();
        AuthentMpgWebClient client = AuthentMpgWebClient.build(config, url);
        try {
            client.authenticate(login, password, randomUUID);
            Assert.fail("Incorrect state");
        } catch (UnsupportedOperationException e) {
            assertEquals("State parameter not found in loginUrl", e.getMessage());
        }
    }

    @Test
    public void testBadStep3() {
        Config config = getConfig();
        String url = "http://localhost:" + server.port();
        String randomUUID = UUID.randomUUID().toString();

        // --- Step 1: Initiate auth with MPG (POST form) ---
        stubFor(post(urlMatching("/auth?.*")).withQueryParams(Map.of("_data", new ContainsPattern("routes/__home/__auth/auth"), "ext-amplitudeId", new ContainsPattern(randomUUID)))
                .withFormParam("email", new ContainsPattern(config.getLogin())).withFormParam("password", new ContainsPattern(config.getPassword()))
                .willReturn(aResponse().withStatus(204).withHeader("x-remix-redirect", url
                        + "/authorize?redirect_uri=https%3A%2F%2Fmpg.football%2Fauth%2Fcallback&response_type=code&response_mode=form_post&client_id=xxxFAKExCLIENTxIDxxx&scope=offline_access+openid+profile+email&audience=https%3A%2F%2Fmpg.ligue1.fr&ext-amplitudeId=tharick-test&ui_locales=fr")));

        // --- Step 2: Follow redirect to Ligue1 OAuth (GET) ---
        stubFor(get(urlMatching("/authorize?.*")).willReturn(aResponse().withStatus(302).withHeader("location", "/u/login?state=xxxFAKExSTATExxx").withHeader("set-cookie",
                "did=xxFAKExDIDxxx; Path=/; Expires=Sun, 25 Oct 2026 23:23:00 GMT; HttpOnly; Secure; SameSite=None, auth0=xxxFAKExAUTHxxx; Path=/; Expires=Tue, 28 Oct 2025 17:23:00 GMT; HttpOnly; Secure; SameSite=None, did_compat=xxFAKExDIDcOMPATxxx; Path=/; Expires=Sun, 25 Oct 2026 23:23:00 GMT; HttpOnly; Secure, auth0_compat=xxFAKExAUTHxCOMPATxxx; Path=/; Expires=Tue, 28 Oct 2025 17:23:00 GMT; HttpOnly; Secure, __cf_bm=xxxFAKExBMxxx; path=/; expires=Sat, 25-Oct-25 17:53:00 GMT; domain=.connect.ligue1.fr; HttpOnly; Secure; SameSite=None")));

        // --- Step 3: Submit credentials to Ligue1 login (POST) ---
        stubFor(post(urlMatching("/u/login?.*")).withQueryParams(Map.of("state", new ContainsPattern("xxxFAKExSTATExxx"))).withFormParam("state", new ContainsPattern("xxxFAKExSTATExxx"))
                .withFormParam("username", new ContainsPattern(config.getLogin())).withFormParam("password", new ContainsPattern(config.getPassword())).willReturn(aResponse().withStatus(302)));

        String login = config.getLogin();
        String password = config.getPassword();
        AuthentMpgWebClient client = AuthentMpgWebClient.build(config, url);
        try {
            client.authenticate(login, password, randomUUID);
            Assert.fail("Incorrect header location");
        } catch (UnsupportedOperationException e) {
            assertEquals("Header 'location' is missing in step 3 oidc authentication step", e.getMessage());
        }
    }

    @Test
    public void testBadStep4() {
        Config config = getConfig();
        String url = "http://localhost:" + server.port();
        String randomUUID = UUID.randomUUID().toString();

        // --- Step 1: Initiate auth with MPG (POST form) ---
        stubFor(post(urlMatching("/auth?.*")).withQueryParams(Map.of("_data", new ContainsPattern("routes/__home/__auth/auth"), "ext-amplitudeId", new ContainsPattern(randomUUID)))
                .withFormParam("email", new ContainsPattern(config.getLogin())).withFormParam("password", new ContainsPattern(config.getPassword()))
                .willReturn(aResponse().withStatus(204).withHeader("x-remix-redirect", url
                        + "/authorize?redirect_uri=https%3A%2F%2Fmpg.football%2Fauth%2Fcallback&response_type=code&response_mode=form_post&client_id=xxxFAKExCLIENTxIDxxx&scope=offline_access+openid+profile+email&audience=https%3A%2F%2Fmpg.ligue1.fr&ext-amplitudeId=tharick-test&ui_locales=fr")));

        // --- Step 2: Follow redirect to Ligue1 OAuth (GET) ---
        stubFor(get(urlMatching("/authorize?.*")).willReturn(aResponse().withStatus(302).withHeader("location", "/u/login?state=xxxFAKExSTATExxx").withHeader("set-cookie",
                "did=xxFAKExDIDxxx; Path=/; Expires=Sun, 25 Oct 2026 23:23:00 GMT; HttpOnly; Secure; SameSite=None, auth0=xxxFAKExAUTHxxx; Path=/; Expires=Tue, 28 Oct 2025 17:23:00 GMT; HttpOnly; Secure; SameSite=None, did_compat=xxFAKExDIDcOMPATxxx; Path=/; Expires=Sun, 25 Oct 2026 23:23:00 GMT; HttpOnly; Secure, auth0_compat=xxFAKExAUTHxCOMPATxxx; Path=/; Expires=Tue, 28 Oct 2025 17:23:00 GMT; HttpOnly; Secure, __cf_bm=xxxFAKExBMxxx; path=/; expires=Sat, 25-Oct-25 17:53:00 GMT; domain=.connect.ligue1.fr; HttpOnly; Secure; SameSite=None")));

        // --- Step 3: Submit credentials to Ligue1 login (POST) ---
        stubFor(post(urlMatching("/u/login?.*")).withQueryParams(Map.of("state", new ContainsPattern("xxxFAKExSTATExxx"))).withFormParam("state", new ContainsPattern("xxxFAKExSTATExxx"))
                .withFormParam("username", new ContainsPattern(config.getLogin())).withFormParam("password", new ContainsPattern(config.getPassword()))
                .willReturn(aResponse().withStatus(302).withHeader("location", "/authorize/resume?state=xxxFAKExSTATExSECONDxxx").withHeader("set-cookie",
                        "did=xxxDICxxx; Path=/; Expires=Sun, 25 Oct 2026 23:23:00 GMT; HttpOnly; Secure; SameSite=None, auth0=xxxAUTH0xxx; Path=/; Expires=Mon, 02 Feb 2026 17:23:00 GMT; HttpOnly; Secure; SameSite=None, did_compat=xxFAKExDIDcOMPATxxx; Path=/; Expires=Sun, 25 Oct 2026 23:23:00 GMT; HttpOnly; Secure, auth0_compat=xxFAKExAUTHxCOMPATxxx; Path=/; Expires=Mon, 02 Feb 2026 17:23:00 GMT; HttpOnly; Secure")));

        // --- Step 4: Get authorization code (GET resume) ---
        stubFor(get(urlMatching("/u/login?.*")).willReturn(aResponse().withStatus(200).withBody("<html><head></body></html>")));

        String login = config.getLogin();
        String password = config.getPassword();
        AuthentMpgWebClient client = AuthentMpgWebClient.build(config, url);
        try {
            client.authenticate(login, password, randomUUID);
            Assert.fail("Incorrect HTMP content");
        } catch (UnsupportedOperationException e) {
            assertEquals("Authorization code not found on resume page (step 4)", e.getMessage());
        }
    }

    @Test
    public void testBadStep5() {
        Config config = getConfig();
        String url = "http://localhost:" + server.port();
        String randomUUID = UUID.randomUUID().toString();

        // --- Step 1: Initiate auth with MPG (POST form) ---
        stubFor(post(urlMatching("/auth?.*")).withQueryParams(Map.of("_data", new ContainsPattern("routes/__home/__auth/auth"), "ext-amplitudeId", new ContainsPattern(randomUUID)))
                .withFormParam("email", new ContainsPattern(config.getLogin())).withFormParam("password", new ContainsPattern(config.getPassword()))
                .willReturn(aResponse().withStatus(204).withHeader("x-remix-redirect", url
                        + "/authorize?redirect_uri=https%3A%2F%2Fmpg.football%2Fauth%2Fcallback&response_type=code&response_mode=form_post&client_id=xxxFAKExCLIENTxIDxxx&scope=offline_access+openid+profile+email&audience=https%3A%2F%2Fmpg.ligue1.fr&ext-amplitudeId=tharick-test&ui_locales=fr")));

        // --- Step 2: Follow redirect to Ligue1 OAuth (GET) ---
        stubFor(get(urlMatching("/authorize?.*")).willReturn(aResponse().withStatus(302).withHeader("location", "/u/login?state=xxxFAKExSTATExxx").withHeader("set-cookie",
                "did=xxFAKExDIDxxx; Path=/; Expires=Sun, 25 Oct 2026 23:23:00 GMT; HttpOnly; Secure; SameSite=None, auth0=xxxFAKExAUTHxxx; Path=/; Expires=Tue, 28 Oct 2025 17:23:00 GMT; HttpOnly; Secure; SameSite=None, did_compat=xxFAKExDIDcOMPATxxx; Path=/; Expires=Sun, 25 Oct 2026 23:23:00 GMT; HttpOnly; Secure, auth0_compat=xxFAKExAUTHxCOMPATxxx; Path=/; Expires=Tue, 28 Oct 2025 17:23:00 GMT; HttpOnly; Secure, __cf_bm=xxxFAKExBMxxx; path=/; expires=Sat, 25-Oct-25 17:53:00 GMT; domain=.connect.ligue1.fr; HttpOnly; Secure; SameSite=None")));

        // --- Step 3: Submit credentials to Ligue1 login (POST) ---
        stubFor(post(urlMatching("/u/login?.*")).withQueryParams(Map.of("state", new ContainsPattern("xxxFAKExSTATExxx"))).withFormParam("state", new ContainsPattern("xxxFAKExSTATExxx"))
                .withFormParam("username", new ContainsPattern(config.getLogin())).withFormParam("password", new ContainsPattern(config.getPassword()))
                .willReturn(aResponse().withStatus(302).withHeader("location", "/authorize/resume?state=xxxFAKExSTATExSECONDxxx").withHeader("set-cookie",
                        "did=xxxDICxxx; Path=/; Expires=Sun, 25 Oct 2026 23:23:00 GMT; HttpOnly; Secure; SameSite=None, auth0=xxxAUTH0xxx; Path=/; Expires=Mon, 02 Feb 2026 17:23:00 GMT; HttpOnly; Secure; SameSite=None, did_compat=xxFAKExDIDcOMPATxxx; Path=/; Expires=Sun, 25 Oct 2026 23:23:00 GMT; HttpOnly; Secure, auth0_compat=xxFAKExAUTHxCOMPATxxx; Path=/; Expires=Mon, 02 Feb 2026 17:23:00 GMT; HttpOnly; Secure")));

        // --- Step 4: Get authorization code (GET resume) ---
        stubFor(get(urlMatching("/u/login?.*")).willReturn(aResponse().withStatus(200).withBody(
                "<html><head><title>Submit This Form</title><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"></head><body onload=\"javascript:document.forms[0].submit()\"><form method=\"post\" action=\"https://mpg.football/auth/callback\"><input type=\"hidden\" name=\"code\" value=\"xxxFAKExCODExxx\"/></form></body></html>")));

        // --- Step 5: Exchange code for session (POST to callback) ---
        stubFor(post("/auth/callback").withFormParam("code", new ContainsPattern("xxxFAKExCODExxx"))
                .willReturn(aResponse().withStatus(302).withHeader("location", "/dashboard?auth=success").withHeader("set-cookie", "Path=/; HttpOnly; Secure")));

        String login = config.getLogin();
        String password = config.getPassword();
        AuthentMpgWebClient client = AuthentMpgWebClient.build(config, url);
        try {
            client.authenticate(login, password, randomUUID);
            Assert.fail("Incorrect cookie");
        } catch (UnsupportedOperationException e) {
            assertEquals("__session cookie not found in callback response (step 5)", e.getMessage());
        }
    }
}
