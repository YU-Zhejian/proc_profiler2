package org.yuzjlab.procfs.process_info;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.yuzjlab.procfs.ProcessUtils;
import org.yuzjlab.procfs.exception.ProcessBaseException;

@DisabledOnOs(WINDOWS)
class EagerEvaluatedProcessInfoTest {
    private final EagerEvaluatedProcessInfo p;
    private final ProcessHandle ph;

    EagerEvaluatedProcessInfoTest() throws ProcessBaseException {
        this.p = new EagerEvaluatedProcessInfo(ProcessUtils.getCurrentPid());
        this.ph = ProcessHandle.current();
    }

    @Test
    void getCwdPath() throws ProcessBaseException, IOException {
        var realCwd = Path.of("").toRealPath();
        assertEquals(this.p.getCwdPath(), realCwd);
    }

    @Test
    void getExePath() throws ProcessBaseException {
        var realExePath = this.ph.info().command().orElseThrow();
        assertEquals(this.p.getExePath(), Path.of(realExePath));
    }
}
