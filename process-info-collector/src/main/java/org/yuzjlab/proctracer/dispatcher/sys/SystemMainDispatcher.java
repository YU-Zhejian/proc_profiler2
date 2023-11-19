package org.yuzjlab.proctracer.dispatcher.sys;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.yuzjlab.proctracer.dispatcher.DispatcherFactory;
import org.yuzjlab.proctracer.opts.TracerOpts;

public class SystemMainDispatcher extends BaseSystemTracer {

    public static final Map<String, Object> DEFAULT_CONFIG =
            Map.of(
                    "interval",
                    0.01,
                    "loadTracer",
                    List.of("org.yuzjlab.proctracer.dispatcher.sys.SystemMemoryTracer"));

    public SystemMainDispatcher(TracerOpts topts) {
        super(topts, false);
    }

    @Override
    protected void probe() {
        // Do nothing.
    }

    @Override
    protected void setUp() {
        super.setUp();
        DispatcherFactory.systemSetUp(this);
    }

    @Override
    public Map<String, String> frontendFetch() {
        return new HashMap<>();
    }
}
