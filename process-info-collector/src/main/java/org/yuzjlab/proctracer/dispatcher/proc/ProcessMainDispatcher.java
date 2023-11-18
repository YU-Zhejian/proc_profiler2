package org.yuzjlab.proctracer.dispatcher.proc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.procfs.process_info.EagerEvaluatedProcessInfo;
import org.yuzjlab.procfs.process_info.ProcessInfoInterface;
import org.yuzjlab.proctracer.dispatcher.BaseDispatcher;
import org.yuzjlab.proctracer.opts.TracerOpts;

public class ProcessMainDispatcher extends BaseDispatcher {
    protected long tracedPID;
    protected final ProcessInfoInterface eepi;
    protected Set<Long> childPIDs;

    public static final Map<String, Object> DEFAULT_CONFIG =
            Map.of("interval", 0.01, "loadTracer", List.of("defaults", "defaults2"));

    public ProcessMainDispatcher(TracerOpts topts, long tracedPID) {
        super(topts, false);
        this.tracedPID = tracedPID;
        ProcessInfoInterface eepi1;
        try {
            eepi1 = new EagerEvaluatedProcessInfo(tracedPID);
        } catch (ProcessBaseException e) {
            this.logManager.logError(e);
            eepi1 = null;
            this.setShouldStop();
        }
        this.eepi = eepi1;
        this.childPIDs = new HashSet<>();
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
    protected long getID() {
        return this.tracedPID;
    }

    @Override
    public Map<String, String> frontendFetch() {
        return new HashMap<>();
    }
}
