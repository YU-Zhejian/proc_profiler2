package org.yuzjlab.proctracer.dispatcher;

import java.util.Map;
import java.util.Set;

import jnr.ffi.annotations.In;
import org.apache.commons.configuration2.Configuration;

public abstract class BaseDispatcher extends Thread implements DispatcherInterface {
    protected Configuration config;
    protected boolean shouldStop;
    protected Configuration defaultConfig =  null;

    protected BaseDispatcher(Configuration config) {
        this.config = config;
    }

    public void terminate() {
        this.shouldStop = true;
    }

    protected abstract void probe();

    protected void setUp() {}

    protected void tearDown() {}

    protected abstract Map<String, String> frontendFetch();


    protected <T> T getConfigWithDefaults(Class <T> cls, String localKey){
        var canonicalKey = "%s.%s".formatted(this.getClass().getCanonicalName(), localKey);
        return this.config.get(cls, canonicalKey, defaultConfig.get(cls, canonicalKey));
    }

    public void run() {
        setUp();
        var interval = (int) (this.getConfigWithDefaults(Float.class, "interval") * 1000);
        while (!this.shouldStop) {
            this.probe();
            try {
                synchronized (this) {
                    this.wait(interval);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        tearDown();
    }

    public final static Set<String> ALL_KNOWN_DISPATCHER_NAMES = Set.of(
            "org.yuzjlab.proctracer.dispatcher.sys.SystemMemoryDispatcher"
    );
}
