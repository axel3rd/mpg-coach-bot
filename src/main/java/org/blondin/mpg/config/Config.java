package org.blondin.mpg.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.blondin.mpg.Main;
import org.blondin.mpg.stats.model.Position;

public class Config {

    private static final String FILE_DEFAULT = "mpg.properties";

    private String login;
    private String password;
    private boolean teampUpdate = false;
    private float noteTacticalSubstituteAttacker = 6;
    private float noteTacticalSubstituteMidfielder = 5;
    private float noteTacticalSubstituteDefender = 5;
    private float efficiencyCoefficientAttacker = 1.2f;
    private float efficiencyCoefficientMidfielder = 1.05f;
    private float efficiencyCoefficientDefender = 1.025f;
    private float efficiencyCoefficientGoalkeeper = 1f;
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
        configNoteTacticalSubstitute(config, properties);
        configEfficiencyCoefficient(config, properties);
        configProxy(config, properties);
        configLogs(properties);
        return config;
    }

    private static void configMain(Config config, Properties properties, File fileConfig) {
        config.login = StringUtils.defaultIfBlank(properties.getProperty("email"), System.getenv("MPG_EMAIL"));
        config.password = StringUtils.defaultIfBlank(properties.getProperty("password"), System.getenv("MPG_PASSWORD"));
        if (StringUtils.isBlank(config.login) || StringUtils.isBlank(config.password)) {
            throw new UnsupportedOperationException(
                    String.format("Login and/or password can't be retrieved from file '%s' of environement variables", fileConfig.getName()));
        }
        String teamUpdateStr = StringUtils.defaultIfBlank(properties.getProperty("team.update"), System.getenv("MPG_TEAM_UPDATE"));
        if (StringUtils.isNotBlank(teamUpdateStr)) {
            config.teampUpdate = Boolean.parseBoolean(teamUpdateStr);
        }
    }

    private static void configNoteTacticalSubstitute(Config config, Properties properties) {
        // Attacker
        String attacker = StringUtils.defaultIfBlank(properties.getProperty("tactical.substitute.attacker"),
                System.getenv("MPG_TACTICAL_SUBSTITUTE_ATTACKER"));
        if (StringUtils.isNotBlank(attacker)) {
            config.noteTacticalSubstituteAttacker = Float.parseFloat(attacker);
        }

        // Midfielder
        String midfielder = StringUtils.defaultIfBlank(properties.getProperty("tactical.substitute.midfielder"),
                System.getenv("MPG_TACTICAL_SUBSTITUTE_MIDFIELDER"));
        if (StringUtils.isNotBlank(midfielder)) {
            config.noteTacticalSubstituteMidfielder = Float.parseFloat(midfielder);
        }

        // Defender
        String defender = StringUtils.defaultIfBlank(properties.getProperty("tactical.substitute.defender"),
                System.getenv("MPG_TACTICAL_SUBSTITUTE_DEFENDER"));
        if (StringUtils.isNotBlank(defender)) {
            config.noteTacticalSubstituteDefender = Float.parseFloat(defender);
        }
    }

    private static void configEfficiencyCoefficient(Config config, Properties properties) {
        // Attacker
        String attacker = StringUtils.defaultIfBlank(properties.getProperty("efficiency.coefficient.attacker"),
                System.getenv("MPG_EFFICIENCY_COEFFICIENT_ATTACKER"));
        if (StringUtils.isNotBlank(attacker)) {
            config.efficiencyCoefficientAttacker = Float.parseFloat(attacker);
        }

        // Midfielder
        String midfielder = StringUtils.defaultIfBlank(properties.getProperty("efficiency.coefficient.midfielder"),
                System.getenv("MPG_EFFICIENCY_COEFFICIENT_MIDFIELDER"));
        if (StringUtils.isNotBlank(midfielder)) {
            config.efficiencyCoefficientMidfielder = Float.parseFloat(midfielder);
        }

        // Defender
        String defender = StringUtils.defaultIfBlank(properties.getProperty("efficiency.coefficient.defender"),
                System.getenv("MPG_EFFICIENCY_COEFFICIENT_DEFENDER"));
        if (StringUtils.isNotBlank(defender)) {
            config.efficiencyCoefficientDefender = Float.parseFloat(defender);
        }

        // Goalkeeper
        String goalkeeper = StringUtils.defaultIfBlank(properties.getProperty("efficiency.coefficient.goalkeeper"),
                System.getenv("MPG_EFFICIENCY_COEFFICIENT_GOALKEEPER"));
        if (StringUtils.isNotBlank(goalkeeper)) {
            config.efficiencyCoefficientGoalkeeper = Float.parseFloat(goalkeeper);
        }
    }

    private static void configLogs(Properties properties) {
        if ("true".equals(StringUtils.defaultIfBlank(properties.getProperty("logs.debug"), System.getenv("MPG_LOGS_DEBUG")))) {
            Logger.getLogger(Main.class.getPackage().getName()).setLevel(Level.DEBUG);
        }
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

    public boolean isTeampUpdate() {
        return teampUpdate;
    }

    public float getNoteTacticalSubstituteAttacker() {
        return noteTacticalSubstituteAttacker;
    }

    public float getNoteTacticalSubstituteMidfielder() {
        return noteTacticalSubstituteMidfielder;
    }

    public float getNoteTacticalSubstituteDefender() {
        return noteTacticalSubstituteDefender;
    }

    public float getEfficiencyCoefficient(Position position) {
        switch (position) {
        case A:
            return efficiencyCoefficientAttacker;
        case M:
            return efficiencyCoefficientMidfielder;
        case D:
            return efficiencyCoefficientDefender;
        case G:
            return efficiencyCoefficientGoalkeeper;
        default:
            throw new UnsupportedOperationException(String.format("Position not supported: %s", position));
        }
    }

    public Proxy getProxy() {
        return proxy;
    }

}
