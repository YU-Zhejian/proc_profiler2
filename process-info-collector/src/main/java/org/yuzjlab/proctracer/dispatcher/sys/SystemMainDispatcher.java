package org.yuzjlab.proctracer.dispatcher.sys;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.yuzjlab.proctracer.dispatcher.BaseDispatcher;
import org.yuzjlab.proctracer.dispatcher.DispatcherInterface;
import org.yuzjlab.proctracer.opts.TracerOpts;

public class SystemMainDispatcher extends BaseDispatcher {

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
    protected void setUp() {
        for (var loadTracer : this.getListConfigWithDefaults(String.class, "loadTracer")) {
            try {
                var tracer = Class.forName(loadTracer);
                if (!DispatcherInterface.class.isAssignableFrom(tracer)) {
                    throw new IllegalArgumentException("%s is not a dispatcher!".formatted(tracer));
                }
                var tracerInstance =
                        (DispatcherInterface)
                                tracer.getDeclaredConstructor(TracerOpts.class)
                                        .newInstance(this.topts);
                this.addDispatcher(tracerInstance);

            } catch (Exception e) {
                this.logError(e);
                this.setShouldStop();
            }
        }
    }

    @Override
    protected void probe() {
        // Do nothing.
    }

    @Override
    protected long getID() {
        return 0;
    }

    @Override
    public Map<String, String> frontendFetch() {
        return new HashMap<>();
    }
}
