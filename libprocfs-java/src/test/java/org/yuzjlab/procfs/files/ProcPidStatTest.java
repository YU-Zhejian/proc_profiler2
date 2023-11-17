package org.yuzjlab.procfs.files;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.yuzjlab.procfs.TestHelper;
import org.yuzjlab.procfs.exception.ProcessBaseException;

class ProcPidStatTest {

    @Test
    void assertRead() throws ProcessBaseException, IOException {
        var statPath = new TestHelper().resolveResources("/proc_pid_stat.txt");
        var stat = new ProcPidStat(statPath);
        var fileStr = new String(Files.readAllBytes(statPath));
        assertEquals(fileStr, stat.toString());
    }
}
