package org.blondin.mpg;

import org.blondin.mpg.config.Config;
import org.blondin.mpg.root.model.League;

public interface MpgProcess {

    void process(League league, ApiClients apiClients, Config config);
}
