package org.blondin.mpg;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.blondin.mpg.config.Proxy;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class ProxyTest {

    private static HttpProxyServer proxy;

    @Rule
    public WireMockRule server = new WireMockRule(options().dynamicPort().portNumber());

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Note: proxy should be transparent, otherwise Proxy-Authenticate header is stripped
        proxy = DefaultHttpProxyServer.bootstrap().withPort(0).withTransparent(true).withFiltersSource(new HttpFiltersSourceAdapter() {
            public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                return new HttpFiltersAdapter(originalRequest) {

                    @Override
                    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
                        if (httpObject instanceof DefaultHttpRequest) {
                            DefaultHttpRequest request = (DefaultHttpRequest) httpObject;
                            // Search header 'Proxy-Authorization: Basic xxxxx'
                            // If not return 407 with header 'Proxy-Authenticate: Basic realm="mock"'
                            // Otherwise test login/password
                            String proxyAuth = request.headers().get("Proxy-Authorization");
                            if (StringUtils.isBlank(proxyAuth)
                                    || !proxyAuth.equals("Basic " + Base64.getEncoder().encodeToString("foo:bar".getBytes()))) {
                                DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                                        HttpResponseStatus.PROXY_AUTHENTICATION_REQUIRED);
                                response.headers().add("Proxy-Authenticate", "Basic realm=\"mock\"");
                                response.headers().add("Proxy-Connection", "close");
                                response.headers().add("Connection", "close");
                                response.headers().add("Content-Length", "0");
                                return response;
                            }
                        }
                        return null;
                    }
                };
            }
        }).start();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        proxy.abort();
    }

    @Test
    public void testProxyMockNoAuthent() {
        AbstractClient client = new AbstractClient() {
        };
        client.setUrl("http://localhost:" + server.port());
        client.setProxy(new Proxy("http://localhost:" + proxy.getListenAddress().getPort(), null, null));

        try {
            client.get("/", String.class);
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals("Unsupported status code: 407 Proxy Authentication Required", e.getMessage());
        }
    }

    @Test
    public void testProxyMockBadAuthent() {
        AbstractClient client = new AbstractClient() {
        };
        client.setUrl("http://localhost:" + server.port());
        client.setProxy(new Proxy("http://localhost:" + proxy.getListenAddress().getPort(), "bar", "foo"));

        try {
            client.get("/", String.class);
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals("Unsupported status code: 407 Proxy Authentication Required", e.getMessage());
        }
    }

    @Test
    public void testProxyMock() {
        final String content = "{ \"key\": \"value\"}";
        final String url = "/api/test";

        stubFor(get(url).willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(content)));

        AbstractClient client = new AbstractClient() {
        };
        client.setUrl("http://localhost:" + server.port());
        client.setProxy(new Proxy("http://localhost:" + proxy.getListenAddress().getPort(), "foo", "bar"));

        Assert.assertEquals(content, client.get(url, String.class));
    }

}
