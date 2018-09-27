/*
 * Creation : 26 sept. 2018
 */
package org.blondin.mpg;

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
        config.login = StringUtils.defaultIfBlank(properties.getProperty("email"), System.getenv("MPG_EMAIL"));
        config.password = StringUtils.defaultIfBlank(properties.getProperty("password"), System.getenv("MPG_PASSWORD"));
        config.leagueTest = StringUtils.defaultIfBlank(properties.getProperty("leagueTest"), System.getenv("MPG_LEAGUE_TEST"));
        if (StringUtils.isBlank(config.login) || StringUtils.isBlank(config.password)) {
            throw new UnsupportedOperationException(
                    String.format("Login and/or password can't be retrieved from file '%s' of environement variables", fileConfig.getName()));
        }
        return config;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getLeagueTest() {
        return leagueTest;
    }
}
