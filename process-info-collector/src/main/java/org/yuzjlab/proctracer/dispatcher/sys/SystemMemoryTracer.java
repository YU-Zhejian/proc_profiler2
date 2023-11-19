package org.yuzjlab.proctracer.dispatcher.sys;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.yuzjlab.procfs.SystemInfo;
import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.procfs.files.ProcMemInfo;
import org.yuzjlab.proctracer.frontend.FrontendUtils;
import org.yuzjlab.proctracer.opts.TracerOpts;

public class SystemMemoryTracer extends BaseSystemTracer {
    protected final SystemInfo sysInfo;
    protected ProcMemInfo cachedMemInfo;

    public SystemMemoryTracer(TracerOpts topts) {
        super(topts, true);
        this.sysInfo = new SystemInfo();
        this.cachedMemInfo = null;
    }

    public static final Map<String, Object> DEFAULT_CONFIG = Map.of("interval", 0.01);

    @Override
    protected void setUp() {
        super.setUp();
        try {
            this.csvPrinter.printRecord(
                    "TIME",
                    "RAM_TOTAL",
                    "SWAP_TOTAL",
                    "RAM_FREE",
                    "RAM_AVAIL",
                    "SWAP_CACHED",
                    "SWAP_FREE",
                    "RAM_ACTIVE",
                    "RAM_INACTIVE",
                    "RAM_BUFFER",
                    "RAM_CACHED");
        } catch (IOException e) {
            this.logManager.logError(e);
            this.setShouldStop();
        }
    }

    @Override
    protected void probe() {
        try {
            this.cachedMemInfo = sysInfo.getMemInfo();
            this.csvPrinter.printRecord(
                    Instant.now().toEpochMilli(),
                    this.cachedMemInfo.memTotalKBytes,
                    this.cachedMemInfo.swapTotalKBytes,
                    this.cachedMemInfo.memFreeKBytes,
                    this.cachedMemInfo.memAvailiableKBytes,
                    this.cachedMemInfo.swapCachedKBytes,
                    this.cachedMemInfo.swapFreeKBytes,
                    this.cachedMemInfo.activeKBytes,
                    this.cachedMemInfo.inactiveKBytes,
                    this.cachedMemInfo.buffersKBytes,
                    this.cachedMemInfo.cachedKBytes);
        } catch (ProcessBaseException | IOException e) {
            this.logManager.logError(e);
        }
    }

    @Override
    public Map<String, String> frontendFetch() {
        if (this.cachedMemInfo == null) {
            return new HashMap<>();
        }
        return Map.of(
                "sys.mem.total",
                        FrontendUtils.toHumanReadable(this.cachedMemInfo.memTotalKBytes * 1024),
                "sys.swap.total",
                        FrontendUtils.toHumanReadable(this.cachedMemInfo.swapTotalKBytes * 1024),
                "sys.mem.avail",
                        FrontendUtils.toHumanReadable(
                                this.cachedMemInfo.memAvailiableKBytes * 1024),
                "sys.swap.avail",
                        FrontendUtils.toHumanReadable(this.cachedMemInfo.swapFreeKBytes * 1024));
    }
}
