package org.blondin.mpg.root;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

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
                        + "/authorize?redirect_uri=https%3A%2F%2Fmpg.football%2Fauth%2Fcallback&response_type=code&response_mode=form_post&client_id=XNNUupMREjh0ULck1InJRC6gb8kyMfdg&scope=offline_access+openid+profile+email&audience=https%3A%2F%2Fmpg.ligue1.fr&ext-amplitudeId=tharick-test&ui_locales=fr")));

        // TODO other steps

        AuthentMpgWebClient client = AuthentMpgWebClient.build(config, url);
        UserSignIn usi = client.authenticate(config.getLogin(), config.getPassword(), randomUUID);
        Assert.assertNotNull(usi);
        Assert.assertNotNull(usi.getToken());
        Assert.assertTrue(usi.getToken().length() > 2000);
    }

    // TODO test for authentication errors
}
