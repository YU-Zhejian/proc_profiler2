package org.yuzjlab.procfs.files;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
import org.junit.jupiter.api.Test;
import org.yuzjlab.procfs.TestHelper;
import org.yuzjlab.procfs.exception.ProcessBaseException;

class ProcPidStatmTest {
    @Test
    void assertRead() throws FileNotFoundException, ProcessBaseException {
        var statmPath = new TestHelper().resolveResources("proc_pid_statm.txt");
        var parsedStatm = new ProcPidStatm(statmPath);
        assertEquals(1574, parsedStatm.size);
        assertEquals(1394, parsedStatm.resident);
        assertEquals(906, parsedStatm.shared);
        assertEquals(232, parsedStatm.text);
        assertEquals(504, parsedStatm.data);
    }
}
