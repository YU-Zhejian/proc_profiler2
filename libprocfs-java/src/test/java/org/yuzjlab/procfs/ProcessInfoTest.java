package org.yuzjlab.procfs;

import org.junit.jupiter.api.Test;
import org.yuzjlab.procfs.exception.ProcessBaseException;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProcessInfoTest {
    private final ProcessInfo p;
    private final ProcessHandle ph;

    ProcessInfoTest() throws ProcessBaseException {
        this.p = new ProcessInfo(ProcessUtils.getCurrentPid());
        this.ph = ProcessHandle.current();
    }


    @Test
    void getCwdPath() throws ProcessBaseException, IOException {
        var realCwd = Path.of("").toRealPath();
        assertEquals(this.p.getCwdPath(), realCwd);
    }


    @Test
    void getExePath() throws ProcessBaseException {
        var realExePath = this.ph
                .info()
                .command()
                .orElseThrow();
        assertEquals(this.p.getExePath(), Path.of(realExePath));
    }

}
