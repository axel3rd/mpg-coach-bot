package org.blondin.mpg.root;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.blondin.mpg.config.Config;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class BadAuthenticationsTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    private Config getConfig(String authentications) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("email = firstName.lastName@gmail.com");
        lines.add("password = foobar");
        lines.add(String.format("authentications = %s", authentications));

        File configFile = new File(testFolder.getRoot(), "mpg.properties.test");
        FileUtils.writeLines(configFile, lines);

        return Config.build(configFile.getPath());
    }

    @Test
    public void testNotDefined() throws IOException {
        Config config = getConfig(",");
        try {
            MpgClient.build(config);
            Assert.fail("Not defined");
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals("Authentications types should be defined", e.getMessage());
        }
    }

    @Test
    public void testUnsupported() throws IOException {
        Config config = getConfig("unknow");
        try {
            MpgClient.build(config);
            Assert.fail("Not defined");
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals("Authentication not supported: 'unknow'", e.getMessage());
        }
    }

}
