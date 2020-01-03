
package org.blondin.mpg;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import javax.ws.rs.ProcessingException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class SslCertificateTest {

    @Rule
    public WireMockRule server = new WireMockRule(options().dynamicHttpsPort());

    @Test
    public void testSslProblem() {
        try {
            final String content = "{ \"key\": \"value\"}";
            final String url = "/api/test";

            stubFor(get(url).willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(content)));

            AbstractClient client = new AbstractClient() {
            };
            client.setUrl("https://localhost:" + server.httpsPort());

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

        AbstractClient client = new AbstractClient() {
        };
        client.setUrl("https://localhost:" + server.httpsPort());
        client.setSslCertificatesCheck(false);

        Assert.assertEquals(content, client.get(url, String.class));
    }
}
