package org.blondin.mpg;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import java.io.File;
import java.util.Locale;

import org.blondin.mpg.config.Config;
import org.blondin.mpg.test.io.ConsoleTestAppender;
import org.junit.Before;
import org.junit.Rule;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public abstract class AbstractMockTestClient {

    public static final String TESTFILES_BASE = "src/test/resources/__files";

    @Rule
    public WireMockRule server = new WireMockRule(options().dynamicPort().portNumber());

    private static final Config config = Config.build("src/test/resources/mpg.properties.here");

    @Before
    public void setUp() {
        // Force English locale, necessary for log output test (French local environment vs English Travis, ...)
        Locale.setDefault(Locale.ENGLISH);

        // Remove any local cache
        for (File file : new File(System.getProperty("java.io.tmpdir")).listFiles((d, name) -> name.startsWith("mpg-coach-bot-httplocalhost"))) {
            file.delete();
        }

        // Reset logs test of previous method
        ConsoleTestAppender.logTestReset();
    }

    protected static final Config getConfig() {
        return config;
    }

    protected final WireMockRule getServer() {
        return server;
    }

    /**
     * Get log (stdout) generated during method test execution
     * 
     * @return String
     */
    protected String getLogOut() {
        return ConsoleTestAppender.getLogOut().toString();
    }

}
