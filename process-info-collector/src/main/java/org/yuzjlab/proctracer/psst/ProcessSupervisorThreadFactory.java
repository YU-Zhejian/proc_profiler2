package org.yuzjlab.proctracer.psst;

import java.io.File;

public class ProcessSupervisorThreadFactory {
    private ProcessSupervisorThreadFactory(){}
    public static ProcessSupervisorThreadInterface create(long pid){
        return new PIDBasedProcessSupervisorThread(pid);
    }
    public static ProcessSupervisorThreadInterface create(String[] cmds, File stdin, File stdout, File stderr, File wd){
        return new CMDBasedProcessSupervisorThread(cmds, stdin, stdout, stderr, wd);
    }
}
