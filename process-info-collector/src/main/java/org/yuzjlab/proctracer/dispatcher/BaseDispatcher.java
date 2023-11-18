package org.yuzjlab.proctracer.dispatcher;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.csv.CSVPrinter;
import org.yuzjlab.proctracer.opts.TracerOpts;
import org.yuzjlab.proctracer.utils.BaseCanStop;
import org.yuzjlab.proctracer.utils.ConfigurationManager;
import org.yuzjlab.proctracer.utils.LogManager;

public abstract class BaseDispatcher extends BaseCanStop implements DispatcherInterface {
    protected final LogManager logManager;
    protected final TracerOpts topts;
    protected final CSVPrinter csvPrinter;
    protected Map<DispatcherInterface, Thread> childDispatcherThreadMap;
    protected ConfigurationManager configurationManager;

    public void setShouldStop() {
        this.logManager.lh.debug("SIGTERM received");
        this.shouldStop = true;
    }

    public Map<String, String> recursiveFrontendFetch() {
        var hm = new HashMap<>(this.frontendFetch());
        for (var childDispatcher : this.childDispatcherThreadMap.keySet()) {
            hm.putAll(childDispatcher.recursiveFrontendFetch());
        }
        return hm;
    }

    protected BaseDispatcher(TracerOpts topts, boolean createCsvPrinter) {
        this.topts = topts;
        this.configurationManager = new ConfigurationManager(topts.getConfig(), this.getClass());
        this.logManager = new LogManager(this);
        this.logManager.lh.debug("Initializing...");

        CSVPrinter csvPrinter1;
        if (createCsvPrinter) {
            try {
                csvPrinter1 =
                        topts.createCSVPrinter(
                                Path.of(topts.getOutDirPath().toString(), this.toString())
                                        .toFile());
            } catch (IOException e) {
                this.logManager.logError(e);
                csvPrinter1 = null;
                this.setShouldStop();
            }
        } else {
            csvPrinter1 = null;
        }
        this.csvPrinter = csvPrinter1;

        this.childDispatcherThreadMap = new HashMap<>();
        this.logManager.lh.debug("Initialized");
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
        this.logManager.lh.debug("Added child-dispatcher {}", di);
        this.childDispatcherThreadMap.put(di, dit);
    }

    public void run() {
        if (this.shouldStop) {
            return;
        }
        this.logManager.lh.debug("Running setup script...");
        setUp();
        this.logManager.lh.debug("Running setup script FIN");
        var interval =
                (int)
                        (this.configurationManager.getConfigWithDefaults(Float.class, "interval")
                                * 1000);
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
        this.logManager.lh.debug("Running termination script...");
        tearDown();
        this.logManager.lh.debug("Running termination script FIN");
    }
}
