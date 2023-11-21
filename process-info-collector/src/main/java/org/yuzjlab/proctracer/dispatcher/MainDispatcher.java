package org.yuzjlab.proctracer.dispatcher;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.PropertiesConfigurationLayout;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.yuzjlab.proctracer.dispatcher.proc.ProcessMainDispatcher;
import org.yuzjlab.proctracer.dispatcher.sys.SystemMainDispatcher;
import org.yuzjlab.proctracer.opts.TracerOpts;
import org.yuzjlab.proctracer.psst.ProcessStatus;
import org.yuzjlab.proctracer.psst.ProcessSupervisorThreadInterface;
import org.yuzjlab.proctracer.utils.ConfigurationManager;
import org.yuzjlab.proctracer.utils.CsvPolicy;

public class MainDispatcher extends BaseDispatcher {
    protected ProcessSupervisorThreadInterface psst;
    protected Thread psstThread;
    protected long setUpTimeStamp;

    public MainDispatcher(TracerOpts topt, ProcessSupervisorThreadInterface psst) {
        super(topt);
        this.psst = psst;
        try {
            topt.validate();
        } catch (IOException | ConfigurationException e) {
            this.logManager.logError(e);
            this.setShouldStop();
        }
    }

    protected boolean shouldLoadSystemTracers() {
        return Boolean.TRUE.equals(
                this.configurationManager.getConfigWithDefaults(Boolean.class, "useSystemTracer"));
    }

    protected boolean shouldLoadProcessTracers() {
        return Boolean.TRUE.equals(
                this.configurationManager.getConfigWithDefaults(Boolean.class, "useProcessTracer"));
    }

    protected Iterable<Class<? extends DispatcherInterface>> preloadAllTracer() {
        var dispatchers = new HashSet<Class<? extends DispatcherInterface>>();
        var processedDispatchers = new HashSet<Class<? extends DispatcherInterface>>();
        if (this.shouldLoadSystemTracers()) {
            dispatchers.add(SystemMainDispatcher.class);
        }
        if (this.shouldLoadProcessTracers()) {
            dispatchers.add(ProcessMainDispatcher.class);
        }
        while (!dispatchers.isEmpty()) {
            var newDispatchers = new HashSet<Class<? extends DispatcherInterface>>();
            for (var dispatcherBeingProcessed : dispatchers) {
                var configurationManager =
                        new ConfigurationManager(this.topts.getConfig(), dispatcherBeingProcessed);
                if (configurationManager.containsKey("loadTracer")) {
                    for (var newDispatcherName :
                            configurationManager.getListConfigWithDefaults(
                                    String.class, "loadTracer")) {
                        try {
                            var newDispatcher = Class.forName(newDispatcherName);
                            if (!DispatcherInterface.class.isAssignableFrom(newDispatcher)) {
                                throw new ClassNotFoundException(
                                        "Class %s is not dispatcher!"
                                                .formatted(newDispatcher.getCanonicalName()));
                            }
                            newDispatchers.add(
                                    (Class<? extends DispatcherInterface>) newDispatcher);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            processedDispatchers.addAll(dispatchers);
            dispatchers.clear();
            dispatchers.addAll(newDispatchers);
        }
        return processedDispatchers;
    }

    protected Iterable<CsvPolicy> getAllCsvPolicies() {
        var csvPolicies = new ArrayList<CsvPolicy>();
        for (var dispatcher : this.preloadAllTracer()) {
            var csvPolicy = BaseDispatcher.getCsvPolicy(dispatcher);
            if (csvPolicy != null) {
                csvPolicies.add(csvPolicy);
            }
        }
        return csvPolicies;
    }

    protected void flushCsvPolicies() throws IOException, ConfigurationException {
        var mConf = new MapConfiguration(new HashMap<>());
        var csvPolicyNames = new ArrayList<String>();
        for (var csvPolicy : this.getAllCsvPolicies()) {
            mConf.setProperty("%s.header".formatted(csvPolicy.name), csvPolicy.header);
            mConf.setProperty("%s.schema".formatted(csvPolicy.name), csvPolicy.schema);
            mConf.setProperty(
                    "%s.isAccumulative".formatted(csvPolicy.name), csvPolicy.isAccumulative);
            csvPolicyNames.add(csvPolicy.name);
        }
        mConf.setProperty("names", csvPolicyNames);
        var propertiesConfiguration = new PropertiesConfiguration();
        propertiesConfiguration.setListDelimiterHandler(new DefaultListDelimiterHandler(','));
        var layOut = new PropertiesConfigurationLayout();
        layOut.setForceSingleLine(true);
        propertiesConfiguration.setLayout(layOut);
        propertiesConfiguration.copy(mConf);
        propertiesConfiguration.write(
                new FileWriter(
                        Path.of(this.topts.getOutDirPath().toString(), "csvPolicies.properties")
                                .toFile()));
    }

    @Override
    protected void setUp() {
        super.setUp();
        this.logManager.lh.info("Main dispatcher added");
        this.setUpTimeStamp = Instant.now().toEpochMilli();
        this.psstThread = new Thread(psst);
        this.psstThread.start();
        var interval =
                (int)
                        (this.configurationManager.getConfigWithDefaults(Float.class, "interval")
                                * 1000);
        while (psst.getStatus() == ProcessStatus.PENDING) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        var tracedPID = psst.getPid();
        if (tracedPID == -1) {
            this.setShouldStop();
            return;
        }
        if (this.shouldLoadSystemTracers()) {
            this.addDispatcher(new ProcessMainDispatcher(this.topts, tracedPID));
        }
        if (this.shouldLoadProcessTracers()) {
            this.addDispatcher(new SystemMainDispatcher(this.topts));
        }
        try {
            this.flushCsvPolicies();
        } catch (ConfigurationException | IOException e) {
            this.logManager.logError(e);
            this.setShouldStop();
        }
    }

    protected void dropTimeStamp() throws IOException {
        try (var fw =
                new FileWriter(
                        Path.of(this.topts.getOutDirPath().toString(), "timestamp").toString())) {
            fw.write("%d\t%d".formatted(this.setUpTimeStamp, Instant.now().toEpochMilli()));
        }
    }

    @Override
    protected void probe() {
        try {
            this.dropTimeStamp();
        } catch (IOException e) {
            this.logManager.logError(e);
            this.setShouldStop();
        }
        if (psst.getStatus() == ProcessStatus.TERMINATED) {
            this.setShouldStop();
        }
    }

    @Override
    protected void tearDown() {
        super.tearDown();
        this.logManager.lh.info("Main dispatcher shutting down");
        synchronized (this) {
            try {
                this.psstThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Defunct. Do not use.
     *
     * @return An empty map.
     */
    @Override
    public Map<String, String> frontendFetch() {
        return Collections.emptyMap();
    }

    @Override
    protected long getID() {
        return 0;
    }

    public static final Map<String, Object> DEFAULT_CONFIG =
            Map.of(
                    "interval", 0.01,
                    "useSystemTracer", true,
                    "useProcessTracer", true);
}
