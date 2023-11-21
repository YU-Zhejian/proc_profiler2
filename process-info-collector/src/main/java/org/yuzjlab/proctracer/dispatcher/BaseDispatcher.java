package org.yuzjlab.proctracer.dispatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.yuzjlab.proctracer.opts.TracerOpts;
import org.yuzjlab.proctracer.utils.BaseCanStop;
import org.yuzjlab.proctracer.utils.ConfigurationManager;
import org.yuzjlab.proctracer.utils.CsvPolicy;
import org.yuzjlab.proctracer.utils.LogManager;

public abstract class BaseDispatcher extends BaseCanStop implements DispatcherInterface {
    protected LogManager logManager;
    protected final TracerOpts topts;
    protected Map<DispatcherInterface, Thread> childDispatcherThreadMap;
    protected ConfigurationManager configurationManager;
    protected CsvPolicy csvPolicy;

    @Override
    public void setShouldStop() {
        if (this.logManager != null) {
            this.logManager.lh.debug("SIGTERM received");
        }
        this.shouldStop = true;
    }

    public Map<String, String> recursiveFrontendFetch() {
        var hm = new HashMap<>(this.frontendFetch());
        for (var childDispatcher : this.childDispatcherThreadMap.keySet()) {
            if (childDispatcher == this) {
                throw new RuntimeException(
                        "%s (%s) is a child of it self"
                                .formatted(this.toString(), this.getClass().getCanonicalName()));
            }
            hm.putAll(childDispatcher.recursiveFrontendFetch());
        }
        return hm;
    }

    protected BaseDispatcher(TracerOpts topts) {
        this.topts = topts;
        this.configurationManager = new ConfigurationManager(topts.getConfig(), this.getClass());
        this.childDispatcherThreadMap = new HashMap<>();
        this.csvPolicy = getCsvPolicy(this.getClass());
    }

    protected abstract void probe();

    protected void setUp() {
        this.logManager = new LogManager(this);
        this.logManager.lh.debug("Running setup script...");
        if (this.csvPolicy != null) {
            try {
                this.csvPolicy.setUp(this);
            } catch (IOException e) {
                this.logManager.logError(e);
                this.setShouldStop();
            }
        }
    }

    protected void tearDown() {
        if (this.csvPolicy != null) {
            try {
                this.csvPolicy.tearDown();
            } catch (IOException e) {
                this.logManager.logError(e);
            }
        }
    }

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

    protected void removeFinishedChildDispatchers() {
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
    }

    protected void terminateAllChildrDispatchers() {
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
    }

    public void run() {
        if (this.shouldStop) {
            return;
        }
        setUp();
        this.logManager.lh.debug("Running setup script FIN");
        var interval =
                (int)
                        (this.configurationManager.getConfigWithDefaults(Float.class, "interval")
                                * 1000);
        while (!this.shouldStop) {
            this.probe();
            this.removeFinishedChildDispatchers();
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        this.terminateAllChildrDispatchers();
        this.logManager.lh.debug("Running termination script...");
        tearDown();
        this.logManager.lh.debug("Running termination script FIN");
    }

    public static CsvPolicy getCsvPolicy(Class<? extends DispatcherInterface> dispatcher) {
        try {
            var csvPolicy = dispatcher.getField("CSV_POLICY").get(null);
            if (!(csvPolicy instanceof CsvPolicy)) {
                throw new ClassNotFoundException(
                        "CSV_POLICY field of class %s is not CsvPolicy!"
                                .formatted(dispatcher.getCanonicalName()));
            }
            return ((CsvPolicy) csvPolicy);
        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException ignored) {
        }
        return null;
    }

    public TracerOpts getTracerOpts() {
        return this.topts;
    }
}
