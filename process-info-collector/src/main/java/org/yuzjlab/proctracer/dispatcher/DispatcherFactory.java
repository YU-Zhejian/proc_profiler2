package org.yuzjlab.proctracer.dispatcher;

import org.yuzjlab.proctracer.dispatcher.proc.BaseProcessTracer;
import org.yuzjlab.proctracer.dispatcher.sys.BaseSystemTracer;
import org.yuzjlab.proctracer.opts.TracerOpts;

public class DispatcherFactory {
    private DispatcherFactory() {}

    public static void systemSetUp(BaseSystemTracer dispatcher) {
        for (var loadTracer :
                dispatcher.configurationManager.getListConfigWithDefaults(
                        String.class, "loadTracer")) {
            try {
                var tracer = Class.forName(loadTracer);
                if (!DispatcherInterface.class.isAssignableFrom(tracer)) {
                    throw new IllegalArgumentException("%s is not a dispatcher!".formatted(tracer));
                }
                var tracerInstance =
                        (DispatcherInterface)
                                tracer.getDeclaredConstructor(TracerOpts.class)
                                        .newInstance(dispatcher.topts);
                dispatcher.addDispatcher(tracerInstance);

            } catch (Exception e) {
                dispatcher.logManager.logError(e);
                dispatcher.setShouldStop();
            }
        }
    }

    public static void processSetUp(BaseProcessTracer dispatcher, long tracePID) {
        for (var loadTracer :
                dispatcher.configurationManager.getListConfigWithDefaults(
                        String.class, "loadTracer")) {
            try {
                var tracer = Class.forName(loadTracer);
                if (!DispatcherInterface.class.isAssignableFrom(tracer)) {
                    throw new IllegalArgumentException("%s is not a dispatcher!".formatted(tracer));
                }
                var tracerInstance =
                        (DispatcherInterface)
                                tracer.getDeclaredConstructor(TracerOpts.class, Long.class)
                                        .newInstance(dispatcher.topts, tracePID);
                dispatcher.addDispatcher(tracerInstance);

            } catch (Exception e) {
                dispatcher.logManager.logError(e);
                dispatcher.setShouldStop();
            }
        }
    }
}
