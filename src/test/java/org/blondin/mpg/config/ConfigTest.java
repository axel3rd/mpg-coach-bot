package org.blondin.mpg.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.blondin.mpg.root.model.Position;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ConfigTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void testConfigDefault() {
        Config config = Config.build("src/test/resources/mpg.properties.here");
        Assert.assertEquals("firstName.lastName@gmail.com", config.getLogin());
        Assert.assertEquals("foobar", config.getPassword());

        Assert.assertEquals(true, config.isTeampUpdate());
        Assert.assertEquals(false, config.isEfficiencyRecentFocus());
        Assert.assertEquals(8, config.getEfficiencyRecentDays());

        Assert.assertEquals(true, config.isUseBonus());

        Assert.assertNotNull(config.getProxy());
        Assert.assertFalse(config.getProxy().isConfigured());

        Assert.assertNotNull(config.getLeaguesInclude());
        Assert.assertTrue(config.getLeaguesInclude().isEmpty());
        Assert.assertNotNull(config.getLeaguesExclude());
        Assert.assertTrue(config.getLeaguesExclude().isEmpty());

        Assert.assertTrue(config.isTacticalSubstitutes());
        Assert.assertEquals(6.0f, config.getNoteTacticalSubstituteAttacker(), 0);
        Assert.assertEquals(5.0f, config.getNoteTacticalSubstituteDefender(), 0);
        Assert.assertEquals(5.0f, config.getNoteTacticalSubstituteMidfielder(), 0);

        // For unit tests, transactions proposal is not enable by default
        Assert.assertEquals(false, config.isTransactionsProposal());

        Assert.assertEquals(3f, config.getEfficiencySell(Position.A), 0);
        Assert.assertEquals(3f, config.getEfficiencySell(Position.M), 0);
        Assert.assertEquals(3f, config.getEfficiencySell(Position.D), 0);
        Assert.assertEquals(3f, config.getEfficiencySell(Position.G), 0);

        Assert.assertEquals(1.2f, config.getEfficiencyCoefficient(Position.A), 0);
        Assert.assertEquals(1.05f, config.getEfficiencyCoefficient(Position.M), 0);
        Assert.assertEquals(1.025f, config.getEfficiencyCoefficient(Position.D), 0);
        Assert.assertEquals(1f, config.getEfficiencyCoefficient(Position.G), 0);
    }

    @Test
    public void testComplete() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("email = firstName.lastName@gmail.com");
        lines.add("password = foobar");
        lines.add("team.update=false");
        lines.add("efficiency.recent.focus=true");
        lines.add("efficiency.recent.days=5");
        lines.add("use.bonus=false");
        lines.add("leagues.include=KX24XMUJ,KLGXSSUM");
        lines.add("leagues.exclude=LJT3FXDX, FOOBAR42");
        lines.add("tactical.substitutes=false");
        lines.add("tactical.substitute.attacker=3.2");
        lines.add("tactical.substitute.midfielder=1");
        lines.add("tactical.substitute.defender=2");
        lines.add("efficiency.coefficient.attacker=1");
        lines.add("efficiency.coefficient.midfielder=2");
        lines.add("efficiency.coefficient.defender=4");
        lines.add("efficiency.coefficient.goalkeeper=5");
        lines.add("transactions.proposal=false");
        lines.add("efficiency.sell.attacker=5");
        lines.add("efficiency.sell.midfielder=4");
        lines.add("efficiency.sell.defender=2");
        lines.add("efficiency.sell.goalkeeper=1");
        lines.add("proxy.uri=http://company.proxy.com:80");
        lines.add("proxy.user=foo");
        lines.add("proxy.password=bar"); // NOSNOAR : Test wanted

        File configFile = new File(testFolder.getRoot(), "mpg.properties.test");
        FileUtils.writeLines(configFile, lines);

        Config config = Config.build(configFile.getPath());
        // No login/password, could be overridden by system properties in real Travis tests

        Assert.assertEquals(false, config.isTeampUpdate());
        Assert.assertEquals(true, config.isEfficiencyRecentFocus());
        Assert.assertEquals(5, config.getEfficiencyRecentDays());

        Assert.assertEquals(false, config.isUseBonus());

        Assert.assertEquals(2, config.getLeaguesInclude().size());
        Assert.assertTrue(config.getLeaguesInclude().contains("KX24XMUJ"));
        Assert.assertTrue(config.getLeaguesInclude().contains("KLGXSSUM"));
        Assert.assertFalse(config.getLeaguesInclude().contains("LJT3FXDX"));
        Assert.assertFalse(config.getLeaguesInclude().contains("FOOBAR42"));
        Assert.assertEquals(2, config.getLeaguesExclude().size());
        Assert.assertTrue(config.getLeaguesExclude().contains("LJT3FXDX"));
        Assert.assertTrue(config.getLeaguesExclude().contains("FOOBAR42"));
        Assert.assertFalse(config.getLeaguesExclude().contains("KX24XMUJ"));
        Assert.assertFalse(config.getLeaguesExclude().contains("KLGXSSUM"));

        Assert.assertFalse(config.isTacticalSubstitutes());
        Assert.assertEquals(3.2f, config.getNoteTacticalSubstituteAttacker(), 0);
        Assert.assertEquals(2f, config.getNoteTacticalSubstituteDefender(), 0);
        Assert.assertEquals(1f, config.getNoteTacticalSubstituteMidfielder(), 0);

        Assert.assertEquals(false, config.isTransactionsProposal());
        Assert.assertEquals(5f, config.getEfficiencySell(Position.A), 0);
        Assert.assertEquals(4f, config.getEfficiencySell(Position.M), 0);
        Assert.assertEquals(2f, config.getEfficiencySell(Position.D), 0);
        Assert.assertEquals(1f, config.getEfficiencySell(Position.G), 0);

        Assert.assertEquals(1f, config.getEfficiencyCoefficient(Position.A), 0);
        Assert.assertEquals(2f, config.getEfficiencyCoefficient(Position.M), 0);
        Assert.assertEquals(4f, config.getEfficiencyCoefficient(Position.D), 0);
        Assert.assertEquals(5f, config.getEfficiencyCoefficient(Position.G), 0);

        Assert.assertNotNull(config.getProxy());
        Assert.assertTrue(config.getProxy().isConfigured());
        Assert.assertEquals("http://company.proxy.com:80", config.getProxy().getUri());
        Assert.assertEquals("foo", config.getProxy().getUser());
        Assert.assertEquals("bar", config.getProxy().getPassword());
    }

    @Test
    public void testBadEfficiencyRecentDayMax() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("email = firstName.lastName@gmail.com");
        lines.add("password = foobar");
        lines.add("efficiency.recent.days=42");
        File configFile = new File(testFolder.getRoot(), "mpg.properties.test");
        FileUtils.writeLines(configFile, lines);

        try {
            Config.build(configFile.getPath());
            Assert.fail("Days > max");
        } catch (UnsupportedOperationException e) {
            Assert.assertTrue(e.getMessage().contains("efficiency.recent.days"));
            Assert.assertTrue(e.getMessage().contains("8"));
        }
    }

    @Test
    public void testBadEfficiencyRecentDayMin() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("email = firstName.lastName@gmail.com");
        lines.add("password = foobar");
        lines.add("efficiency.recent.days=0");
        File configFile = new File(testFolder.getRoot(), "mpg.properties.test");
        FileUtils.writeLines(configFile, lines);

        try {
            Config.build(configFile.getPath());
            Assert.fail("Days > max");
        } catch (UnsupportedOperationException e) {
            Assert.assertTrue(e.getMessage().contains("efficiency.recent.days"));
            Assert.assertTrue(e.getMessage().contains("8"));
        }
    }

}
