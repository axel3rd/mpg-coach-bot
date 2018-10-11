package org.blondin.mpg;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.blondin.mpg.equipeactu.InjuredSuspendedClient;
import org.blondin.mpg.root.MpgClient;
import org.blondin.mpg.root.model.Coach;
import org.blondin.mpg.root.model.Dashboard;
import org.blondin.mpg.root.model.Player;
import org.blondin.mpg.stats.MpgStatsClient;
import org.blondin.mpg.stats.model.Championship;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MainTest {

    @Test
    public void testRealIfCredentials() throws Exception {
        final String config = "src/test/resources/mpg.properties";
        if (new File(config).exists()) {
            Main.main(new String[] { config });
        }
    }

    @Test
    public void testRealWithBadCredentials() throws Exception {
        try {
            Main.main(new String[] { "src/test/resources/mpg.properties.here" });
        } catch (UnsupportedOperationException e) {
            // Credentials in sample file are fake
        }
    }

    @Test
    public void testProcessMock() throws Exception {
        // Mock initialization
        MpgClient mpgClient = mock(MpgClient.class);
        when(mpgClient.getCoach(anyString())).thenReturn(new ObjectMapper().enable(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .readValue(new File("src/test/resources/datas", "mpg.coach-1.json"), Coach.class));
        when(mpgClient.getDashboard()).thenReturn(new ObjectMapper().enable(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .readValue(new File("src/test/resources/datas", "mpg.dashboard-1.json"), Dashboard.class));

        MpgStatsClient mpgStatsClient = mock(MpgStatsClient.class);
        when(mpgStatsClient.getStats())
                .thenReturn(new ObjectMapper().readValue(new File("src/test/resources/datas", "mpgstats.ligue-1.json"), Championship.class));

        InjuredSuspendedClient outPlayersClient = spy(InjuredSuspendedClient.class);
        when(outPlayersClient.getHtmlContent())
                .thenReturn(FileUtils.readFileToString(new File("src/test/resources/datas", "equipeactu.ligue-1-1.html")));

        // Test out (on cloned list)
        List<Player> players = new ArrayList<>(mpgClient.getCoach("fake").getPlayers());
        Assert.assertNotNull("Nkunku should be here",
                players.stream().filter(customer -> "Nkunku".equals(customer.getLastName())).findAny().orElse(null));
        Main.removeOutPlayers(players, outPlayersClient);
        Assert.assertNull("Nkunku should be removed",
                players.stream().filter(customer -> "Nkunku".equals(customer.getLastName())).findAny().orElse(null));

        // Run global process
        Main.process(mpgClient, mpgStatsClient, outPlayersClient);
    }
}
