package org.blondin.mpg;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Locale;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.blondin.mpg.config.Config;
import org.blondin.mpg.test.io.ConsoleTestAppender;
import org.junit.Before;
import org.junit.Rule;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public abstract class AbstractMockTestClient {

    public static final String TESTFILES_BASE = "src/test/resources/__files";
    protected static final String TOKEN_ACCESS = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IiJ9.eyJzaWduSW5UeXBlIjoic2lnbmVkSW4iLCJ1c2VySWQiOiJ1c2VyX1hYWCIsImluaXRpYWxBdXRoMElkIjoiYXV0aDB8bXBnXzQyNDI0MiIsIm5hbWUiOiJKb2huIERvZSIsImh0dHBzOi8vY29ubmVjdC5saWd1ZTEuZnIvY2xpZW50X25hbWUiOiJNUEcgV2ViIiwiY29ubmVjdGlvblR5cGUiOiJhdXRoMCIsImh0dHBzOi8vY29ubmVjdC5saWd1ZTEuZnIvY29ubmVjdGlvbiI6ImF1dGgwIiwiaHR0cHM6Ly9jb25uZWN0LmxpZ3VlMS5mci9vcmlnaW4iOiJtcGcuZm9vdGJhbGwiLCJodHRwczovL2Nvbm5lY3QubGlndWUxLmZyL2RzcF9jbGllbnRfaWQiOiJtcGciLCJhcHBJZCI6Im1wZyIsImh0dHBzOi8vY29ubmVjdC5saWd1ZTEuZnIvY3VzdG9taXphdGlvbiI6ImJhY2tncm91bmQ9WFhYIiwiaXNzIjoiaHR0cHM6Ly9jb25uZWN0LmxpZ3VlMS5mci8iLCJzdWIiOiJhdXRoMHxtcGdfNDI0MjQyIiwiYXVkIjpbImh0dHBzOi8vbXBnLmxpZ3VlMS5mciJdLCJpYXQiOjAsImV4cCI6OTk5OTk5OTk5OSwic2NvcGUiOiJvcGVuaWQgcHJvZmlsZSBlbWFpbCBvZmZsaW5lX2FjY2VzcyIsImF6cCI6IlhYWCIsInBlcm1pc3Npb25zIjpbXX0=.42";
    protected static final String TOKEN_REFRESH = "v1.xxxREFRESHxxx";

    @Rule
    public WireMockRule server = new WireMockRule(options().dynamicPort().portNumber());

    private static final Config config = Config.build("src/test/resources/mpg.properties.here");

    static {
        // Inject fake Bearer tokens to avoid real Web authentication during tests
        try {
            Field fieldTokenAccess = Config.class.getDeclaredField("tokenAccess");
            fieldTokenAccess.setAccessible(true);
            fieldTokenAccess.set(config, TOKEN_ACCESS);

            Field fieldTokenRefresh = Config.class.getDeclaredField("tokenRefresh");
            fieldTokenRefresh.setAccessible(true);
            fieldTokenRefresh.set(config, TOKEN_REFRESH);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException("Unable to inject fake Bearer tokens to avoid real Web authentication during tests", e);
        }
    }

    @Before
    public void setUp() {
        // Force English locale, necessary for log output test (French local environment vs English GitHub Actions, ...)
        Locale.setDefault(Locale.ENGLISH);

        // Remove any local cache
        for (File file : new File(System.getProperty("java.io.tmpdir")).listFiles((d, name) -> name.startsWith("mpg-coach-bot-httplocalhost"))) {
            file.delete();
        }

        // Reset logs test of previous method
        ConsoleTestAppender.logTestReset();

        // Reset log Level to info (some test configure 'debug' level)
        Configurator.setLevel(Main.class.getPackage().getName(), Level.INFO);
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
        return ConsoleTestAppender.getLogOut();
    }

}
