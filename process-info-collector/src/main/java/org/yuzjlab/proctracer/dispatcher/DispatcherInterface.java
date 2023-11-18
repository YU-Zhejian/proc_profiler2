package org.yuzjlab.proctracer.dispatcher;

import java.util.Map;

public interface DispatcherInterface extends Runnable {
    Map<String, String> frontendFetch();

    Map<String, String> recursiveFrontendFetch();

    boolean getShouldStop();

    void setShouldStop();
}
