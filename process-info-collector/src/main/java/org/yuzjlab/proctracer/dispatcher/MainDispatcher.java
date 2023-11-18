package org.yuzjlab.proctracer.dispatcher;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import org.yuzjlab.proctracer.dispatcher.proc.ProcessMainDispatcher;
import org.yuzjlab.proctracer.dispatcher.sys.SystemMainDispatcher;
import org.yuzjlab.proctracer.opts.TracerOpts;
import org.yuzjlab.proctracer.psst.ProcessStatus;
import org.yuzjlab.proctracer.psst.ProcessSupervisorThreadInterface;

public class MainDispatcher extends BaseDispatcher {
    ProcessSupervisorThreadInterface psst;
    Thread psstThread;

    public MainDispatcher(TracerOpts topt, ProcessSupervisorThreadInterface psst) {
        super(topt, false);
        this.psst = psst;
        try {
            topt.validate();
        } catch (IOException e) {
            this.logManager.logError(e);
            this.setShouldStop();
        }
    }

    @Override
    protected void setUp() {
        this.psstThread = new Thread(psst);
        psstThread.start();
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
        if (this.configurationManager.getConfigWithDefaults(Boolean.class, "useSystemTracer")) {
            this.addDispatcher(new ProcessMainDispatcher(this.topts, tracedPID));
        }
        if (this.configurationManager.getConfigWithDefaults(Boolean.class, "useProcessTracer")) {
            this.addDispatcher(new SystemMainDispatcher(this.topts));
        }
    }

    @Override
    protected void probe() {
        if (psst.getStatus() == ProcessStatus.TERMINATED) {
            this.setShouldStop();
        }
    }

    @Override
    protected void tearDown() {
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
