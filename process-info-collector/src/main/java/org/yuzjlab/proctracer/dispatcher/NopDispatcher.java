package org.yuzjlab.proctracer.dispatcher;

import java.util.Collections;
import java.util.Map;
import org.yuzjlab.proctracer.opts.TracerOpts;

@SuppressWarnings("unused")
public class NopDispatcher extends BaseDispatcher {

    public NopDispatcher(TracerOpts topt) {
        super(topt);
    }

    public NopDispatcher(TracerOpts topt, Long tracePID) {
        super(topt);
    }

    @Override
    protected void probe() {
        // Do nothing
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
        return Thread.currentThread().getId();
    }

    public static final Map<String, Object> DEFAULT_CONFIG = Map.of();
}
