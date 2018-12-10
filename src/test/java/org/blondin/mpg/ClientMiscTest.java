package org.blondin.mpg;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import java.io.File;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class ClientMiscTest {

    @Rule
    public WireMockRule server = new WireMockRule(options().dynamicPort().portNumber());

    @Before
    public void setUp() {
        for (File file : new File(System.getProperty("java.io.tmpdir")).listFiles((d, name) -> name.startsWith("mpg-coach-bot-httplocalhost"))) {
            file.delete();
        }
    }

    @Test
    public void testErrorContentMock() throws Exception {
        final String content = "{ \"msg\": \"foobar\"}";
        final String path = "/api/test";
        stubFor(get(path).willReturn(aResponse().withStatus(400).withHeader("Content-Type", "application/json").withBody(content)));
        AbstractClient client = new AbstractClient() {
        };
        String url = "http://localhost:" + server.port();
        client.setUrl(url);

        try {
            client.get(path, String.class);
            Assert.fail("Should fail");
        } catch (UnsupportedOperationException e) {
            String s = e.getMessage();
            Assert.assertTrue(s, s.contains("foobar"));
        }

    }

    @Test
    public void testErrorNoContentMock() throws Exception {
        final String path = "/api/test";
        stubFor(get(path).willReturn(aResponse().withStatus(400)));
        AbstractClient client = new AbstractClient() {
        };
        String url = "http://localhost:" + server.port();
        client.setUrl(url);

        try {
            client.get(path, String.class);
            Assert.fail("Should fail");
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals("Unsupported status code: 400 Bad Request", e.getMessage());
        }

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCacheObjMock() throws Exception {
        final String content = "{ \"key\": \"value\"}";
        final String path = "/api/test";
        stubFor(get(path).willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(content)));
        AbstractClient client = new AbstractClient() {
        };
        String url = "http://localhost:" + server.port();
        client.setUrl(url);

        File cacheFile = AbstractClient.getCacheFile(url, path);
        Assert.assertFalse(cacheFile.exists());
        // First call with no cache
        Map<String, String> map = client.get(path, Map.class);
        Assert.assertNotNull(map);
        Assert.assertNotNull(map.get("key"));
        Assert.assertEquals("value", map.get("key"));

        // Second call with infinite cache
        map = client.get(path, Map.class, 0);
        Assert.assertNotNull(map);
        Assert.assertTrue(cacheFile.exists());
        long saveTime = cacheFile.lastModified();
        // Third call with infinite cache, previous file is used
        map = client.get(path, Map.class, 0);
        Assert.assertNotNull(map);
        Assert.assertTrue(cacheFile.exists());
        Assert.assertEquals(saveTime, cacheFile.lastModified());
        // Last call with short cache time (File.lastModified could be second), the file should be updated
        Thread.sleep(2000); // NOSONAR : Sleep wanted
        map = client.get(path, Map.class, 1);
        Assert.assertNotNull(map);
        Assert.assertTrue(cacheFile.exists());
        Assert.assertTrue(String.format("Saved time: %s, last time: %s", saveTime, cacheFile.lastModified()), cacheFile.lastModified() > saveTime);
    }

    @Test
    public void testCacheStringMock() throws Exception {
        final String content = "Hello";
        final String path = "/api/test";

        stubFor(get(path).willReturn(aResponse().withHeader("Content-Type", "text/html").withBody(content)));

        AbstractClient client = new AbstractClient() {
        };
        String url = "http://localhost:" + server.port();
        client.setUrl(url);

        File cacheFile = AbstractClient.getCacheFile(url, path);
        Assert.assertFalse(cacheFile.exists());
        // First call with no cache
        Assert.assertEquals("Hello", client.get(path, String.class));
        // Second call with infinite cache
        Assert.assertEquals("Hello", client.get(path, String.class, 0));
        Assert.assertTrue(cacheFile.exists());
        long saveTime = cacheFile.lastModified();
        // Third call with infinite cache, previous file is used
        Assert.assertEquals("Hello", client.get(path, String.class, 0));
        Assert.assertTrue(cacheFile.exists());
        Assert.assertEquals(saveTime, cacheFile.lastModified());
        // Last call with short cache time (File.lastModified could be second), the file should be updated
        Thread.sleep(2000); // NOSONAR : Sleep wanted
        Assert.assertEquals("Hello", client.get(path, String.class, 1));
        Assert.assertTrue(cacheFile.exists());
        Assert.assertTrue(String.format("Saved time: %s, last time: %s", saveTime, cacheFile.lastModified()), cacheFile.lastModified() > saveTime);
    }
}
