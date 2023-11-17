package org.yuzjlab.proctracer.dispatcher;

import java.util.Collections;
import java.util.Map;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.yuzjlab.proctracer.opts.TracerOpts;
import org.yuzjlab.proctracer.psst.ProcessSupervisorThreadInterface;

public class MainDispatcher extends BaseDispatcher {
    ProcessSupervisorThreadInterface psst;
    Thread psstThread;

    public MainDispatcher(TracerOpts topt, ProcessSupervisorThreadInterface psst) {
        super(topt.getConfig());
        this.psst = psst;
        this.psstThread = new Thread(psst);
        psstThread.start();
        this.defaultConfig = MainDispatcher.getDefaultConfig();
        topt.validate();
    }

    @Override
    protected void probe() {
        if (psst.getPid() == -1) {
            this.terminate();
        }
    }

    /**
     * Defunct. Do not use.
     *
     * @return An empty map.
     */
    @Override
    protected Map<String, String> frontendFetch() {
        return Collections.emptyMap();
    }

    public static Configuration getDefaultConfig() {
        var className = MainDispatcher.class.getCanonicalName();
        PropertiesConfiguration defConfig;
        try {
            defConfig =
                    new BasicConfigurationBuilder<>(PropertiesConfiguration.class)
                            .getConfiguration();
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
        defConfig.setProperty("%s.interval".formatted(className), 0.01);
        return defConfig;
    }
}
