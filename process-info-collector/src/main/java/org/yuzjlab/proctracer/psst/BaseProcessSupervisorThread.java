package org.yuzjlab.proctracer.psst;

import jnr.posix.POSIX;
import jnr.posix.POSIXFactory;

public abstract class BaseProcessSupervisorThread extends Thread implements ProcessSupervisorThreadInterface {
    protected    POSIX posix;

    protected BaseProcessSupervisorThread(){
        this.posix = POSIXFactory.getPOSIX();
    }

    public void kill(int signal) {
        var pid = this.getPid();
        if (pid != -1){
            this.posix.kill(this.getPid(), signal);
        }
    }

    @Override
    public void run() {
        // Does nothing
    }
}
