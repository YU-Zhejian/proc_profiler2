package org.yuzjlab.procfs.files;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import org.junit.jupiter.api.Test;
import org.yuzjlab.procfs.TestHelper;
import org.yuzjlab.procfs.exception.ProcessBaseException;

class ProcPidIoTest {
    @Test
    void parseEnviron() throws FileNotFoundException, ProcessBaseException {
        var ioPath = new TestHelper().resolveResources("proc_pid_io.txt");
        var parsedEnviron = new ProcPidIo(ioPath);
        assertEquals(323934931, parsedEnviron.readChars);
        assertEquals(323929600, parsedEnviron.writeChars);
        assertEquals(632687, parsedEnviron.readSyscalls);
        assertEquals(632675, parsedEnviron.writeSyscalls);
        assertEquals(0, parsedEnviron.readBytes);
        assertEquals(323932160, parsedEnviron.writeBytes);
        assertEquals(0, parsedEnviron.cancelledWriteBytes);
    }
}
