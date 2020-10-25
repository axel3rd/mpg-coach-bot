package org.blondin.mpg;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.blondin.mpg.config.Config;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class LogsTest extends AbstractMockTestClient {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @After
    public void tearDown() {
        // Re-set default log level defined in 'log4j2-test.xml' file
        Configurator.setLevel(Main.class.getPackage().getName(), Level.INFO);
    }

    @Test
    public void testLevelInfo() throws Exception {
        final String content = "{ \"msg\": \"foobar\"}";
        final String path = "/api/test";
        stubFor(get(path).willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(content)));
        AbstractClient client = new AbstractClient(null) {
        };
        String url = "http://localhost:" + server.port();
        client.setUrl(url);
        client.get(path, String.class);

        Assert.assertTrue(getLogOut(), getLogOut().isEmpty());
    }

    @Test
    public void testLevelDebug() throws Exception {

        // Initialize config with debug logs
        List<String> lines = new ArrayList<>();
        lines.add("email = foo.bar@gmail.com");
        lines.add("password = foobar");
        lines.add("logs.debug = true");
        File configFile = new File(testFolder.getRoot(), "mpg.properties.test");
        FileUtils.writeLines(configFile, lines);

        Config.build(configFile.getPath());

        final String content = "{ \"msg\": \"foobar\"}";
        final String path = "/api/test";
        stubFor(get(path).willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(content)));
        AbstractClient client = new AbstractClient(null) {
        };
        String url = "http://localhost:" + server.port();
        client.setUrl(url);
        client.get(path, String.class);

        Assert.assertFalse(getLogOut(), getLogOut().isEmpty());
        Assert.assertTrue(getLogOut(), getLogOut().contains("Call URL time elaps ms:"));
    }
}
