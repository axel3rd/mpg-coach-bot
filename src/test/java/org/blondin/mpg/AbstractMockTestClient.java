package org.blondin.mpg;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import java.io.File;

import org.blondin.mpg.config.Config;
import org.junit.Before;
import org.junit.Rule;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public abstract class AbstractMockTestClient {

    @Rule
    public WireMockRule server = new WireMockRule(options().dynamicPort().portNumber());

    private static final Config config = Config.build("src/test/resources/mpg.properties.here");

    @Before
    public void setUp() {
        // Remove any local cache
        for (File file : new File(System.getProperty("java.io.tmpdir")).listFiles((d, name) -> name.startsWith("mpg-coach-bot-httplocalhost"))) {
            file.delete();
        }
    }

    protected final static Config getConfig() {
        return config;
    }

    protected final WireMockRule getServer() {
        return server;
    }
}
