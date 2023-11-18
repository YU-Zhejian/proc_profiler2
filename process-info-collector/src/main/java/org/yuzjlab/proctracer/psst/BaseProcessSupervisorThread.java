package org.yuzjlab.proctracer.psst;

import jnr.posix.POSIX;
import jnr.posix.POSIXFactory;

public abstract class BaseProcessSupervisorThread implements ProcessSupervisorThreadInterface {
    protected POSIX posix;
    protected ProcessStatus status;

    protected BaseProcessSupervisorThread() {
        this.posix = POSIXFactory.getPOSIX();
        this.status = ProcessStatus.PENDING;
    }

    public ProcessStatus getStatus() {
        return this.status;
    }

    public void kill(int signal) {
        var pid = this.getPid();
        if (pid != -1) {
            this.posix.kill(pid, signal);
        }
    }
}
