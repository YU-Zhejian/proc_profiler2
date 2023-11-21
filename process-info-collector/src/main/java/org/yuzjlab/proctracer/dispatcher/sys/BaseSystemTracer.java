package org.yuzjlab.proctracer.dispatcher.sys;

import org.yuzjlab.proctracer.dispatcher.BaseDispatcher;
import org.yuzjlab.proctracer.opts.TracerOpts;

public abstract class BaseSystemTracer extends BaseDispatcher {

    protected BaseSystemTracer(TracerOpts topts) {
        super(topts);
    }

    @Override
    protected long getID() {
        return 0;
    }
}
