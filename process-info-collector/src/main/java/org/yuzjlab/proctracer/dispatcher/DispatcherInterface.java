package org.yuzjlab.proctracer.dispatcher;

import java.util.Map;
import org.yuzjlab.proctracer.opts.TracerOpts;
import org.yuzjlab.proctracer.utils.CanStopInterface;

public interface DispatcherInterface extends Runnable, CanStopInterface {
    Map<String, String> frontendFetch();

    Map<String, String> recursiveFrontendFetch();

    TracerOpts getTracerOpts();
}
