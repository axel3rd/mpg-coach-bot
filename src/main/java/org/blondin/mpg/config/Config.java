package org.blondin.mpg.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.blondin.mpg.Main;
import org.blondin.mpg.root.model.Position;

public class Config {

    private static final String FILE_DEFAULT = "mpg.properties";

    private String login;
    private String password;
    private boolean teampUpdate = false;
    private boolean tacticalSubstitutesUpdate = true;
    private float noteTacticalSubstituteAttacker = 6;
    private float noteTacticalSubstituteMidfielder = 5;
    private float noteTacticalSubstituteDefender = 5;
    private float efficiencyCoefficientAttacker = 1.2f;
    private float efficiencyCoefficientMidfielder = 1.05f;
    private float efficiencyCoefficientDefender = 1.025f;
    private float efficiencyCoefficientGoalkeeper = 1f;
    private boolean transactionsProposal = true;
    private float efficiencySellAttacker = 3f;
    private float efficiencySellMidfielder = 3f;
    private float efficiencySellDefender = 3f;
    private float efficiencySellGoalkeeper = 3f;
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
        configEfficiencySell(config, properties);
        configProxy(config, properties);
        configLogs(properties);
        return config;
    }

    private static String parseString(Properties properties, String key) {
        return StringUtils.defaultIfBlank(properties.getProperty(key), System.getenv("MPG_" + key.toUpperCase().replaceAll("\\.", "_")));
    }

    private static float parseFloat(Properties properties, String key, float valueIfNotDefined) {
        String value = parseString(properties, key);
        if (StringUtils.isNotBlank(value)) {
            return Float.parseFloat(value);
        }
        return valueIfNotDefined;
    }

    private static boolean parseBoolean(Properties properties, String key, boolean valueIfNotDefined) {
        String value = parseString(properties, key);
        if (StringUtils.isNotBlank(value)) {
            return Boolean.parseBoolean(value);
        }
        return valueIfNotDefined;
    }

    private static void configMain(Config config, Properties properties, File fileConfig) {
        config.login = parseString(properties, "email");
        config.password = parseString(properties, "password");
        if (StringUtils.isBlank(config.login) || StringUtils.isBlank(config.password)) {
            throw new UnsupportedOperationException(
                    String.format("Login and/or password can't be retrieved from file '%s' of environement variables", fileConfig.getName()));
        }
        config.teampUpdate = parseBoolean(properties, "team.update", config.teampUpdate);
        config.transactionsProposal = parseBoolean(properties, "transactions.proposal", config.transactionsProposal);
    }

    private static void configNoteTacticalSubstitute(Config config, Properties properties) {
        config.tacticalSubstitutesUpdate = parseBoolean(properties, "tactical.substitutes", config.tacticalSubstitutesUpdate);
        config.noteTacticalSubstituteAttacker = parseFloat(properties, "tactical.substitute.attacker", config.noteTacticalSubstituteAttacker);
        config.noteTacticalSubstituteMidfielder = parseFloat(properties, "tactical.substitute.midfielder", config.noteTacticalSubstituteMidfielder);
        config.noteTacticalSubstituteDefender = parseFloat(properties, "tactical.substitute.defender", config.noteTacticalSubstituteDefender);
    }

    private static void configEfficiencyCoefficient(Config config, Properties properties) {
        config.efficiencyCoefficientAttacker = parseFloat(properties, "efficiency.coefficient.attacker", config.efficiencyCoefficientAttacker);
        config.efficiencyCoefficientMidfielder = parseFloat(properties, "efficiency.coefficient.midfielder", config.efficiencyCoefficientMidfielder);
        config.efficiencyCoefficientDefender = parseFloat(properties, "efficiency.coefficient.defender", config.efficiencyCoefficientDefender);
        config.efficiencyCoefficientGoalkeeper = parseFloat(properties, "efficiency.coefficient.goalkeeper", config.efficiencyCoefficientGoalkeeper);
    }

    private static void configEfficiencySell(Config config, Properties properties) {
        config.efficiencySellAttacker = parseFloat(properties, "efficiency.sell.attacker", config.efficiencySellAttacker);
        config.efficiencySellMidfielder = parseFloat(properties, "efficiency.sell.midfielder", config.efficiencySellMidfielder);
        config.efficiencySellDefender = parseFloat(properties, "efficiency.sell.defender", config.efficiencySellDefender);
        config.efficiencySellGoalkeeper = parseFloat(properties, "efficiency.sell.goalkeeper", config.efficiencySellGoalkeeper);
    }

    private static void configLogs(Properties properties) {
        if (parseBoolean(properties, "logs.debug", false)) {
            Logger.getLogger(Main.class.getPackage().getName()).setLevel(Level.DEBUG);
        }
    }

    private static void configProxy(Config config, Properties properties) {
        String uri = parseString(properties, "proxy.uri");
        String user = parseString(properties, "proxy.user");
        String password = parseString(properties, "proxy.password");
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

    public boolean isTransactionsProposal() {
        return transactionsProposal;
    }

    public boolean isTacticalSubstitutes() {
        return tacticalSubstitutesUpdate;
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

    public float getEfficiencySell(Position position) {
        switch (position) {
        case A:
            return efficiencySellAttacker;
        case M:
            return efficiencySellMidfielder;
        case D:
            return efficiencySellDefender;
        case G:
            return efficiencySellGoalkeeper;
        default:
            throw new UnsupportedOperationException(String.format("Position not supported: %s", position));
        }
    }

    public Proxy getProxy() {
        return proxy;
    }

}
