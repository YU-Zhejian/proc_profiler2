package org.yuzjlab.proctracer.dispatcher;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yuzjlab.proctracer.opts.TracerOpts;
import org.yuzjlab.proctracer.utils.BaseConfigurable;

public abstract class BaseDispatcher extends BaseConfigurable implements DispatcherInterface {
    protected final Logger lh;
    protected final TracerOpts topts;
    protected final CSVPrinter csvPrinter;
    protected boolean shouldStop;
    protected Map<DispatcherInterface, Thread> childDispatcherThreadMap;

    @Override
    public boolean getShouldStop() {
        return this.shouldStop;
    }

    public Map<String, String> recursiveFrontendFetch() {
        var hm = new HashMap<>(this.frontendFetch());
        for (var childDispatcher : this.childDispatcherThreadMap.keySet()) {
            hm.putAll(childDispatcher.recursiveFrontendFetch());
        }
        return hm;
    }

    protected BaseDispatcher(TracerOpts topts, boolean createCsvPrinter) {
        super(topts.getConfig());
        this.topts = topts;
        this.lh = LoggerFactory.getLogger(this.toString());
        lh.debug("Initializing...");

        CSVPrinter csvPrinter1;
        if (createCsvPrinter) {
            try {
                csvPrinter1 =
                        topts.createCSVPrinter(
                                Path.of(topts.getOutDirPath().toString(), this.toString())
                                        .toFile());
            } catch (IOException e) {
                this.logError(e);
                csvPrinter1 = null;
                this.setShouldStop();
            }
        } else {
            csvPrinter1 = null;
        }
        this.csvPrinter = csvPrinter1;

        this.childDispatcherThreadMap = new HashMap<>();
        lh.debug("Initialized");
    }

    public void setShouldStop() {
        lh.debug("SIGTERM received");
        this.shouldStop = true;
    }

    protected abstract void probe();

    protected void setUp() {}

    protected void tearDown() {}

    protected abstract long getID();

    public String toString() {
        return this.getClass().getSimpleName() + "-" + this.getID();
    }

    protected void addDispatcher(DispatcherInterface di) {
        var dit = new Thread(di, di.toString());
        dit.start();
        lh.debug("Added child-dispatcher {}", di);
        this.childDispatcherThreadMap.put(di, dit);
    }

    public void run() {
        if (this.shouldStop) {
            return;
        }
        lh.debug("Running setup script...");
        setUp();
        lh.debug("Running setup script FIN");
        var interval = (int) (this.getConfigWithDefaults(Float.class, "interval") * 1000);
        while (!this.shouldStop) {
            this.probe();
            var toRemove = new ArrayList<DispatcherInterface>();
            for (var childDispatcherThreadMapItem : this.childDispatcherThreadMap.entrySet()) {
                if (childDispatcherThreadMapItem.getKey().getShouldStop()
                        || (!childDispatcherThreadMapItem.getValue().isAlive())) {
                    childDispatcherThreadMapItem.getKey().setShouldStop();
                    try {
                        synchronized (this) {
                            childDispatcherThreadMapItem.getValue().join();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    toRemove.add(childDispatcherThreadMapItem.getKey());
                }
            }
            for (var toRemoveKey : toRemove) {
                this.childDispatcherThreadMap.remove(toRemoveKey);
            }

            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        for (var childDispatcherThreadMapItem : this.childDispatcherThreadMap.entrySet()) {
            childDispatcherThreadMapItem.getKey().setShouldStop();
            try {
                synchronized (this) {
                    childDispatcherThreadMapItem.getValue().join();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        this.childDispatcherThreadMap.clear();
        lh.debug("Running termination script...");
        tearDown();
        lh.debug("Running termination script FIN");
    }

    protected void logError(Throwable e) {
        lh.error("Failed because of {}", e.toString());
    }

    public static final Set<String> ALL_KNOWN_DISPATCHER_NAMES =
            Set.of(
                    "org.yuzjlab.proctracer.dispatcher.sys.SystemMemoryTracer",
                    "org.yuzjlab.proctracer.dispatcher.MainDispatcher",
                    "org.yuzjlab.proctracer.dispatcher.proc.ProcessMainDispatcher",
                    "org.yuzjlab.proctracer.dispatcher.sys.SystemMainDispatcher");
}
