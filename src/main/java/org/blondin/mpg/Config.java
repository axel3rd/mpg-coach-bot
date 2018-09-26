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
        File fileConfig = new File(file);
        if (!fileConfig.exists()) {
            fileConfig = new File(FILE_DEFAULT);
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
