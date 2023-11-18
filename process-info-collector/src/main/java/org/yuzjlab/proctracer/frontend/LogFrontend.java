package org.yuzjlab.proctracer.frontend;

import org.yuzjlab.proctracer.dispatcher.MainDispatcher;
import org.yuzjlab.proctracer.opts.TracerOpts;

public class LogFrontend extends BaseFrontend {

    public LogFrontend(TracerOpts topt, MainDispatcher mainDispatcher) {
        super(topt, mainDispatcher);
    }

    @Override
    protected void flush() {
        if (this.frontendStatus.isEmpty()) {
            this.logManager.lh.info("EMPTY");
        } else {
            for (var kv : this.frontendStatus.entrySet()) {
                this.logManager.lh.info("{}: {}", kv.getKey(), kv.getValue());
            }
        }
    }
}
