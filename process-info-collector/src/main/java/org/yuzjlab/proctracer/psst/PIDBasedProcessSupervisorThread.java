package org.yuzjlab.proctracer.psst;

import org.yuzjlab.procfs.exception.ProcessBaseException;
import org.yuzjlab.procfs.process_info.EagerEvaluatedProcessInfo;
import org.yuzjlab.procfs.process_info.ProcessInfoInterface;

public class PIDBasedProcessSupervisorThread extends BaseProcessSupervisorThread {
    protected long pid;
    protected ProcessInfoInterface processInfo;

    public PIDBasedProcessSupervisorThread(long pid) {
        try {
            this.status = ProcessStatus.RUNNING;
            this.processInfo = new EagerEvaluatedProcessInfo(pid);
        } catch (ProcessBaseException processBaseException) {
            this.status = ProcessStatus.TERMINATED;
            this.processInfo = null;
        }
    }

    @Override
    public void run() {
        if (this.processInfo == null) {
            return;
        }
        while (this.processInfo.isAlive()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        this.status = ProcessStatus.TERMINATED;
    }

    public long getPid() {
        return this.pid;
    }

    public int getExitValue() {
        return -1;
    }
}
