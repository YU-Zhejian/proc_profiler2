package org.yuzjlab.proctracer.dispatcher.proc;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.procfs.files.ProcPidStatm;
import org.yuzjlab.proctracer.frontend.FrontendUtils;
import org.yuzjlab.proctracer.opts.TracerOpts;
import org.yuzjlab.proctracer.utils.CsvPolicy;

@SuppressWarnings("unused")
public class ProcessMemoryTracer extends BaseProcessTracer {
    protected ProcPidStatm cachedProcPidStatm;
    public static final CsvPolicy CSV_POLICY =
            new CsvPolicy(
                    ProcessMemoryTracer.class,
                    new String[] {
                        "TIME", "RAM_VIRTUAL", "RAM_RESIDENT", "RAM_SHARED", "RAM_DATA", "RAM_TEXT"
                    },
                    new String[] {
                        "Time:MilliSecSinceEpoch", "Long", "Long", "Long", "Long", "Long"
                    },
                    new Boolean[] {false, false, false, false, false, false});

    public ProcessMemoryTracer(TracerOpts topts, Long tracePID) {
        super(topts, tracePID);
        this.cachedProcPidStatm = null;
    }

    public static final Map<String, Object> DEFAULT_CONFIG = Map.of("interval", 0.01);

    @Override
    protected void probe() {
        try {
            this.cachedProcPidStatm = eepi.getMemoryInformation();
            this.csvPolicy.printRecord(
                    Instant.now().toEpochMilli(),
                    this.cachedProcPidStatm.getSizeBytes(),
                    this.cachedProcPidStatm.getResidentBytes(),
                    this.cachedProcPidStatm.getSharedBytes(),
                    this.cachedProcPidStatm.getDataBytes(),
                    this.cachedProcPidStatm.getTextBytes());
        } catch (ProcessBaseException | IOException e) {
            this.logManager.logError(e);
        }
    }

    @Override
    public Map<String, String> frontendFetch() {
        if (this.cachedProcPidStatm == null) {
            return new HashMap<>();
        }
        return Map.of(
                "proc#%d.mem.vm".formatted(this.tracePID),
                FrontendUtils.toHumanReadable(this.cachedProcPidStatm.getSizeBytes()),
                "proc#%d.mem.res".formatted(this.tracePID),
                FrontendUtils.toHumanReadable(this.cachedProcPidStatm.getResidentBytes()));
    }
}
