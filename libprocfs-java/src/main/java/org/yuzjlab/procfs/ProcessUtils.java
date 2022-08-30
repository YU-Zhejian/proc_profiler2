package org.yuzjlab.procfs;

import java.lang.management.ManagementFactory;

public class ProcessUtils {

    public static long getCurrentPid() {
        var runtime = ManagementFactory.getRuntimeMXBean();
        return runtime.getPid(); // format: "pid@hostname"

    }

}
