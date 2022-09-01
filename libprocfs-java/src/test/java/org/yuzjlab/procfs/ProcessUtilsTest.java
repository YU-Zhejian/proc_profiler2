package org.yuzjlab.procfs;

import org.junit.jupiter.api.Test;
import org.yuzjlab.procfs.exception.ProcessBaseException;

import java.lang.management.ManagementFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProcessUtilsTest {

    @Test
    void getCurrentPid() throws ProcessBaseException {
        var runtime = ManagementFactory.getRuntimeMXBean();
        long rtPid = runtime.getPid();
        assertEquals(ProcessUtils.getCurrentPid(), rtPid);
    }
}
