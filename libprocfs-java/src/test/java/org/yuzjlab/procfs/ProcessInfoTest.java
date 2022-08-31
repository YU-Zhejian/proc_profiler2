package org.yuzjlab.procfs;

import org.junit.jupiter.api.Test;
import org.yuzjlab.procfs.exception.ProcessNotExistException;
import org.yuzjlab.procfs.exception.ProcessPermissionDeniedException;
import org.yuzjlab.procfs.exception.ProcessUnknownException;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProcessInfoTest {
    private final ProcessInfo p;
    private final ProcessHandle ph;

    ProcessInfoTest() {
        this.p = new ProcessInfo(ProcessUtils.getCurrentPid());
        this.ph = ProcessHandle.current();
        var runtime = ManagementFactory.getRuntimeMXBean();
        runtime.getPid();
    }


    @Test
    void getCwdPath() throws ProcessPermissionDeniedException, ProcessUnknownException, ProcessNotExistException, IOException {
        var realCwd = Path.of("").toRealPath();
        assertEquals(this.p.getCwdPath(), realCwd);
    }


    @Test
    void getExePath() throws ProcessPermissionDeniedException, ProcessUnknownException, ProcessNotExistException {
        var realExePath = this.ph
                .info()
                .command()
                .orElseThrow();
        assertEquals(this.p.getExePath(), Path.of(realExePath));
    }

}
