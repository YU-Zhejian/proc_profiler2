package org.yuzjlab.procfs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

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
    void getCwdPath() throws IOException {
        var realCwd = Path.of("").toRealPath();
        assertEquals(this.p.getCwdPath(), realCwd);
    }


    @Test
    void getExePath() throws IOException {
        var realExePath = this.ph
                .info()
                .command()
                .orElseThrow();
        assertEquals(this.p.getExePath(), Path.of(realExePath));
    }

}
