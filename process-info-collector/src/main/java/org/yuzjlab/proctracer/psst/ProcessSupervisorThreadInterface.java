package org.yuzjlab.proctracer.psst;

import org.yuzjlab.proctracer.utils.ThreadInterface;

public interface ProcessSupervisorThreadInterface extends ThreadInterface {
    long getPid();
    int getExitValue();
    void kill(int signal);
}