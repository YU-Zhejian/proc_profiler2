package org.yuzjlab.proctracer.dispatcher.proc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.proctracer.dispatcher.DispatcherFactory;
import org.yuzjlab.proctracer.opts.TracerOpts;

public class ProcessMainDispatcher extends BaseProcessTracer {
    protected Set<Long> childPIDs;

    public static final Map<String, Object> DEFAULT_CONFIG =
            Map.of(
                    "interval",
                    0.01,
                    "loadTracer",
                    List.of("org.yuzjlab.proctracer.dispatcher.proc.ProcessMemoryTracer"));

    public ProcessMainDispatcher(TracerOpts topts, long tracedPID) {
        super(topts, false, tracedPID);
        this.childPIDs = new HashSet<>();
    }

    @Override
    protected void setUp() {
        super.setUp();
        DispatcherFactory.processSetUp(this, this.tracePID);
    }

    @Override
    protected void probe() {
        try {
            var newChildPIDs = this.eepi.getChildPIDs();
            for (var childPID : newChildPIDs) {
                if (!this.childPIDs.contains(childPID)) {
                    this.childPIDs.add(childPID);
                    this.addDispatcher(new ProcessMainDispatcher(this.topts, childPID));
                }
            }
        } catch (ProcessBaseException e) {
            this.logManager.logError(e);
            this.setShouldStop();
        }
    }

    @Override
    public Map<String, String> frontendFetch() {
        return new HashMap<>();
    }
}
