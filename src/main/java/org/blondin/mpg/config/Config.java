package org.blondin.mpg.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

public class Config {

    private static final String FILE_DEFAULT = "mpg.properties";

    private String login;
    private String password;
    private String leagueTest;
    private Proxy proxy;

    private Config() {
        super();
    }

    public static Config build(String file) {
        Config config = new Config();
        File fileConfig = new File(FILE_DEFAULT);
        if (StringUtils.isNotBlank(file) && new File(file).exists()) {
            fileConfig = new File(file);
        }
        Properties properties = new Properties();
        if (fileConfig.exists()) {
            try {
                properties.load(new FileInputStream(fileConfig));
            } catch (IOException e) {
                // Nothing
            }
        }
        configMain(config, properties, fileConfig);
        configProxy(config, properties);
        configTest(config, properties);
        return config;
    }

    private static void configMain(Config config, Properties properties, File fileConfig) {
        config.login = StringUtils.defaultIfBlank(properties.getProperty("email"), System.getenv("MPG_EMAIL"));
        config.password = StringUtils.defaultIfBlank(properties.getProperty("password"), System.getenv("MPG_PASSWORD"));
        if (StringUtils.isBlank(config.login) || StringUtils.isBlank(config.password)) {
            throw new UnsupportedOperationException(
                    String.format("Login and/or password can't be retrieved from file '%s' of environement variables", fileConfig.getName()));
        }
    }

    private static void configTest(Config config, Properties properties) {
        config.leagueTest = StringUtils.defaultIfBlank(properties.getProperty("leagueTest"), System.getenv("MPG_LEAGUE_TEST"));
    }

    private static void configProxy(Config config, Properties properties) {
        String uri = StringUtils.defaultIfBlank(properties.getProperty("proxy.uri"), System.getenv("MPG_PROXY_URI"));
        String user = StringUtils.defaultIfBlank(properties.getProperty("proxy.user"), System.getenv("MPG_PROXY_USER"));
        String password = StringUtils.defaultIfBlank(properties.getProperty("proxy.password"), System.getenv("MPG_PROXY_PASSWORD"));
        config.proxy = new Proxy(uri, user, password);
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public String getLeagueTest() {
        return leagueTest;
    }
}
