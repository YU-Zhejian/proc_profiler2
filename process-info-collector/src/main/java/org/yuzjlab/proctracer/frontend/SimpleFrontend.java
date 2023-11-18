package org.yuzjlab.proctracer.frontend;

import org.yuzjlab.proctracer.dispatcher.MainDispatcher;
import org.yuzjlab.proctracer.opts.TracerOpts;

public class SimpleFrontend extends BaseFrontend {

    public SimpleFrontend(TracerOpts topt, MainDispatcher mainDispatcher) {
        super(topt, mainDispatcher);
    }

    @Override
    protected void flush() {
        // Do nothing
    }
}
