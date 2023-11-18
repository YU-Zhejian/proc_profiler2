package org.yuzjlab.proctracer.frontend;

import org.yuzjlab.proctracer.dispatcher.MainDispatcher;
import org.yuzjlab.proctracer.opts.TracerOpts;

public class NOPFrontend extends BaseFrontend {

    public NOPFrontend(TracerOpts topt, MainDispatcher mainDispatcher) {
        super(topt, mainDispatcher);
    }

    @Override
    protected void flush() {
        // Do nothing
    }
}
