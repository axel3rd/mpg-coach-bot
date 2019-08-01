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

        // Check log config and reset log test
        ConsoleTestAppender.checkLogBinding();
        ConsoleTestAppender.logTestReset();
    }

    protected final static Config getConfig() {
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

    /**
     * Get log (stderr) generated during method test execution
     * 
     * @return String
     */
    protected String getLogErr() {
        return ConsoleTestAppender.getLogErr().toString();
    }
}
