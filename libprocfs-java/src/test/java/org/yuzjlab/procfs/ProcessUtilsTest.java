package org.yuzjlab.procfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import java.lang.management.ManagementFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.yuzjlab.procfs.exception.ProcessBaseException;

class ProcessUtilsTest {

    @DisabledOnOs(WINDOWS)
    @Test
    void getCurrentPid() throws ProcessBaseException {
        var runtime = ManagementFactory.getRuntimeMXBean();
        long rtPid = runtime.getPid();
        assertEquals(ProcessUtils.getCurrentPid(), rtPid);
    }
}
