
package org.blondin.mpg;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.blondin.mpg.config.Config;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import jakarta.ws.rs.ProcessingException;

public class SslCertificateTest {

    @Rule
    public WireMockRule server = new WireMockRule(options().dynamicHttpsPort());

    @Test
    public void testSslProblem() {

        final String content = "{ \"key\": \"value\"}";
        final String url = "/api/test";

        stubFor(get(url).willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(content)));

        AbstractClient client = new AbstractClient(null) {
        };
        client.setUrl("https://localhost:" + server.httpsPort());
        try {
            client.get(url, String.class);
            Assert.fail("No valid certificate, and no ssl check disabled");
        } catch (ProcessingException e) {
            Assert.assertTrue("Certificate Error", e.getMessage().contains("SSLHandshakeException"));
        }
    }

    @Test
    public void testSslDisable() {
        final String content = "{ \"key\": \"value\"}";
        final String url = "/api/test";

        stubFor(get(url).willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(content)));

        Config config = mock(Config.class);
        doReturn(false).when(config).isSslCertificatesCheck();
        AbstractClient client = new AbstractClient(config) {
        };
        client.setUrl("https://localhost:" + server.httpsPort());

        Assert.assertEquals(content, client.get(url, String.class));
    }
}
