package org.yuzjlab.proctracer.psst;

import java.io.File;
import java.io.IOException;

import jnr.posix.POSIX;
import jnr.posix.POSIXFactory;
import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.procfs.process_info.EagerEvaluatedProcessInfo;
import org.yuzjlab.procfs.process_info.ProcessInfoInterface;


public class PIDBasedProcessSupervisorThread extends BaseProcessSupervisorThread {
    protected long pid;
    protected ProcessInfoInterface processInfo;

    public PIDBasedProcessSupervisorThread(long pid){
        try{
            this.processInfo = new EagerEvaluatedProcessInfo(pid);
        }
        catch (ProcessBaseException processBaseException){
            this.processInfo = null;
        }
    }

    @Override
    public void run() {
        if(this.processInfo == null){
            return;
        }
        while (this.processInfo.isAlive()){
            try {
                sleep(1);
            } catch (InterruptedException ignored) {

            }
        }
    }

    public long getPid() {
        return this.pid;
    }

    public int getExitValue() {
        return -1;
    }
}
