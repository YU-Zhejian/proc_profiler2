package org.yuzjlab.proctracer.dispatcher.proc;

import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.procfs.process_info.EagerEvaluatedProcessInfo;
import org.yuzjlab.proctracer.dispatcher.BaseDispatcher;
import org.yuzjlab.proctracer.opts.TracerOpts;

public abstract class BaseProcessTracer extends BaseDispatcher {

    protected EagerEvaluatedProcessInfo eepi;
    protected long tracePID;

    protected BaseProcessTracer(TracerOpts topts, boolean createCsvPrinter, long tracePID) {
        super(topts, createCsvPrinter);
        this.eepi = null;
        this.tracePID = tracePID;
    }

    @Override
    protected void setUp() {
        super.setUp();
        try {
            this.eepi = new EagerEvaluatedProcessInfo(this.tracePID);
        } catch (ProcessBaseException e) {
            this.logManager.logError(e);
            this.setShouldStop();
        }
    }

    @Override
    protected long getID() {
        return this.tracePID;
    }
}
