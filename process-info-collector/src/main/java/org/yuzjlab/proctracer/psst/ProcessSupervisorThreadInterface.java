package org.yuzjlab.proctracer.psst;

public interface ProcessSupervisorThreadInterface extends Runnable {
    long getPid();

    int getExitValue();

    void kill(int signal);
}
